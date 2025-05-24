package com.siw.it.siw_books.Controller;

import com.siw.it.siw_books.Model.Author;
import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Service.AuthorService;
import com.siw.it.siw_books.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ImageController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Transactional
    @GetMapping("/images/books/{bookId}/{imageIndex}")
    public ResponseEntity<byte[]> getBookImage(@PathVariable Long bookId, @PathVariable int imageIndex) {
        try {
            Book book = bookService.findById(bookId).orElse(null);
            if (book == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Safely check if images exist and get the specific image
            List<byte[]> images = book.getImages();
            if (images == null || imageIndex >= images.size()) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageData = images.get(imageIndex);
            if (imageData == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Default to JPEG, could be enhanced to detect type
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            // Handle any LOB access exceptions or other database issues
            return ResponseEntity.notFound().build();
        }
    }

    @Transactional
    @GetMapping("/images/authors/{authorId}")
    public ResponseEntity<byte[]> getAuthorPhoto(@PathVariable Long authorId) {
        Author author = authorService.findById(authorId).orElse(null);
        if (author == null || author.getPhoto() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Default to JPEG, could be enhanced to detect type
        return new ResponseEntity<>(author.getPhoto(), headers, HttpStatus.OK);
    }
}
