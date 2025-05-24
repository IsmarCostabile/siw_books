package com.siw.it.siw_books.Service;

import com.siw.it.siw_books.Model.Review;
import com.siw.it.siw_books.Repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }

    public List<Review> findByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    public List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    public Optional<Review> findByUserIdAndBookId(Long userId, Long bookId) {
        return reviewRepository.findByUserIdAndBookId(userId, bookId);
    }

    public boolean existsByUserIdAndBookId(Long userId, Long bookId) {
        return reviewRepository.existsByUserIdAndBookId(userId, bookId);
    }

    public List<Review> findByRating(Integer rating) {
        return reviewRepository.findByRating(rating);
    }

    public List<Review> findByBookIdOrderByRatingDesc(Long bookId) {
        return reviewRepository.findByBookIdOrderByRatingDesc(bookId);
    }

    public Double findAverageRatingByBookId(Long bookId) {
        return reviewRepository.findAverageRatingByBookId(bookId);
    }

    public Long countReviewsByBookId(Long bookId) {
        return reviewRepository.countReviewsByBookId(bookId);
    }

    public List<Review> findByTitleContaining(String title) {
        return reviewRepository.findByTitleContaining(title);
    }
}
