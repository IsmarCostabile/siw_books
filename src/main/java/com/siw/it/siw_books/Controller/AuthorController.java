package com.siw.it.siw_books.Controller;

import com.siw.it.siw_books.Model.Author;
import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Service.AuthorService;
import com.siw.it.siw_books.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "*")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        List<Author> authors = authorService.findAll();
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        Optional<Author> author = authorService.findById(id);
        return author.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/with-books")
    public ResponseEntity<Author> getAuthorByIdWithBooks(@PathVariable Long id) {
        Optional<Author> author = authorService.findByIdWithBooks(id);
        return author.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Author>> searchAuthorsByName(@RequestParam String name) {
        List<Author> authors = authorService.findByNameContaining(name);
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/search/fullname")
    public ResponseEntity<List<Author>> searchAuthorsByFullName(@RequestParam String fullName) {
        List<Author> authors = authorService.findByFullNameContaining(fullName);
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/nationality/{nationality}")
    public ResponseEntity<List<Author>> getAuthorsByNationality(@PathVariable String nationality) {
        List<Author> authors = authorService.findByNationality(nationality);
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/birth-date-range")
    public ResponseEntity<List<Author>> getAuthorsByBirthDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Author> authors = authorService.findByBirthDateBetween(startDate, endDate);
        return ResponseEntity.ok(authors);
    }

    @GetMapping("/living")
    public ResponseEntity<List<Author>> getLivingAuthors() {
        List<Author> authors = authorService.findLivingAuthors();
        return ResponseEntity.ok(authors);
    }

    @PostMapping
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        if (authorService.existsByNameAndSurnameAndBirthDate(
                author.getName(), author.getSurname(), author.getBirthDate())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Author savedAuthor = authorService.save(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAuthor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Long id, @RequestBody Author author) {
        if (!authorService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        author.setId(id);
        Author updatedAuthor = authorService.save(author);
        return ResponseEntity.ok(updatedAuthor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        if (!authorService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        authorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkAuthorExists(
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate) {
        boolean exists = authorService.existsByNameAndSurnameAndBirthDate(name, surname, birthDate);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * Add a book to an author
     */
    @PostMapping("/{authorId}/books/{bookId}")
    public ResponseEntity<Author> addBookToAuthor(@PathVariable Long authorId, @PathVariable Long bookId) {
        if (!authorService.findById(authorId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        if (!bookService.findById(bookId).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        
        Author updatedAuthor = authorService.addBook(authorId, bookId);
        if (updatedAuthor != null) {
            return ResponseEntity.ok(updatedAuthor);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Book already associated
        }
    }
    
    /**
     * Remove a book from an author
     */
    @DeleteMapping("/{authorId}/books/{bookId}")
    public ResponseEntity<Author> removeBookFromAuthor(@PathVariable Long authorId, @PathVariable Long bookId) {
        if (!authorService.findById(authorId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Author updatedAuthor = authorService.removeBook(authorId, bookId);
        if (updatedAuthor != null) {
            return ResponseEntity.ok(updatedAuthor);
        } else {
            return ResponseEntity.notFound().build(); // Book not found in author
        }
    }
    
    /**
     * Set books for an author (replaces existing books)
     */
    @PutMapping("/{authorId}/books")
    public ResponseEntity<Author> setBooksForAuthor(@PathVariable Long authorId, @RequestBody List<Long> bookIds) {
        if (!authorService.findById(authorId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // Validate that all book IDs exist
        for (Long bookId : bookIds) {
            if (!bookService.findById(bookId).isPresent()) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        Author updatedAuthor = authorService.setBooks(authorId, bookIds);
        if (updatedAuthor != null) {
            return ResponseEntity.ok(updatedAuthor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Add multiple books to an author (adds to existing books)
     */
    @PostMapping("/{authorId}/books")
    public ResponseEntity<Author> addBooksToAuthor(@PathVariable Long authorId, @RequestBody List<Long> bookIds) {
        if (!authorService.findById(authorId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // Validate that all book IDs exist
        for (Long bookId : bookIds) {
            if (!bookService.findById(bookId).isPresent()) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        Author updatedAuthor = authorService.addBooks(authorId, bookIds);
        if (updatedAuthor != null) {
            return ResponseEntity.ok(updatedAuthor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all books of a specific author
     */
    @GetMapping("/{authorId}/books")
    public ResponseEntity<List<Book>> getAuthorBooks(@PathVariable Long authorId) {
        Optional<Author> author = authorService.findByIdWithBooks(authorId);
        if (author.isPresent()) {
            return ResponseEntity.ok(author.get().getBooks());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
