package com.facthub.factcheck.repository;

import com.facthub.factcheck.domain.FactCheckEvidence;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FactCheckEvidenceRepository
        extends JpaRepository<FactCheckEvidence, Long> {

    /*
     * 특정 주장에 연결된 근거들을
     * 표시 순서대로 조회한다.
     *
     * 근거가 참조하는 Source도
     * 함께 조회한다.
     */
    @EntityGraph(
            attributePaths = {
                    "source"
            }
    )
    List<FactCheckEvidence>
    findAllByClaim_IdOrderByEvidenceOrderAsc(
            Long claimId
    );

    /*
     * 해당 주장에 연결된 근거 개수
     */
    long countByClaim_Id(
            Long claimId
    );

    /*
     * 특정 출처가 몇 개의 근거에서
     * 사용되는지 확인한다.
     */
    long countBySource_Id(
            Long sourceId
    );

    /*
     * 특정 주장의 모든 근거 삭제
     */
    void deleteAllByClaim_Id(
            Long claimId
    );
}