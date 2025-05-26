package com.siw.it.siw_books.ViewControllers;

import com.siw.it.siw_books.Model.Credentials;
import com.siw.it.siw_books.Model.User;
import com.siw.it.siw_books.Model.UserRole;
import com.siw.it.siw_books.Service.CredentialsService;
import com.siw.it.siw_books.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AuthViewController {

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                             @RequestParam String password, 
                             HttpSession session, 
                             RedirectAttributes redirectAttributes) {
        try {
            Optional<Credentials> credentialsOpt = credentialsService.findByUsernameWithUser(username);
            
            if (credentialsOpt.isPresent()) {
                Credentials credentials = credentialsOpt.get();
                
                // Use password encoder to verify password
                if (credentialsService.verifyPassword(password, credentials.getPassword())) {
                    // Login successful - store user in session
                    session.setAttribute("loggedInUser", credentials.getUser());
                    session.setAttribute("loggedInCredentials", credentials);
                    
                    redirectAttributes.addFlashAttribute("successMessage", "Login successful! Welcome back.");
                    return "redirect:/";
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during login. Please try again.");
        }
        
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@RequestParam String firstName,
                                    @RequestParam String lastName,
                                    @RequestParam String email,
                                    @RequestParam String username,
                                    @RequestParam String password,
                                    @RequestParam String confirmPassword,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Validation
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
                return "redirect:/register";
            }

            if (credentialsService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Username already exists. Please choose a different one.");
                return "redirect:/register";
            }

            if (userService.findByEmail(email).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email already registered. Please use a different email.");
                return "redirect:/register";
            }

            // Create new user
            User newUser = new User(email, firstName, lastName, UserRole.USER);
            User savedUser = userService.save(newUser);

            // Create credentials with encrypted password
            Credentials newCredentials = new Credentials(username, password);
            newCredentials.setUser(savedUser);
            credentialsService.saveWithEncodedPassword(newCredentials);

            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during registration. Please try again.");
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/";
    }
}
