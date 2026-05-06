package com.library.service;

import com.library.dto.request.BookRequest;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
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
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorService authorService;

    @InjectMocks
    private BookService bookService;

    private Author createAuthor() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Author");
        return author;
    }

    private Book createBook(Author author) {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Java");
        book.setIsbn("123");
        book.setAvailable(true);
        book.setAuthor(author);
        return book;
    }

    private BookRequest createRequest() {
        BookRequest request = new BookRequest();
        request.setTitle("Java");
        request.setIsbn("123");
        request.setAuthorId(1L);
        return request;
    }

//    --------- Test cases for Create Method --------

    @Test
    void shouldCreateBookSuccessfully() {
        BookRequest request = createRequest();
        Author author = createAuthor();

        when(bookRepository.findByIsbn("123")).thenReturn(Optional.empty());
        when(authorService.getAuthorOrThrow(1L)).thenReturn(author);
        when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArgument(0));

        var response = bookService.create(request);

        assertEquals("Java", response.getTitle());
        assertEquals("123", response.getIsbn());
        assertEquals(1L, response.getAuthorId());

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void shouldThrowException_whenIsbnAlreadyExists_onCreate() {
        BookRequest request = createRequest();

        when(bookRepository.findByIsbn("123")).thenReturn(Optional.of(new Book()));

        assertThrows(DuplicateResourceException.class, () -> bookService.create(request));

        verify(bookRepository, never()).save(any());
    }

//    --------- Test cases for FindAll Method --------
    @Test
    void shouldReturnBooks_whenFilterApplied() {
        Author author = createAuthor();
        Book book = createBook(author);

        Page<Book> page = new PageImpl<>(List.of(book));
        Pageable pageable = PageRequest.of(0, 5);

        when(bookRepository.findByAvailable(true, pageable)).thenReturn(page);

        var response = bookService.findAll(true, pageable);

        assertEquals(1, response.getContent().size());
        assertEquals("Java", response.getContent().get(0).getTitle());
    }

    @Test
    void shouldReturnAllBooks_whenNoFilter() {
        Author author = createAuthor();
        Book book = createBook(author);

        Page<Book> page = new PageImpl<>(List.of(book));
        Pageable pageable = PageRequest.of(0, 5);

        when(bookRepository.findAll(pageable)).thenReturn(page);

        var response = bookService.findAll(null, pageable);

        assertEquals(1, response.getContent().size());
    }


    //    --------- Test cases for FindById Method --------
    @Test
    void shouldReturnBookById() {
        Author author = createAuthor();
        Book book = createBook(author);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        var response = bookService.findById(1L);

        assertEquals("Java", response.getTitle());
    }

    @Test
    void shouldThrowException_whenBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.findById(1L));
    }

    //    --------- Test cases for update Method --------
    @Test
    void shouldUpdateBookSuccessfully() {
        Author author = createAuthor();
        Book existing = createBook(author);

        BookRequest request = createRequest();
        request.setIsbn("456");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.findByIsbn("456")).thenReturn(Optional.empty());
        when(authorService.getAuthorOrThrow(1L)).thenReturn(author);
        when(bookRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = bookService.update(1L, request);

        assertEquals("456", response.getIsbn());
    }

    @Test
    void shouldThrowException_whenIsbnAlreadyExists_onUpdate() {
        Author author = createAuthor();
        Book existing = createBook(author);

        BookRequest request = createRequest();
        request.setIsbn("456");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.findByIsbn("456")).thenReturn(Optional.of(new Book()));

        assertThrows(DuplicateResourceException.class, () -> bookService.update(1L, request));
    }


    //    --------- Test cases for Delete Method --------
    @Test
    void shouldDeleteBookSuccessfully() {
        Author author = createAuthor();
        Book book = createBook(author);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        bookService.delete(1L);

        verify(bookRepository).delete(book);
    }

}