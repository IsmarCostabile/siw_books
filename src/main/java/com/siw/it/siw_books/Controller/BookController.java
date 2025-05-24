package com.siw.it.siw_books.Controller;

import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.findById(id);
        return book.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/with-authors")
    public ResponseEntity<Book> getBookByIdWithAuthors(@PathVariable Long id) {
        Optional<Book> book = bookService.findByIdWithAuthors(id);
        return book.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/with-reviews")
    public ResponseEntity<Book> getBookByIdWithReviews(@PathVariable Long id) {
        Optional<Book> book = bookService.findByIdWithReviews(id);
        return book.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooksByTitle(@RequestParam String title) {
        List<Book> books = bookService.findByTitleContaining(title);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<Book>> getBooksByYear(@PathVariable Integer year) {
        List<Book> books = bookService.findByPublicationYear(year);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/year-range")
    public ResponseEntity<List<Book>> getBooksByYearRange(
            @RequestParam Integer startYear, 
            @RequestParam Integer endYear) {
        List<Book> books = bookService.findByPublicationYearBetween(startYear, endYear);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable Long authorId) {
        List<Book> books = bookService.findByAuthorId(authorId);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/author/search")
    public ResponseEntity<List<Book>> searchBooksByAuthorName(@RequestParam String authorName) {
        List<Book> books = bookService.findByAuthorName(authorName);
        return ResponseEntity.ok(books);
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book savedBook = bookService.save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        if (!bookService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        book.setId(id);
        Book updatedBook = bookService.save(book);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (!bookService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
