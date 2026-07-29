package com.facthub.factcheck.repository;

import com.facthub.factcheck.domain.FactCheckStatus;
import com.facthub.factcheck.domain.PostAnalysisSelection;
import com.facthub.post.domain.PostStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostAnalysisSelectionRepository
        extends JpaRepository<
        PostAnalysisSelection,
        Long
        > {

    /*
     * 게시글의 현재 대표 분석 선택 정보 조회
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
     * 게시글 목록에 표시할 대표 분석 일괄 조회
     *
     * 페이지에 포함된 게시글 ID를 한 번에 조회해
     * 게시글마다 개별 쿼리가 실행되는 N+1 문제를 막는다.
     */
    @EntityGraph(
            attributePaths = {
                    "post",
                    "analysis",
                    "analysis.requestedBy",
                    "selectedBy"
            }
    )
    List<PostAnalysisSelection>
    findAllByPost_IdIn(
            Collection<Long> postIds
    );

    /*
     * 현재 유효한 검증 완료 게시글 수
     *
     * 대표 분석이 존재하고,
     * 분석 상태가 COMPLETED이며,
     * 게시글 수정 이후 stale 상태가 아닌 경우만 센다.
     */
    @Query("""
            SELECT COUNT(selection)
            FROM PostAnalysisSelection selection
            WHERE selection.post.status = :postStatus
              AND selection.analysis.status = :analysisStatus
              AND selection.analysis.stale = false
            """)
    long countCurrentVerifiedPosts(
            @Param("postStatus")
            PostStatus postStatus,

            @Param("analysisStatus")
            FactCheckStatus analysisStatus
    );

    boolean existsByPost_Id(
            Long postId
    );

    boolean existsByAnalysis_Id(
            Long analysisId
    );
}