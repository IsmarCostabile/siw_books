package com.siw.it.siw_books.Repository;

import com.siw.it.siw_books.Model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    List<Author> findByNameContainingIgnoreCase(String name);
    
    List<Author> findBySurnameContainingIgnoreCase(String name);

    List<Author> findByNationality(String nationality);
    
    @Query("SELECT a FROM Author a WHERE a.birthDate BETWEEN :startDate AND :endDate")
    List<Author> findByBirthDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Author a WHERE a.deathDate IS NULL")
    List<Author> findLivingAuthors();
    
    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(@Param("id") Long id);
    
    @Query("SELECT a FROM Author a WHERE LOWER(CONCAT(a.name, ' ', a.surname)) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    List<Author> findByFullNameContaining(@Param("fullName") String fullName);
    
    boolean existsByNameAndSurnameAndBirthDate(String name, String surname, LocalDate birthDate);
}
