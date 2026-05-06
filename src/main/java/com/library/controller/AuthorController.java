package com.library.controller;

import com.library.dto.request.AuthorRequest;
import com.library.dto.response.AuthorResponse;
import com.library.dto.response.PageResponse;
import com.library.service.AuthorService;
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
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Manage library authors")
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    @Operation(summary = "Create a new author")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Author created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authorService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all authors with pagination")
    @ApiResponse(responseCode = "200", description = "Paginated list of authors")
    public ResponseEntity<PageResponse<AuthorResponse>> findAll(
            @Parameter(description = "Pagination: page, size, sort")
            Pageable pageable) {
        return ResponseEntity.ok(authorService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author found"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    public ResponseEntity<AuthorResponse> findById(
            @Parameter(description = "Author ID") @PathVariable Long id) {
        return ResponseEntity.ok(authorService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an author")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author updated"),
            @ApiResponse(responseCode = "404", description = "Author not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<AuthorResponse> update(
            @Parameter(description = "Author ID") @PathVariable Long id,
            @Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.ok(authorService.update(id, request));
    }
}