package com.library.dto.response;

import com.library.entity.BorrowStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BorrowRecordResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long memberId;
    private String memberName;
    private LocalDate borrowedAt;
    private LocalDate dueDate;
    private LocalDate returnedAt;
    private BorrowStatus status;
}