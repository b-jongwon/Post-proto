package com.facthub.factcheck.domain;

/*
 * 출처가 특정 주장에 어떤 역할을 하는지 나타낸다.
 */
public enum EvidenceStance {

    /*
     * 출처가 해당 주장을 뒷받침한다.
     */
    SUPPORTS,

    /*
     * 출처가 해당 주장을 반박한다.
     */
    REFUTES,

    /*
     * 직접적인 지지·반박보다는
     * 배경이나 맥락을 제공한다.
     */
    CONTEXT
}