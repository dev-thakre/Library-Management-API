package com.library.service;

import com.library.dto.request.BorrowRequest;
import com.library.entity.*;
import com.library.exception.BusinessRuleException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BorrowRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookService bookService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private BorrowService borrowService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(borrowService, "loanPeriodDays", 14);
        ReflectionTestUtils.setField(borrowService, "maxActiveBorrows", 3);
    }

//    -------- Test Cases for Borrow Method ---------
    @Test
    void shouldBorrowBookSuccessfully_whenBookAvailableAndWithinLimit() {
//        create borrow request
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);
        request.setMemberId(1L);

//        create member
        Member member = new Member();
        member.setId(1L);
        member.setName("Dev");

//        create book
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Java Book");
        book.setAvailable(true);

        when(memberService.getMemberOrThrow(1L)).thenReturn(member);
        when(bookService.getBookOrThrow(1L)).thenReturn(book);
        when(borrowRecordRepository.countByMemberIdAndStatus(1L, BorrowStatus.ACTIVE))
                .thenReturn(0L);

        var response = borrowService.borrow(request);

//        Checking response
        assertNotNull(response);
        assertEquals(1L, response.getBookId());
        assertEquals("Java Book", response.getBookTitle());

        verify(borrowRecordRepository, times(1)).save(any(BorrowRecord.class));
    }

    @Test
    void shouldThrowException_whenBookNotAvailable() {

        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);
        request.setMemberId(1L);

        Member member = new Member();
        member.setId(1L);

//        creating book with false availability
        Book book = new Book();
        book.setId(1L);
        book.setAvailable(false);

        when(memberService.getMemberOrThrow(1L)).thenReturn(member);
        when(bookService.getBookOrThrow(1L)).thenReturn(book);

//        method must throw exception
        assertThrows(BusinessRuleException.class, () -> {
            borrowService.borrow(request);
        });

        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenMaxBorrowLimitExceeded() {

        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);
        request.setMemberId(1L);

        Member member = new Member();
        member.setId(1L);

        Book book = new Book();
        book.setId(1L);
        book.setAvailable(true);

        when(memberService.getMemberOrThrow(1L)).thenReturn(member);
        when(bookService.getBookOrThrow(1L)).thenReturn(book);

        when(borrowRecordRepository.countByMemberIdAndStatus(1L, BorrowStatus.ACTIVE))
                .thenReturn(3L); // equals max limit

        assertThrows(BusinessRuleException.class, () -> borrowService.borrow(request));

        verify(borrowRecordRepository, never()).save(any());
    }

//    ------- Test cases for returnBook Method -------

    @Test
    void shouldReturnBookSuccessfully_whenWithinDueDate() {

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Java Book");
        book.setAvailable(false);

        Member member = new Member();
        member.setId(1L);
        member.setName("Dev");

        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setBook(book);
        record.setDueDate(LocalDate.now().plusDays(5));
        record.setStatus(BorrowStatus.ACTIVE);
        record.setMember(member);

        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(borrowRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = borrowService.returnBook(1L);

        assertNotNull(response);
        assertEquals(BorrowStatus.RETURNED, response.getStatus());
        assertTrue(book.getAvailable());

        verify(borrowRecordRepository).save(record);
    }

    @Test
    void shouldMarkAsOverdue_whenReturnedAfterDueDate() {

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Java Book");
        book.setAvailable(false);

        Member member = new Member();
        member.setId(1L);
        member.setName("Dev");

        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setBook(book);
        record.setMember(member);
        record.setDueDate(LocalDate.now().minusDays(2));
        record.setStatus(BorrowStatus.ACTIVE);


        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(borrowRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = borrowService.returnBook(1L);

        assertEquals(BorrowStatus.OVERDUE, response.getStatus());
        assertTrue(book.getAvailable());
    }

    @Test
    void shouldThrowException_whenBookAlreadyReturned() {

        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setStatus(BorrowStatus.RETURNED);

        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertThrows(BusinessRuleException.class, () -> borrowService.returnBook(1L));

        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenBorrowRecordNotFound() {
        when(borrowRecordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> borrowService.returnBook(1L));
    }

    @Test
    void shouldReturnBorrowHistoryForMember() {

        Member member = new Member();
        member.setId(1L);

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Java Book");

        BorrowRecord record = new BorrowRecord();
        record.setId(1L);
        record.setBook(book);
        record.setMember(member);
        record.setStatus(BorrowStatus.ACTIVE);
        record.setBorrowedAt(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(5));

        when(memberService.getMemberOrThrow(1L)).thenReturn(member);
        when(borrowRecordRepository.findByMemberId(1L))
                .thenReturn(List.of(record));

        var result = borrowService.getMemberBorrowHistory(1L);

        assertEquals(1, result.size());
        assertEquals("Java Book", result.get(0).getBookTitle());
    }
}
