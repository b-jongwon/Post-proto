package com.facthub.factcheck.domain;

/*
 * 팩트체크에 사용된 출처의 유형
 */
public enum FactCheckSourceType {

    /*
     * 정부기관 자료
     */
    GOVERNMENT,

    /*
     * 공공기관 자료
     */
    PUBLIC_INSTITUTION,

    /*
     * 논문·학술 자료
     */
    ACADEMIC,

    /*
     * 법령, 판결문, 통계 원문 등 1차 자료
     */
    PRIMARY_SOURCE,

    /*
     * 언론 기사
     */
    NEWS,

    /*
     * 백과사전·지식 사전
     */
    ENCYCLOPEDIA,

    /*
     * 온라인 커뮤니티
     */
    COMMUNITY,

    /*
     * 개인 또는 기관 블로그
     */
    BLOG,

    /*
     * 영상 자료
     */
    VIDEO,

    /*
     * 분류하기 어려운 기타 출처
     */
    OTHER
}