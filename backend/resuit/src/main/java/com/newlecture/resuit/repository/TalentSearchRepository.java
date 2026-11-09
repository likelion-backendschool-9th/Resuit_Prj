package com.newlecture.resuit.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.newlecture.resuit.dto.TalentSearchCondition;

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
		StringBuilder where = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		String fieldCond = c.field() == null ? "" : " AND o.field = :field";
		if (c.field() != null) {
			params.put("field", c.field());
		}
		// 회원별 가장 최근 공개 이력서 하나만 남긴다. 그래서 한 사람이 한 줄로 나온다.
		where.append(" WHERE c.is_public = true")
				.append(c.field() == null ? "" : " AND c.field = :field")
				.append(" AND c.id = (SELECT MAX(o.id) FROM common_resumes o")
				.append(" WHERE o.user_id = c.user_id AND o.is_public = true").append(fieldCond).append(")");

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

		String from = " FROM common_resumes c JOIN users u ON u.id = c.user_id";
		Pageable pageable = c.toPageable();

		Query select = em.createNativeQuery("SELECT c.id" + from + where + " ORDER BY c.id DESC");
		params.forEach(select::setParameter);
		select.setFirstResult((int) pageable.getOffset());
		select.setMaxResults(pageable.getPageSize());
		List<Long> ids = ((List<?>) select.getResultList()).stream()
				.map(id -> ((Number) id).longValue()).toList();

		Query count = em.createNativeQuery("SELECT COUNT(*)" + from + where);
		params.forEach(count::setParameter);
		long total = ((Number) count.getSingleResult()).longValue();

		return new PageImpl<>(ids, pageable, total);
	}
}
