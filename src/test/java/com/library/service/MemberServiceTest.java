package com.library.service;

import com.library.dto.request.MemberRequest;
import com.library.entity.Member;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member createMember() {
        Member member = new Member();
        member.setId(1L);
        member.setName("Dev");
        member.setEmail("dev@test.com");
        member.setPhone("9999999999");
        return member;
    }

    private MemberRequest createRequest() {
        MemberRequest request = new MemberRequest();
        request.setName("Dev");
        request.setEmail("dev@test.com");
        request.setPhone("9999999999");
        return request;
    }

    @Test
    void shouldCreateMemberSuccessfully() {
        MemberRequest request = createRequest();

        when(memberRepository.findByEmail("dev@test.com")).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

        var response = memberService.create(request);

        assertEquals("Dev", response.getName());
        assertEquals("dev@test.com", response.getEmail());

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists_onCreate() {
        MemberRequest request = createRequest();

        when(memberRepository.findByEmail("dev@test.com"))
                .thenReturn(Optional.of(new Member()));

        assertThrows(DuplicateResourceException.class,
                () -> memberService.create(request));

        verify(memberRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllMembers() {
        Member member = createMember();
        Page<Member> page = new PageImpl<>(List.of(member));
        Pageable pageable = PageRequest.of(0, 5);

        when(memberRepository.findAll(pageable)).thenReturn(page);

        var response = memberService.findAll(pageable);

        assertEquals(1, response.getContent().size());
        assertEquals("Dev", response.getContent().get(0).getName());
    }

    @Test
    void shouldReturnMemberById() {
        Member member = createMember();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        var response = memberService.findById(1L);

        assertEquals("Dev", response.getName());
    }

    @Test
    void shouldThrowException_whenMemberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> memberService.findById(1L));
    }

    @Test
    void shouldUpdateMemberSuccessfully() {
        Member existing = createMember();

        MemberRequest request = new MemberRequest();
        request.setName("Updated");
        request.setEmail("new@test.com");
        request.setPhone("8888888888");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(memberRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(memberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = memberService.update(1L, request);

        assertEquals("Updated", response.getName());
        assertEquals("new@test.com", response.getEmail());
        assertEquals("8888888888", response.getPhone());
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists_onUpdate() {
        Member existing = createMember();

        MemberRequest request = new MemberRequest();
        request.setName("Dev");
        request.setEmail("new@test.com");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(memberRepository.findByEmail("new@test.com"))
                .thenReturn(Optional.of(new Member()));

        assertThrows(DuplicateResourceException.class,
                () -> memberService.update(1L, request));
    }

    @Test
    void shouldUpdateMember_whenEmailUnchanged() {
        Member existing = createMember();

        MemberRequest request = new MemberRequest();
        request.setName("Updated");
        request.setEmail("dev@test.com");
        request.setPhone("7777777777");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(memberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = memberService.update(1L, request);

        assertEquals("Updated", response.getName());
        assertEquals("dev@test.com", response.getEmail());
    }

    @Test
    void shouldThrowException_whenGetMemberOrThrowNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> memberService.getMemberOrThrow(1L));
    }
}