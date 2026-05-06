package com.library.service;

import com.library.dto.request.BorrowRequest;
import com.library.dto.response.BorrowRecordResponse;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowStatus;
import com.library.entity.Member;
import com.library.exception.BusinessRuleException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookService bookService;
    private final MemberService memberService;

    @Value("${library.loan-period-days}")
    private int loanPeriodDays;

    @Value("${library.max-active-borrows-per-member}")
    private int maxActiveBorrows;

    public BorrowRecordResponse borrow(BorrowRequest request) {
        // Validate both entities exist
        Member member = memberService.getMemberOrThrow(request.getMemberId());
        Book book = bookService.getBookOrThrow(request.getBookId());

        // Check book is available
        if (!book.getAvailable()) {
            throw new BusinessRuleException(
                    "Book '" + book.getTitle() + "' is currently not available");
        }

        // Check member hasn't exceeded active borrow limit
        long activeBorrows = borrowRecordRepository
                .countByMemberIdAndStatus(member.getId(), BorrowStatus.ACTIVE);
        if (activeBorrows >= maxActiveBorrows) {
            throw new BusinessRuleException(
                    "Member has reached the maximum limit of "
                            + maxActiveBorrows + " active borrows");
        }

        // Create borrow record
        LocalDate today = LocalDate.now();
        BorrowRecord record = BorrowRecord.builder()
                .book(book)
                .member(member)
                .borrowedAt(today)
                .dueDate(today.plusDays(loanPeriodDays))
                .status(BorrowStatus.ACTIVE)
                .build();

        // Mark book as unavailable
        book.setAvailable(false);

        // Save both
        borrowRecordRepository.save(record);

        return toResponse(record);
    }

    @Transactional
    public BorrowRecordResponse returnBook(Long borrowId) {
        // Finds borrow record
        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Borrow record not found with id: " + borrowId));

        // checks if already returned
        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BusinessRuleException("This book has already been returned");
        }

        // Set return date and determine final status
        LocalDate today = LocalDate.now();
        record.setReturnedAt(today);
        record.setStatus(today.isAfter(record.getDueDate())
                ? BorrowStatus.OVERDUE
                : BorrowStatus.RETURNED);

        // Mark book as available again
        record.getBook().setAvailable(true);

        return toResponse(borrowRecordRepository.save(record));
    }

    public List<BorrowRecordResponse> getMemberBorrowHistory(Long memberId) {
        // Validates member exists first
        memberService.getMemberOrThrow(memberId);
        return borrowRecordRepository.findByMemberId(memberId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

// helper method
    private BorrowRecordResponse toResponse(BorrowRecord record) {
        return BorrowRecordResponse.builder()
                .id(record.getId())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .memberId(record.getMember().getId())
                .memberName(record.getMember().getName())
                .borrowedAt(record.getBorrowedAt())
                .dueDate(record.getDueDate())
                .returnedAt(record.getReturnedAt())
                .status(record.getStatus())
                .build();
    }
}