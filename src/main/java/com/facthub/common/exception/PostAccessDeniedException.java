package com.facthub.common.exception;

public class PostAccessDeniedException
        extends RuntimeException {

    public PostAccessDeniedException() {
        super("게시글을 수정하거나 삭제할 권한이 없습니다.");
    }
}