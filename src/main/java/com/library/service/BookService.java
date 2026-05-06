package com.library.service;

import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import com.library.dto.response.PageResponse;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorService authorService;

    public BookResponse create(BookRequest request) {
        if (bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new DuplicateResourceException(
                    "Book with ISBN " + request.getIsbn() + " already exists");
        }
        Author author = authorService.getAuthorOrThrow(request.getAuthorId());
        Book book = Book.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .available(true)
                .author(author)
                .build();
        return toResponse(bookRepository.save(book));
    }

    public PageResponse<BookResponse> findAll(Boolean available, Pageable pageable) {
        Page<Book> page = (available != null)
                ? bookRepository.findByAvailable(available, pageable)
                : bookRepository.findAll(pageable);
        return toPageResponse(page.map(this::toResponse));
    }

    public BookResponse findById(Long id) {
        return toResponse(getBookOrThrow(id));
    }

    public BookResponse update(Long id, BookRequest request) {
        Book book = getBookOrThrow(id);

        if (!request.getIsbn().equals(book.getIsbn()) &&
                bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new DuplicateResourceException(
                    "Book with ISBN " + request.getIsbn() + " already exists");
        }

        Author author = authorService.getAuthorOrThrow(request.getAuthorId());
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setAuthor(author);
        return toResponse(bookRepository.save(book));
    }

    public void delete(Long id) {
        Book book = getBookOrThrow(id);
        bookRepository.delete(book);
    }

// helper methods
    public Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Book not found with id: " + id));
    }

    private BookResponse toResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .available(book.getAvailable())
                .authorId(book.getAuthor().getId())
                .authorName(book.getAuthor().getName())
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