package com.siw.it.siw_books.Repository;

import com.siw.it.siw_books.Model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByBookId(Long bookId);
    
    List<Review> findByUserId(Long userId);
    
    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);
    
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    
    List<Review> findByRating(Integer rating);
    
    List<Review> findByBookIdOrderByRatingDesc(Long bookId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId")
    Long countReviewsByBookId(@Param("bookId") Long bookId);
    
    List<Review> findByTitleContaining(String title);
}
