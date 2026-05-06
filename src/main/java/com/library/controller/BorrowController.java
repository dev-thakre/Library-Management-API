package com.library.controller;

import com.library.dto.request.BorrowRequest;
import com.library.dto.response.BorrowRecordResponse;
import com.library.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Borrowing", description = "Manage book borrowing and returns")
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/api/borrows")
    @Operation(summary = "Borrow a book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book borrowed successfully"),
            @ApiResponse(responseCode = "400", description = "Book unavailable or borrow limit reached"),
            @ApiResponse(responseCode = "404", description = "Book or Member not found")
    })
    public ResponseEntity<BorrowRecordResponse> borrow(@Valid @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowService.borrow(request));
    }

    @PutMapping("/api/borrows/{id}/return")
    @Operation(summary = "Return a borrowed book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book returned successfully"),
            @ApiResponse(responseCode = "404", description = "Borrow record not found")
    })
    public ResponseEntity<BorrowRecordResponse> returnBook(
            @Parameter(description = "Borrow record ID") @PathVariable Long id) {
        return ResponseEntity.ok(borrowService.returnBook(id));
    }

    @GetMapping("/api/members/{id}/borrows")
    @Operation(summary = "Get borrow history for a member")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Borrow history retrieved"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<List<BorrowRecordResponse>> getMemberBorrowHistory(
            @Parameter(description = "Member ID") @PathVariable Long id) {
        return ResponseEntity.ok(borrowService.getMemberBorrowHistory(id));
    }
}