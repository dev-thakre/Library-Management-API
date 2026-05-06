package com.library.service;

import com.library.dto.request.AuthorRequest;
import com.library.entity.Author;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.AuthorRepository;
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
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    private Author createAuthor() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Dev");
        author.setEmail("dev@test.com");
        return author;
    }

    private AuthorRequest createRequest() {
        AuthorRequest request = new AuthorRequest();
        request.setName("Dev");
        request.setEmail("dev@test.com");
        return request;
    }

    @Test
    void shouldCreateAuthorSuccessfully_withEmail() {
        AuthorRequest request = createRequest();

        when(authorRepository.findByEmail("dev@test.com")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(i -> i.getArgument(0));

        var response = authorService.create(request);

        assertEquals("Dev", response.getName());
        assertEquals("dev@test.com", response.getEmail());

        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void shouldCreateAuthorSuccessfully_whenEmailIsNull() {
        AuthorRequest request = new AuthorRequest();
        request.setName("Dev");
        request.setEmail(null);

        when(authorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = authorService.create(request);

        assertNull(response.getEmail());
        verify(authorRepository).save(any());
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists_onCreate() {
        AuthorRequest request = createRequest();

        when(authorRepository.findByEmail("dev@test.com"))
                .thenReturn(Optional.of(new Author()));

        assertThrows(DuplicateResourceException.class,
                () -> authorService.create(request));

        verify(authorRepository, never()).save(any());
    }

    @Test
    void shouldReturnAllAuthors() {
        Author author = createAuthor();
        Page<Author> page = new PageImpl<>(List.of(author));
        Pageable pageable = PageRequest.of(0, 5);

        when(authorRepository.findAll(pageable)).thenReturn(page);

        var response = authorService.findAll(pageable);

        assertEquals(1, response.getContent().size());
        assertEquals("Dev", response.getContent().get(0).getName());
    }

    @Test
    void shouldReturnAuthorById() {
        Author author = createAuthor();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        var response = authorService.findById(1L);

        assertEquals("Dev", response.getName());
    }

    @Test
    void shouldThrowException_whenAuthorNotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authorService.findById(1L));
    }

    @Test
    void shouldUpdateAuthorSuccessfully() {
        Author existing = createAuthor();

        AuthorRequest request = new AuthorRequest();
        request.setName("Updated");
        request.setEmail("new@test.com");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(authorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = authorService.update(1L, request);

        assertEquals("Updated", response.getName());
        assertEquals("new@test.com", response.getEmail());
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists_onUpdate() {
        Author existing = createAuthor();

        AuthorRequest request = new AuthorRequest();
        request.setName("Dev");
        request.setEmail("new@test.com");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.findByEmail("new@test.com"))
                .thenReturn(Optional.of(new Author()));

        assertThrows(DuplicateResourceException.class,
                () -> authorService.update(1L, request));
    }

    @Test
    void shouldUpdateAuthor_whenEmailUnchanged() {
        Author existing = createAuthor();

        AuthorRequest request = new AuthorRequest();
        request.setName("Updated");
        request.setEmail("dev@test.com");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = authorService.update(1L, request);

        assertEquals("Updated", response.getName());
        assertEquals("dev@test.com", response.getEmail());
    }

    @Test
    void shouldThrowException_whenGetAuthorOrThrowNotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authorService.getAuthorOrThrow(1L));
    }
}