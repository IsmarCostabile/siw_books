package com.siw.it.siw_books.Service;

import com.siw.it.siw_books.Model.Author;
import com.siw.it.siw_books.Repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    public Author save(Author author) {
        return authorRepository.save(author);
    }

    public void deleteById(Long id) {
        authorRepository.deleteById(id);
    }

    public List<Author> findByNameContaining(String name) {
        return authorRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(name, name);
    }

    public List<Author> findByNationality(String nationality) {
        return authorRepository.findByNationality(nationality);
    }

    public List<Author> findByBirthDateBetween(LocalDate startDate, LocalDate endDate) {
        return authorRepository.findByBirthDateBetween(startDate, endDate);
    }

    public List<Author> findLivingAuthors() {
        return authorRepository.findLivingAuthors();
    }

    public Optional<Author> findByIdWithBooks(Long id) {
        return authorRepository.findByIdWithBooks(id);
    }

    public List<Author> findByFullNameContaining(String fullName) {
        return authorRepository.findByFullNameContaining(fullName);
    }

    public boolean existsByNameAndSurnameAndBirthDate(String name, String surname, LocalDate birthDate) {
        return authorRepository.existsByNameAndSurnameAndBirthDate(name, surname, birthDate);
    }
}
