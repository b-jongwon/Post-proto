package com.facthub.factcheck.repository;

import com.facthub.factcheck.domain.FactCheckAnalysis;
import com.facthub.factcheck.domain.FactCheckStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FactCheckAnalysisRepository
        extends JpaRepository<FactCheckAnalysis, Long> {

    /*
     * 특정 게시글에 속한 특정 분석 조회
     *
     * analysisId만 조회하지 않고 postId도 함께 검사하여
     * 다른 게시글의 분석을 잘못 조회하는 것을 방지한다.
     */
    @EntityGraph(
            attributePaths = {
                    "post",
                    "requestedBy"
            }
    )
    Optional<FactCheckAnalysis> findByIdAndPost_Id(
            Long analysisId,
            Long postId
    );

    /*
     * 게시글의 전체 분석 이력 조회
     *
     * 가장 최근 실행한 분석이 먼저 나오도록
     * runNumber 내림차순으로 정렬한다.
     */
    @EntityGraph(
            attributePaths = {
                    "post",
                    "requestedBy"
            }
    )
    List<FactCheckAnalysis>
    findAllByPost_IdOrderByRunNumberDesc(
            Long postId
    );

    /*
     * 게시글의 가장 최근 분석 조회
     *
     * 대표 분석 조회용은 아니며,
     * 최근 실행 상태나 재분석 조건을 확인할 때 사용한다.
     */
    @EntityGraph(
            attributePaths = {
                    "post",
                    "requestedBy"
            }
    )
    Optional<FactCheckAnalysis>
    findFirstByPost_IdOrderByRunNumberDesc(
            Long postId
    );

    /*
     * 해당 게시글에 특정 상태의 분석이 존재하는지 확인
     *
     * PROCESSING 분석이 이미 있으면
     * 같은 게시글의 동시 분석 요청을 차단할 수 있다.
     */
    boolean existsByPost_IdAndStatus(
            Long postId,
            FactCheckStatus status
    );

    /*
     * 게시글의 현재 최대 실행 번호 조회
     *
     * 분석 기록이 없으면 0을 반환한다.
     *
     * 예:
     * 기존 최대 runNumber = 2
     * 새 분석 runNumber = 3
     */
    @Query("""
            SELECT COALESCE(
                    MAX(analysis.runNumber),
                    0
            )
            FROM FactCheckAnalysis analysis
            WHERE analysis.post.id = :postId
            """)
    Integer findMaxRunNumberByPostId(
            @Param("postId")
            Long postId
    );

    /*
     * 게시글이 수정되었을 때
     * 기존의 모든 분석을 오래된 분석으로 표시한다.
     *
     * 이미 stale인 행은 다시 수정하지 않는다.
     *
     * 반환값:
     * stale 처리된 분석 행 개수
     */
    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            UPDATE FactCheckAnalysis analysis
            SET analysis.stale = true
            WHERE analysis.post.id = :postId
              AND analysis.stale = false
            """)
    int markAllStaleByPostId(
            @Param("postId")
            Long postId
    );
}