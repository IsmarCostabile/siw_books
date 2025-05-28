package com.siw.it.siw_books.Repository;

import com.siw.it.siw_books.Model.User;
import com.siw.it.siw_books.Model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN u.credentials c WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name% OR c.username LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT u FROM User u JOIN FETCH u.reviews WHERE u.id = :id")
    Optional<User> findByIdWithReviews(@Param("id") Long id);
}
