package com.siw.it.siw_books.Service;

import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> findAll() {
        return bookRepository.findAllWithoutImages();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    public List<Book> findByTitleContaining(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> findByPublicationYear(Integer year) {
        return bookRepository.findByPublicationYear(year);
    }

    public List<Book> findByPublicationYearBetween(Integer startYear, Integer endYear) {
        return bookRepository.findByPublicationYearBetween(startYear, endYear);
    }

    public Optional<Book> findByIdWithAuthors(Long id) {
        return bookRepository.findByIdWithAuthors(id);
    }

    public Optional<Book> findByIdWithReviews(Long id) {
        return bookRepository.findByIdWithReviews(id);
    }

    public List<Book> findByAuthorId(Long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }

    public List<Book> findByAuthorName(String authorName) {
        return bookRepository.findByAuthorNameContaining(authorName);
    }
}
