package com.siw.it.siw_books.Service;

import com.siw.it.siw_books.Model.Author;
import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Repository.AuthorRepository;
import com.siw.it.siw_books.Repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    public Author save(Author author) {
        return authorRepository.save(author);
    }

    public void deleteById(Long id) {
        authorRepository.deleteById(id);
    }

    public List<Author> findByNameContaining(String name) {
        List<Author> authorsWithName = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            authorsWithName.addAll(authorRepository.findByNameContainingIgnoreCase(name));
            authorsWithName.addAll(authorRepository.findBySurnameContainingIgnoreCase(name));
        }
        return authorsWithName;
    }

    public List<Author> findByNationality(String nationality) {
        return authorRepository.findByNationality(nationality);
    }

    public List<Author> findByBirthDateBetween(LocalDate startDate, LocalDate endDate) {
        return authorRepository.findByBirthDateBetween(startDate, endDate);
    }

    public List<Author> findLivingAuthors() {
        return authorRepository.findLivingAuthors();
    }

    @Transactional(readOnly = true)
    public Optional<Author> findByIdWithBooks(Long id) {
        return authorRepository.findByIdWithBooks(id);
    }

    public List<Author> findByFullNameContaining(String fullName) {
        return authorRepository.findByFullNameContaining(fullName);
    }

    public boolean existsByNameAndSurnameAndBirthDate(String name, String surname, LocalDate birthDate) {
        return authorRepository.existsByNameAndSurnameAndBirthDate(name, surname, birthDate);
    }

    @Transactional
    public Author addBook(Long authorId, Long bookId) {
        Optional<Author> authorOpt = authorRepository.findByIdWithBooks(authorId);
        Optional<Book> bookOpt = bookRepository.findById(bookId);

        if (authorOpt.isPresent() && bookOpt.isPresent()) {
            Author author = authorOpt.get();
            Book book = bookOpt.get();

            if (!author.getBooks().contains(book)) {
                author.getBooks().add(book);
                book.getAuthors().add(author);
                return authorRepository.save(author);
            }
        }
        return null;
    }

    @Transactional
    public Author addBooks(Long authorId, List<Long> bookIds) {
        Optional<Author> authorOpt = authorRepository.findByIdWithBooks(authorId);

        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();

            for (Long bookId : bookIds) {
                Optional<Book> bookOpt = bookRepository.findById(bookId);
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    if (!author.getBooks().contains(book)) {
                        author.getBooks().add(book);
                        book.getAuthors().add(author);
                    }
                }
            }
            return authorRepository.save(author);
        }
        return null;
    }

    @Transactional
    public Author setBooks(Long authorId, List<Long> bookIds) {
        Optional<Author> authorOpt = authorRepository.findByIdWithBooks(authorId);

        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();

            // Remove existing relationships
            for (Book book : author.getBooks()) {
                book.getAuthors().remove(author);
            }
            author.getBooks().clear();

            // Add new relationships
            for (Long bookId : bookIds) {
                Optional<Book> bookOpt = bookRepository.findById(bookId);
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    author.getBooks().add(book);
                    book.getAuthors().add(author);
                }
            }
            return authorRepository.save(author);
        }
        return null;
    }

    @Transactional
    public Author removeBook(Long authorId, Long bookId) {
        Optional<Author> authorOpt = authorRepository.findByIdWithBooks(authorId);
        Optional<Book> bookOpt = bookRepository.findById(bookId);

        if (authorOpt.isPresent() && bookOpt.isPresent()) {
            Author author = authorOpt.get();
            Book book = bookOpt.get();

            author.getBooks().remove(book);
            book.getAuthors().remove(author);
            return authorRepository.save(author);
        }
        return null;
    }
}
