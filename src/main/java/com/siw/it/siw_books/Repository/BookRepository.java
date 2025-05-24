package com.siw.it.siw_books.Repository;

import com.siw.it.siw_books.Model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    @Query("SELECT b FROM Book b")
    List<Book> findAllWithoutImages();
    
    @Query("SELECT CASE WHEN COUNT(bi) > 0 THEN true ELSE false END FROM Book b JOIN b.images bi WHERE b.id = :bookId")
    boolean hasImages(@Param("bookId") Long bookId);
    
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    List<Book> findByPublicationYear(Integer year);
    
    @Query("SELECT b FROM Book b WHERE b.publicationYear BETWEEN :startYear AND :endYear")
    List<Book> findByPublicationYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);
    
    @Query("SELECT b FROM Book b JOIN FETCH b.authors WHERE b.id = :id")
    Optional<Book> findByIdWithAuthors(@Param("id") Long id);
    
    @Query("SELECT b FROM Book b JOIN FETCH b.reviews WHERE b.id = :id")
    Optional<Book> findByIdWithReviews(@Param("id") Long id);
    
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    List<Book> findByAuthorId(@Param("authorId") Long authorId);
    
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%')) OR LOWER(a.surname) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorNameContaining(@Param("authorName") String authorName);
}
