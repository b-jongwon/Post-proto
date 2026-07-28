package com.facthub.factcheck.repository;

import com.facthub.factcheck.domain.FactCheckClaim;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FactCheckClaimRepository
        extends JpaRepository<FactCheckClaim, Long> {

    /*
     * 특정 분석에 포함된 주장들을
     * 원래 표시 순서대로 조회한다.
     *
     * Evidence와 Source도 함께 불러온다.
     */
    @EntityGraph(
            attributePaths = {
                    "evidences",
                    "evidences.source"
            }
    )
    List<FactCheckClaim>
    findAllByAnalysis_IdOrderByClaimOrderAsc(
            Long analysisId
    );

    /*
     * 분석에 포함된 주장 개수
     *
     * 분석 이력 목록에서 claimCount를
     * 표시할 때 사용할 수 있다.
     */
    long countByAnalysis_Id(
            Long analysisId
    );

    /*
     * 특정 분석의 모든 주장 삭제
     *
     * 일반적인 정상 흐름에서는
     * Analysis의 cascade로 삭제되지만,
     * 관리·복구 작업을 위해 제공한다.
     */
    void deleteAllByAnalysis_Id(
            Long analysisId
    );
}