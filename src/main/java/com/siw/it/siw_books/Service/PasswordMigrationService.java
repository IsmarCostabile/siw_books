package com.siw.it.siw_books.Service;

import com.siw.it.siw_books.Model.Credentials;
import com.siw.it.siw_books.Repository.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service to migrate existing plain text passwords to encrypted passwords.
 * This should be run once when upgrading from plain text to encrypted passwords.
 * 
 * WARNING: This assumes all existing passwords are plain text and will encrypt them.
 * Run this only once during the migration!
 */
@Service
public class PasswordMigrationService implements CommandLineRunner {

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean migrationEnabled = true; // Set to true to enable migration

    @Override
    public void run(String... args) throws Exception {
        if (migrationEnabled) {
            migratePasswords();
        }
    }

    public void migratePasswords() {
        System.out.println("Starting password migration...");
        
        List<Credentials> allCredentials = credentialsRepository.findAll();
        int migratedCount = 0;
        
        for (Credentials credentials : allCredentials) {
            String currentPassword = credentials.getPassword();
            
            // Check if password is already encrypted (BCrypt hashes start with $2a$, $2b$, or $2y$)
            if (!currentPassword.startsWith("$2")) {
                System.out.println("Migrating password for user: " + credentials.getUsername());
                
                // Encrypt the plain text password
                String encryptedPassword = passwordEncoder.encode(currentPassword);
                credentials.setPassword(encryptedPassword);
                
                credentialsRepository.save(credentials);
                migratedCount++;
            }
        }
        
        System.out.println("Password migration completed. Migrated " + migratedCount + " passwords.");
    }

    // Method to manually trigger migration (for testing)
    public void enableMigrationAndRun() {
        migrationEnabled = true;
        migratePasswords();
        migrationEnabled = false;
    }
}
