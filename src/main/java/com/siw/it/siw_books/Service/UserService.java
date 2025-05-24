package com.siw.it.siw_books.Service;

import com.siw.it.siw_books.Model.User;
import com.siw.it.siw_books.Model.UserRole;
import com.siw.it.siw_books.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> findByNameContaining(String name) {
        return userRepository.findByNameContaining(name);
    }

    public Optional<User> findByIdWithReviews(Long id) {
        return userRepository.findByIdWithReviews(id);
    }
}
