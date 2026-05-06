package com.library.controller;

import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import com.library.dto.response.PageResponse;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Manage library books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    @Operation(summary = "Add a new book")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Author not found"),
            @ApiResponse(responseCode = "409", description = "ISBN already exists")
    })
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all books with pagination", description = "Optionally filter by availability")
    @ApiResponse(responseCode = "200", description = "Paginated list of books")
    public ResponseEntity<PageResponse<BookResponse>> findAll(
            @Parameter(description = "Filter by availability: true or false")
            @RequestParam(required = false) Boolean available,
            @Parameter(description = "Pagination: page, size, sort")
            Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(available, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookResponse> findById(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "409", description = "ISBN already exists")
    })
    public ResponseEntity<BookResponse> update(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Book ID") @PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}