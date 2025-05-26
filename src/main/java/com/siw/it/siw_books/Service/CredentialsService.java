package com.siw.it.siw_books.Service;

import com.siw.it.siw_books.Model.Credentials;
import com.siw.it.siw_books.Repository.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CredentialsService {

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Credentials> findAll() {
        return credentialsRepository.findAll();
    }

    public Optional<Credentials> findById(Long id) {
        return credentialsRepository.findById(id);
    }

    public Credentials save(Credentials credentials) {
        return credentialsRepository.save(credentials);
    }

    public void deleteById(Long id) {
        credentialsRepository.deleteById(id);
    }

    public Optional<Credentials> findByUsername(String username) {
        return credentialsRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return credentialsRepository.existsByUsername(username);
    }

    public Optional<Credentials> findByUsernameWithUser(String username) {
        return credentialsRepository.findByUsernameWithUser(username);
    }

    public List<Credentials> findByLastPasswordChangeBefore(LocalDateTime dateTime) {
        return credentialsRepository.findByLastPasswordChangeBefore(dateTime);
    }

    public Optional<Credentials> findByUserId(Long userId) {
        return credentialsRepository.findByUserId(userId);
    }

    public Credentials saveWithEncodedPassword(Credentials credentials) {
        // Encode the password before saving
        credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
        return credentialsRepository.save(credentials);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public Credentials updatePassword(Long credentialsId, String newPassword) {
        Optional<Credentials> credentialsOpt = findById(credentialsId);
        if (credentialsOpt.isPresent()) {
            Credentials credentials = credentialsOpt.get();
            credentials.setPassword(passwordEncoder.encode(newPassword));
            return save(credentials);
        }
        throw new RuntimeException("Credentials not found with id: " + credentialsId);
    }
}
