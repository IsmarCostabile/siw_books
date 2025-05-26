package com.siw.it.siw_books.Service;

import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Model.Author;
import com.siw.it.siw_books.Repository.BookRepository;
import com.siw.it.siw_books.Repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;

    public List<Book> findAll() {
        return bookRepository.findAllWithoutImages();
    }
    
    public List<Book> findAllWithAuthors() {
        return bookRepository.findAllWithAuthors();
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
        return bookRepository.findByTitleContainingIgnoreCaseWithAuthors(title);
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

    public boolean hasImages(Long bookId) {
        return bookRepository.hasImages(bookId);
    }

    public long countImages(Long bookId) {
        return bookRepository.countImages(bookId);
    }

    @Transactional
    public Book addAuthor(Long bookId, Long authorId) {
        Optional<Book> bookOpt = bookRepository.findByIdWithAuthors(bookId);
        Optional<Author> authorOpt = authorRepository.findById(authorId);
        
        if (bookOpt.isPresent() && authorOpt.isPresent()) {
            Book book = bookOpt.get();
            Author author = authorOpt.get();
            
            if (!book.getAuthors().contains(author)) {
                book.getAuthors().add(author);
                author.getBooks().add(book);
                return bookRepository.save(book);
            }
        }
        return null;
    }

    @Transactional
    public Book addAuthors(Long bookId, List<Long> authorIds) {
        Optional<Book> bookOpt = bookRepository.findByIdWithAuthors(bookId);
        
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            
            for (Long authorId : authorIds) {
                Optional<Author> authorOpt = authorRepository.findById(authorId);
                if (authorOpt.isPresent()) {
                    Author author = authorOpt.get();
                    if (!book.getAuthors().contains(author)) {
                        book.getAuthors().add(author);
                        author.getBooks().add(book);
                    }
                }
            }
            return bookRepository.save(book);
        }
        return null;
    }

    @Transactional
    public Book setAuthors(Long bookId, List<Long> authorIds) {
        Optional<Book> bookOpt = bookRepository.findByIdWithAuthors(bookId);
        
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            
            // Remove existing relationships
            for (Author author : book.getAuthors()) {
                author.getBooks().remove(book);
            }
            book.getAuthors().clear();
            
            // Add new relationships
            for (Long authorId : authorIds) {
                Optional<Author> authorOpt = authorRepository.findById(authorId);
                if (authorOpt.isPresent()) {
                    Author author = authorOpt.get();
                    book.getAuthors().add(author);
                    author.getBooks().add(book);
                }
            }
            return bookRepository.save(book);
        }
        return null;
    }

    @Transactional
    public Book removeAuthor(Long bookId, Long authorId) {
        Optional<Book> bookOpt = bookRepository.findByIdWithAuthors(bookId);
        Optional<Author> authorOpt = authorRepository.findById(authorId);
        
        if (bookOpt.isPresent() && authorOpt.isPresent()) {
            Book book = bookOpt.get();
            Author author = authorOpt.get();
            
            book.getAuthors().remove(author);
            author.getBooks().remove(book);
            return bookRepository.save(book);
        }
        return null;
    }
}
