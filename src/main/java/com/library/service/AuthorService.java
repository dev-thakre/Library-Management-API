package com.library.service;

import com.library.dto.request.AuthorRequest;
import com.library.dto.response.AuthorResponse;
import com.library.dto.response.PageResponse;
import com.library.entity.Author;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorResponse create(AuthorRequest request) {
        if (request.getEmail() != null &&
                authorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                    "Author with email " + request.getEmail() + " already exists");
        }

        Author author = Author.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();
        return toResponse(authorRepository.save(author));
    }

    public PageResponse<AuthorResponse> findAll(Pageable pageable) {
        Page<Author> page = authorRepository.findAll(pageable);
        return toPageResponse(page.map(this::toResponse));
    }

    public AuthorResponse findById(Long id) {
        return toResponse(getAuthorOrThrow(id));
    }

    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthorOrThrow(id);

        if (request.getEmail() != null &&
                !request.getEmail().equals(author.getEmail()) &&
                authorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException(
                    "Author with email " + request.getEmail() + " already exists");
        }

        author.setName(request.getName());
        author.setEmail(request.getEmail());
        return toResponse(authorRepository.save(author));
    }
// helper methods
    public Author getAuthorOrThrow(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Author not found with id: " + id));
    }

    private AuthorResponse toResponse(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .email(author.getEmail())
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