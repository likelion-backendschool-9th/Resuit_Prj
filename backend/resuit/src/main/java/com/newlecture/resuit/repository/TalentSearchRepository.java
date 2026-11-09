package com.newlecture.resuit.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.newlecture.resuit.dto.TalentSearchCondition;
import com.newlecture.resuit.entity.UserBadge;
import com.newlecture.resuit.entity.UserBadgeHistory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

// 인재 찾기 검색 쿼리. 들어온 조건만 WHERE에 붙이는 동적 SQL이라 메서드 이름 쿼리 대신 직접 조립한다.
// 결과는 회원별 대표 공개 이력서(가장 최근 공개 이력서) ID 페이지다. 엔티티는 서비스에서 따로 읽는다.
@Repository
public class TalentSearchRepository {

	// 회원별 경력 일수. 겹치는 기간은 한 번만 세고, 재직 중(end_date 없음)은 오늘까지로 계산한다.
	// 1) 시작일 순으로 정렬해 앞선 경력들의 가장 늦은 종료일(prev_end)을 구한다.
	// 2) 시작일이 prev_end보다 늦으면 새 묶음이 시작된다(grp 증가). 겹치거나 이어지면 같은 묶음이다.
	// 3) 묶음마다 (가장 늦은 종료일 - 가장 이른 시작일)을 구해 모두 더한다. 경력이 없으면 0일이다.
	private static final String CAREER_DAYS = """
			(SELECT COALESCE(SUM(m.e - m.s), 0) FROM (
			    SELECT MIN(g.s) AS s, MAX(g.e) AS e FROM (
			        SELECT a.s, a.e,
			               SUM(CASE WHEN a.prev_end IS NULL OR a.s > a.prev_end THEN 1 ELSE 0 END)
			                   OVER (ORDER BY a.s, a.e) AS grp
			        FROM (
			            SELECT uc.start_date AS s, COALESCE(uc.end_date, :today) AS e,
			                   MAX(COALESCE(uc.end_date, :today)) OVER (
			                       ORDER BY uc.start_date, COALESCE(uc.end_date, :today)
			                       ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING) AS prev_end
			            FROM user_careers uc WHERE uc.user_id = u.id
			        ) a
			    ) g GROUP BY g.grp
			) m)
			""";

	private static final int ONE_YEAR_DAYS = 365;
	private static final int THREE_YEARS_DAYS = 365 * 3;

	@PersistenceContext
	private EntityManager em;

	public Page<Long> searchResumeIds(TalentSearchCondition c, LocalDate today, LocalDateTime now) {
		Map<String, Object> params = new HashMap<>();

		String fieldCond = c.field() == null ? "" : " AND o.field = :field";
		if (c.field() != null) {
			params.put("field", c.field());
		}
		// 회원별 가장 최근 공개 이력서 하나만 남긴다. 그래서 한 사람이 한 줄로 나온다.
		String where = " WHERE c.is_public = true"
				+ (c.field() == null ? "" : " AND c.field = :field")
				+ " AND c.id = (SELECT MAX(o.id) FROM common_resumes o"
				+ " WHERE o.user_id = c.user_id AND o.is_public = true" + fieldCond + ")"
				+ filters(c, today, now, params);

		Pageable pageable = c.toPageable();

		Query select = em.createNativeQuery("SELECT c.id" + FROM + where + " ORDER BY c.id DESC");
		params.forEach(select::setParameter);
		select.setFirstResult((int) pageable.getOffset());
		select.setMaxResults(pageable.getPageSize());
		List<Long> ids = ((List<?>) select.getResultList()).stream()
				.map(id -> ((Number) id).longValue()).toList();

		Query count = em.createNativeQuery("SELECT COUNT(*)" + FROM + where);
		params.forEach(count::setParameter);
		long total = ((Number) count.getSingleResult()).longValue();

		return new PageImpl<>(ids, pageable, total);
	}

	// 분야 탭 옆에 보여 줄 인원 수. 상세 필터는 그대로 반영하고 분야 조건만 뺀다.
	// 키 ALL은 전체 탭(회원별 최신 공개 이력서 기준), 나머지 키는 분야별(회원별 그 분야의 최신 공개 이력서 기준)이다.
	public Map<String, Long> countByField(TalentSearchCondition c, LocalDate today, LocalDateTime now) {
		Map<String, Object> params = new HashMap<>();
		String filters = filters(c, today, now, params);
		Map<String, Long> counts = new HashMap<>();

		Query all = em.createNativeQuery("SELECT COUNT(*)" + FROM
				+ " WHERE c.is_public = true AND c.id = (SELECT MAX(o.id) FROM common_resumes o"
				+ " WHERE o.user_id = c.user_id AND o.is_public = true)" + filters);
		params.forEach(all::setParameter);
		counts.put("ALL", ((Number) all.getSingleResult()).longValue());

		Query byField = em.createNativeQuery("SELECT c.field, COUNT(*)" + FROM
				+ " WHERE c.is_public = true AND c.field IS NOT NULL AND c.id = (SELECT MAX(o.id) FROM common_resumes o"
				+ " WHERE o.user_id = c.user_id AND o.is_public = true AND o.field = c.field)" + filters
				+ " GROUP BY c.field");
		params.forEach(byField::setParameter);
		for (Object row : byField.getResultList()) {
			Object[] r = (Object[]) row;
			counts.put((String) r[0], ((Number) r[1]).longValue());
		}
		return counts;
	}

