package com.facthub.factcheck.repository;

import com.facthub.factcheck.domain.FactCheckSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactCheckSourceRepository
        extends JpaRepository<FactCheckSource, Long> {

    /*
     * 특정 분석의 전체 출처를
     * 화면 표시 순서대로 조회한다.
     */
    List<FactCheckSource>
    findAllByAnalysis_IdOrderBySourceOrderAsc(
            Long analysisId
    );

    /*
     * 동일 분석 안에서 같은 URL 출처가
     * 이미 저장되어 있는지 확인한다.
     *
     * 긴 URL 자체가 아니라
     * SHA-256 URL 해시를 사용한다.
     */
    Optional<FactCheckSource>
    findByAnalysis_IdAndUrlHash(
            Long analysisId,
            String urlHash
    );

    /*
     * 동일 분석 안에서 특정 URL 해시가
     * 이미 존재하는지 확인한다.
     */
    boolean existsByAnalysis_IdAndUrlHash(
            Long analysisId,
            String urlHash
    );

    /*
     * 분석에 사용된 출처 개수
     */
    long countByAnalysis_Id(
            Long analysisId
    );

    /*
     * 특정 분석의 모든 출처 삭제
     */
    void deleteAllByAnalysis_Id(
            Long analysisId
    );
}