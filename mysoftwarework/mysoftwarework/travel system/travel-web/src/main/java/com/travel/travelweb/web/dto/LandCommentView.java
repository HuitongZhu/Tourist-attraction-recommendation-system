package com.travel.travelweb.web.dto;

import java.time.LocalDateTime;

public record LandCommentView(
        String commentId,
        String userId,
        String userName,
        String content,
        LocalDateTime publishTime
) {
}