	// 회원별 경력 일수(겹친 기간은 한 번만). 카드의 경력 구간 표시에 쓴다. 필터와 같은 계산식이다.
	public Map<UUID, Long> careerDays(Collection<UUID> userIds, LocalDate today) {
		if (userIds.isEmpty()) {
			return Map.of();
		}
		Query q = em.createNativeQuery("SELECT u.id, " + CAREER_DAYS + " FROM users u WHERE u.id IN (:ids)");
		q.setParameter("ids", userIds);
		q.setParameter("today", today);
		return toMap(q.getResultList());
	}

	public Map<UUID, Long> postCounts(Collection<UUID> userIds) {
		if (userIds.isEmpty()) {
			return Map.of();
		}
		Query q = em.createNativeQuery("SELECT p.user_id, COUNT(*) FROM posts p"
				+ " WHERE p.user_id IN (:ids) AND p.deleted_at IS NULL GROUP BY p.user_id");
		q.setParameter("ids", userIds);
		return toMap(q.getResultList());
	}

	// 여러 회원의 배지를 배지 정보와 함께 한 번에 읽는다. 회원마다 따로 읽지 않으려고 IN으로 묶는다.
	public List<UserBadge> badges(Collection<UUID> userIds) {
		if (userIds.isEmpty()) {
			return List.of();
		}
		return em.createQuery("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.user.id IN :ids", UserBadge.class)
				.setParameter("ids", userIds).getResultList();
	}

	public List<UserBadgeHistory> badgeHistory(Collection<UUID> userIds) {
		if (userIds.isEmpty()) {
			return List.of();
		}
		return em.createQuery("SELECT h FROM UserBadgeHistory h JOIN FETCH h.badge WHERE h.user.id IN :ids"
				+ " ORDER BY h.activeUntil DESC", UserBadgeHistory.class)
				.setParameter("ids", userIds).getResultList();
	}

	private static final String FROM = " FROM common_resumes c JOIN users u ON u.id = c.user_id";

	// [Pro] 상세 필터 조건을 SQL 조각으로 만들고, 바인딩 값을 params에 넣는다. 목록과 탭 인원 수가 같이 쓴다.
	private String filters(TalentSearchCondition c, LocalDate today, LocalDateTime now, Map<String, Object> params) {
		StringBuilder where = new StringBuilder();
		if (c.badge() != null) {
			where.append(" AND EXISTS (SELECT 1 FROM user_badge ub WHERE ub.user_id = u.id AND ub.status = 'ACTIVE')");
		}
		// 배지 화면과 같은 계산(달 차이 / 3)이 streak 이상이 되려면 continuous_since가 기준 시각보다 앞서야 한다.
		if (c.streak() != null) {
			where.append(" AND EXISTS (SELECT 1 FROM user_badge ub WHERE ub.user_id = u.id")
					.append(" AND ub.status = 'ACTIVE' AND ub.continuous_since < :streakBefore)");
			// 이번 달 1일에서 (3 * streak - 1)달 전. 이보다 이전에 시작했으면 streak 분기 이상이다.
			params.put("streakBefore", now.toLocalDate().withDayOfMonth(1).minusMonths(3L * c.streak() - 1).atStartOfDay());
		}
		if (c.skills() != null && !c.skills().isEmpty()) {
			where.append(" AND jsonb_exists_all(c.content -> 'skills', string_to_array(CAST(:skills AS text), ','))");
			params.put("skills", String.join(",", c.skills()));
		}
		// [Pro] 경력 구간. 경력이 없는 회원은 0일이라 신입(ENTRY)에 들어간다.
		if (c.career() != null) {
			String range = switch (c.career()) {
				case "ENTRY" -> " < " + ONE_YEAR_DAYS;
				case "ONE_TO_THREE" -> " BETWEEN " + ONE_YEAR_DAYS + " AND " + (THREE_YEARS_DAYS - 1);
				default -> " >= " + THREE_YEARS_DAYS;
			};
			where.append(" AND ").append(CAREER_DAYS).append(range);
			params.put("today", today);
		}
		// [Pro] 저장해 둔 GitHub 기여 수가 최소값 이상이어야 한다. 아직 저장되지 않은 회원은 0으로 본다.
		if (c.minContrib() != null) {
			where.append(" AND COALESCE((SELECT g.total_contributions FROM github_contribution_stats g")
					.append(" WHERE g.user_id = u.id), 0) >= :minContrib");
			params.put("minContrib", c.minContrib());
		}
		if (c.minPosts() != null) {
			where.append(" AND (SELECT COUNT(*) FROM posts p WHERE p.user_id = u.id AND p.deleted_at IS NULL) >= :minPosts");
			params.put("minPosts", c.minPosts());
		}
		if (c.region() != null) {
			where.append(" AND u.region = :region");
			params.put("region", c.region().name());
		}
		return where.toString();
	}

	private static Map<UUID, Long> toMap(List<?> rows) {
		Map<UUID, Long> map = new HashMap<>();
		for (Object row : rows) {
			Object[] r = (Object[]) row;
			map.put((UUID) r[0], ((Number) r[1]).longValue());
		}
		return map;
	}
}
