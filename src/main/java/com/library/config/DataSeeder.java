package com.library.config;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(AuthorRepository authorRepository,
                                      BookRepository bookRepository,
                                      MemberRepository memberRepository) {
        return args -> {
            if (authorRepository.count() > 0) {
                log.info("Database already seeded, skipping.");
                return;
            }

            // Authors
            Author martin = authorRepository.save(Author.builder()
                    .name("Robert Martin").email("martin@books.com").build());
            Author fowler = authorRepository.save(Author.builder()
                    .name("Martin Fowler").email("fowler@books.com").build());
            Author bloch = authorRepository.save(Author.builder()
                    .name("Joshua Bloch").email("bloch@books.com").build());

            // Books
            bookRepository.save(Book.builder()
                    .title("Clean Code").isbn("978-01").available(true).author(martin).build());
            bookRepository.save(Book.builder()
                    .title("Refactoring").isbn("978-02").available(true).author(fowler).build());
            bookRepository.save(Book.builder()
                    .title("Effective Java").isbn("978-03").available(true).author(bloch).build());

            // Members
            memberRepository.save(Member.builder()
                    .name("Alice").email("alice@library.com").phone("9999999999").build());
            memberRepository.save(Member.builder()
                    .name("Bob").email("bob@library.com").phone("8888888888").build());

            log.info("Database seeded with sample data.");
        };
    }
}