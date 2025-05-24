package com.siw.it.siw_books.Controller;

import com.siw.it.siw_books.Model.Review;
import com.siw.it.siw_books.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.findAll();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.findById(id);
        return review.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Review>> getReviewsByBook(@PathVariable Long bookId) {
        List<Review> reviews = reviewService.findByBookId(bookId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/book/{bookId}/sorted")
    public ResponseEntity<List<Review>> getReviewsByBookOrderByRating(@PathVariable Long bookId) {
        List<Review> reviews = reviewService.findByBookIdOrderByRatingDesc(bookId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId) {
        List<Review> reviews = reviewService.findByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}/book/{bookId}")
    public ResponseEntity<Review> getReviewByUserAndBook(
            @PathVariable Long userId, 
            @PathVariable Long bookId) {
        Optional<Review> review = reviewService.findByUserIdAndBookId(userId, bookId);
        return review.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<Review>> getReviewsByRating(@PathVariable Integer rating) {
        List<Review> reviews = reviewService.findByRating(rating);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Review>> searchReviewsByTitle(@RequestParam String title) {
        List<Review> reviews = reviewService.findByTitleContaining(title);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/book/{bookId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByBook(@PathVariable Long bookId) {
        Double averageRating = reviewService.findAverageRatingByBookId(bookId);
        return ResponseEntity.ok(averageRating != null ? averageRating : 0.0);
    }

    @GetMapping("/book/{bookId}/count")
    public ResponseEntity<Long> getReviewCountByBook(@PathVariable Long bookId) {
        Long count = reviewService.countReviewsByBookId(bookId);
        return ResponseEntity.ok(count);
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        // Check if user already has a review for this book
        if (reviewService.existsByUserIdAndBookId(review.getUser().getId(), review.getBook().getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Review savedReview = reviewService.save(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review review) {
        if (!reviewService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        review.setId(id);
        Review updatedReview = reviewService.save(review);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        if (!reviewService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/user/{userId}/book/{bookId}")
    public ResponseEntity<Boolean> checkReviewExists(
            @PathVariable Long userId, 
            @PathVariable Long bookId) {
        boolean exists = reviewService.existsByUserIdAndBookId(userId, bookId);
        return ResponseEntity.ok(exists);
    }
}
