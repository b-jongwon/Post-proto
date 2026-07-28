package com.facthub.factcheck.domain;

import com.facthub.post.domain.Post;
import com.facthub.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_analysis_selections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name =
                                "uk_post_analysis_selections_analysis",
                        columnNames = "analysis_id"
                )
        }
)
public class PostAnalysisSelection {

    /*
     * 게시글 ID를 그대로 대표 분석 선택 정보의
     * 기본키로 사용한다.
     *
     * 게시글 하나당 대표 분석은 하나만 존재한다.
     */
    @Id
    @Column(name = "post_id")
    private Long postId;

    /*
     * 대표 분석을 보유한 게시글
     *
     * @MapsId를 사용하므로
     * Post의 ID가 이 Entity의 기본키가 된다.
     */
    @MapsId
    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "post_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name =
                            "fk_post_analysis_selections_post"
            )
    )
    private Post post;

    /*
     * 현재 게시글의 대표 팩트체크 분석
     *
     * 하나의 분석 결과를 여러 게시글의
     * 대표 분석으로 사용할 수 없도록 UNIQUE 처리한다.
     */
    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "analysis_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name =
                            "fk_post_analysis_selections_analysis"
            )
    )
    private FactCheckAnalysis analysis;

    /*
     * 대표 분석을 지정한 사용자
     *
     * 최초 분석에서는 분석 요청자,
     * 이후 변경에서는 작성자 또는 관리자가 들어간다.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "selected_by",
            nullable = false,
            foreignKey = @ForeignKey(
                    name =
                            "fk_post_analysis_selections_user"
            )
    )
    private User selectedBy;

    /*
     * 현재 대표 분석이 지정된 시각
     */
    @Column(
            name = "selected_at",
            nullable = false
    )
    private LocalDateTime selectedAt;

    protected PostAnalysisSelection() {
    }

    private PostAnalysisSelection(
            Post post,
            FactCheckAnalysis analysis,
            User selectedBy
    ) {
        this.post = post;
        this.analysis = analysis;
        this.selectedBy = selectedBy;
        this.selectedAt =
                LocalDateTime.now();
    }

    /*
     * 최초 대표 분석 선택 정보 생성
     *
     * 첫 번째 분석이 성공적으로 완료되었을 때 사용한다.
     */
    public static PostAnalysisSelection create(
            Post post,
            FactCheckAnalysis analysis,
            User selectedBy
    ) {
        validateRequiredValues(
                post,
                analysis,
                selectedBy
        );

        validateAnalysis(
                post,
                analysis
        );

        return new PostAnalysisSelection(
                post,
                analysis,
                selectedBy
        );
    }

    /*
     * 대표 분석 변경
     *
     * 기존 Selection 행을 삭제하고 다시 만드는 대신
     * analysis_id와 선택자, 선택 시각만 변경한다.
     */
    public void changeAnalysis(
            FactCheckAnalysis newAnalysis,
            User selectedBy
    ) {
        if (newAnalysis == null) {
            throw new IllegalArgumentException(
                    "새 대표 분석은 필수입니다."
            );
        }

        if (selectedBy == null) {
            throw new IllegalArgumentException(
                    "대표 분석 선택자는 필수입니다."
            );
        }

        validateAnalysis(
                this.post,
                newAnalysis
        );

        this.analysis = newAnalysis;
        this.selectedBy = selectedBy;
        this.selectedAt =
                LocalDateTime.now();
    }

    /*
     * 필수 값 검증
     */
    private static void validateRequiredValues(
            Post post,
            FactCheckAnalysis analysis,
            User selectedBy
    ) {
        if (post == null) {
            throw new IllegalArgumentException(
                    "대표 분석이 연결될 게시글은 필수입니다."
            );
        }

        if (analysis == null) {
            throw new IllegalArgumentException(
                    "대표 분석은 필수입니다."
            );
        }

        if (selectedBy == null) {
            throw new IllegalArgumentException(
                    "대표 분석 선택자는 필수입니다."
            );
        }
    }

    /*
     * 대표 분석으로 선택 가능한 분석인지 검증한다.
     */
    private static void validateAnalysis(
            Post post,
            FactCheckAnalysis analysis
    ) {
        validateSamePost(
                post,
                analysis.getPost()
        );

        if (!analysis.isCompleted()) {
            throw new IllegalArgumentException(
                    "완료된 분석만 대표 분석으로 지정할 수 있습니다."
            );
        }
    }

    /*
     * 선택하려는 분석이 실제로
     * 해당 게시글의 분석인지 검증한다.
     *
     * 다른 게시글의 분석을 잘못 선택하는 것을 방지한다.
     */
    private static void validateSamePost(
            Post post,
            Post analysisPost
    ) {
        if (analysisPost == null) {
            throw new IllegalArgumentException(
                    "분석에 연결된 게시글이 없습니다."
            );
        }

        if (post == analysisPost) {
            return;
        }

        Long postId = post.getId();
        Long analysisPostId =
                analysisPost.getId();

        boolean samePersistedPost =
                postId != null
                        && analysisPostId != null
                        && postId.equals(
                        analysisPostId
                );

        if (!samePersistedPost) {
            throw new IllegalArgumentException(
                    "해당 게시글의 분석만 대표 분석으로 지정할 수 있습니다."
            );
        }
    }

    @PrePersist
    private void prePersist() {
        if (this.selectedAt == null) {
            this.selectedAt =
                    LocalDateTime.now();
        }
    }

    public Long getPostId() {
        return postId;
    }

    public Post getPost() {
        return post;
    }

    public FactCheckAnalysis getAnalysis() {
        return analysis;
    }

    public User getSelectedBy() {
        return selectedBy;
    }

    public LocalDateTime getSelectedAt() {
        return selectedAt;
    }
}