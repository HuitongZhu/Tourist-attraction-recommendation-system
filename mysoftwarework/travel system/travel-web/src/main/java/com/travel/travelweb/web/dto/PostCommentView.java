package com.travel.travelweb.web.dto;

import java.time.LocalDateTime;

public record PostCommentView(
        String commentId,
        String userId,
        String userName,
        String content,
        LocalDateTime publishTime
) {
}
