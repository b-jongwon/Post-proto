package com.facthub.factcheck.repository;

import com.facthub.factcheck.domain.PostAnalysisSelection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostAnalysisSelectionRepository
        extends JpaRepository<
        PostAnalysisSelection,
        Long
        > {

    /*
     * 게시글의 현재 대표 분석 선택 정보를 조회한다.
     *
     * post_analysis_selections의 기본키가
     * post_id이므로 findById(postId)를 사용해도 되지만,
     * 관계 객체를 함께 가져오기 위해 별도 메서드를 둔다.
     */
    @EntityGraph(
            attributePaths = {
                    "post",
                    "analysis",
                    "analysis.requestedBy",
                    "selectedBy"
            }
    )
    Optional<PostAnalysisSelection>
    findByPost_Id(
            Long postId
    );

    /*
     * 게시글에 대표 분석이 이미 지정되어 있는지 확인한다.
     */
    boolean existsByPost_Id(
            Long postId
    );

    /*
     * 특정 분석이 이미 어느 게시글의
     * 대표 분석으로 지정되어 있는지 확인한다.
     */
    boolean existsByAnalysis_Id(
            Long analysisId
    );
}