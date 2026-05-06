package com.library.controller;

import com.library.dto.request.MemberRequest;
import com.library.dto.response.MemberResponse;
import com.library.dto.response.PageResponse;
import com.library.service.MemberService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Manage library members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Register a new member")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    @GetMapping
    @Operation(summary = "Get all members with pagination")
    @ApiResponse(responseCode = "200", description = "Paginated list of members")
    public ResponseEntity<PageResponse<MemberResponse>> findAll(
            @Parameter(description = "Pagination: page, size, sort")
            Pageable pageable) {
        return ResponseEntity.ok(memberService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member found"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<MemberResponse> findById(
            @Parameter(description = "Member ID") @PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update member details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member updated"),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<MemberResponse> update(
            @Parameter(description = "Member ID") @PathVariable Long id,
            @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.update(id, request));
    }
}