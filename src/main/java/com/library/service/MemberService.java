package com.library.service;

import com.library.dto.request.MemberRequest;
import com.library.dto.response.MemberResponse;
import com.library.dto.response.PageResponse;
import com.library.entity.Member;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse create(MemberRequest request) {
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                    "Member with email " + request.getEmail() + " already exists");
        }
        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        return toResponse(memberRepository.save(member));
    }

    public PageResponse<MemberResponse> findAll(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return toPageResponse(page.map(this::toResponse));
    }

    public MemberResponse findById(Long id) {
        return toResponse(getMemberOrThrow(id));
    }

    public MemberResponse update(Long id, MemberRequest request) {
        Member member = getMemberOrThrow(id);

        if (!request.getEmail().equals(member.getEmail()) &&
                memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                    "Member with email " + request.getEmail() + " already exists");
        }

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        return toResponse(memberRepository.save(member));
    }
// helper methods
    public Member getMemberOrThrow(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Member not found with id: " + id));
    }

    private MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .build();
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}