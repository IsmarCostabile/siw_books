package com.siw.it.siw_books.ViewControllers;

import com.siw.it.siw_books.Model.Review;
import com.siw.it.siw_books.Model.User;
import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Service.ReviewService;
import com.siw.it.siw_books.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewViewController {

    // Constants for common strings
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String REDIRECT_BOOKS = "redirect:/books";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String LOGIN_PATH = "/login";
    private static final String REVIEW_NOT_FOUND_MSG = "Review not found.";
    private static final String LOGIN_REQUIRED_MSG = "Please log in to continue.";

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private BookService bookService;

    /**
     * Helper method to get logged-in user from session
     */
    private Optional<User> getLoggedInUser(HttpSession session) {
        Object userObj = session.getAttribute("loggedInUser");
        return userObj instanceof User user ? Optional.of(user) : Optional.empty();
    }

    /**
     * Helper method to validate user authentication
     */
    private String checkAuthentication(HttpSession session, RedirectAttributes redirectAttributes, String redirectPath) {
        if (getLoggedInUser(session).isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, LOGIN_REQUIRED_MSG);
            return "redirect:" + redirectPath;
        }
        return null;
    }

    /**
     * Helper method to check if user can modify review
     */
    private boolean canUserModifyReview(User user, Review review) {
        if (user == null || review == null || review.getUser() == null) {
            return false;
        }
        return user.getId().equals(review.getUser().getId()) || user.isAdmin();
    }

    /**
     * Helper method to safely redirect to book page
     */
    private String redirectToBook(Long bookId) {
        // Validate bookId to prevent injection
        if (bookId == null || bookId <= 0) {
            return REDIRECT_BOOKS;
        }
        return "redirect:/books/" + bookId;
    }

    @GetMapping
    public String getAllReviews(Model model) {
        List<Review> reviews = reviewService.findAll();
        model.addAttribute("reviews", reviews);
        return "reviews/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam Long bookId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Validate bookId first
        if (bookId == null || bookId <= 0) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Invalid book ID.");
            return REDIRECT_BOOKS;
        }

        Optional<User> userOpt = getLoggedInUser(session);
        if (userOpt.isEmpty() || !userOpt.get().isRegisteredUser()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Please log in to write a review.");
            return redirectToBook(bookId);
        }
        
        User loggedInUser = userOpt.get();
        
        // Check if user already has a review for this book
        if (reviewService.existsByUserIdAndBookId(loggedInUser.getId(), bookId)) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "You have already reviewed this book.");
            return redirectToBook(bookId);
        }
        
        Optional<Book> book = bookService.findById(bookId);
        if (book.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Book not found.");
            return REDIRECT_BOOKS;
        }
        
        Review review = new Review();
        review.setBook(book.get());
        review.setUser(loggedInUser);
        
        model.addAttribute("review", review);
        model.addAttribute("book", book.get());
        return "reviews/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String authCheck = checkAuthentication(session, redirectAttributes, LOGIN_PATH);
        if (authCheck != null) return authCheck;
        
        Optional<User> userOpt = getLoggedInUser(session);
        if (userOpt.isEmpty()) {
            return REDIRECT_LOGIN; // This shouldn't happen due to authCheck above
        }
        User loggedInUser = userOpt.get();
        
        Optional<Review> reviewOpt = reviewService.findById(id);
        if (reviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, REVIEW_NOT_FOUND_MSG);
            return "redirect:/reviews";
        }
        
        Review review = reviewOpt.get();
        
        // Check if user can modify the review
        if (!canUserModifyReview(loggedInUser, review)) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "You can only edit your own reviews.");
            return redirectToBook(review.getBook().getId());
        }
        
        model.addAttribute("review", review);
        model.addAttribute("book", review.getBook());
        return "reviews/form";
    }

    @PostMapping("/save")
    public String saveReview(@ModelAttribute Review review, HttpSession session, RedirectAttributes redirectAttributes) {
        String authCheck = checkAuthentication(session, redirectAttributes, LOGIN_PATH);
        if (authCheck != null) return authCheck;
        
        Optional<User> userOpt = getLoggedInUser(session);
        if (userOpt.isEmpty()) {
            return REDIRECT_LOGIN; // This shouldn't happen due to authCheck above
        }
        User loggedInUser = userOpt.get();
        
        // Validate review data
        if (review.getBook() == null || review.getBook().getId() == null) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Invalid review data.");
            return REDIRECT_BOOKS;
        }
        
        String validationResult = validateReviewOwnership(review, loggedInUser, redirectAttributes);
        if (validationResult != null) return validationResult;
        
        try {
            reviewService.save(review);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Review saved successfully!");
            return redirectToBook(review.getBook().getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error saving review. Please try again.");
            return "redirect:/reviews/new?bookId=" + review.getBook().getId();
        }
    }

    /**
     * Helper method to validate review ownership
     */
    private String validateReviewOwnership(Review review, User loggedInUser, RedirectAttributes redirectAttributes) {
        if (review.getId() == null) {
            // New review - set the user
            review.setUser(loggedInUser);
        } else {
            // Existing review - check ownership
            Optional<Review> existingReview = reviewService.findById(review.getId());
            if (existingReview.isPresent()) {
                Review existing = existingReview.get();
                if (!canUserModifyReview(loggedInUser, existing)) {
                    redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "You can only edit your own reviews.");
                    return redirectToBook(review.getBook().getId());
                }
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, REVIEW_NOT_FOUND_MSG);
                return redirectToBook(review.getBook().getId());
            }
        }
        return null;
    }

    @GetMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        String authCheck = checkAuthentication(session, redirectAttributes, LOGIN_PATH);
        if (authCheck != null) return authCheck;
        
        Optional<User> userOpt = getLoggedInUser(session);
        if (userOpt.isEmpty()) {
            return REDIRECT_LOGIN; // This shouldn't happen due to authCheck above
        }
        User loggedInUser = userOpt.get();
        
        Optional<Review> reviewOpt = reviewService.findById(id);
        if (reviewOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, REVIEW_NOT_FOUND_MSG);
            return "redirect:/reviews";
        }
        
        Review review = reviewOpt.get();
        
        // Check if user can delete the review
        if (!canUserModifyReview(loggedInUser, review)) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "You can only delete your own reviews.");
            return redirectToBook(review.getBook().getId());
        }
        
        Long bookId = review.getBook().getId();
        try {
            reviewService.deleteById(id);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Review deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error deleting review. Please try again.");
        }
        
        return redirectToBook(bookId);
    }
}
