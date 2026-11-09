package com.newlecture.resuit.entity;

// 회원 지역. DB(users.region)에는 이 enum의 이름(SEOUL 등)이 문자열로 저장된다.
// 지역을 추가할 때는 여기에 값만 더하면 된다. DB 컬럼은 VARCHAR(20)이고 CHECK 제약이 없어 바꿀 필요가 없다.
public enum Region {
    SEOUL("서울"),
    BUSAN("부산"),
    DAEGU("대구"),
    INCHEON("인천"),
    GWANGJU("광주"),
    DAEJEON("대전"),
    ULSAN("울산"),
    GYEONGGI("경기"),
    JEJU("제주"),
    ETC("기타");

    private final String label;

    Region(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
