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

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private BookService bookService;

    @GetMapping
    public String getAllReviews(Model model) {
        List<Review> reviews = reviewService.findAll();
        model.addAttribute("reviews", reviews);
        return "reviews/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam Long bookId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isRegisteredUser()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to write a review.");
            return "redirect:/books/" + bookId;
        }
        
        // Check if user already has a review for this book
        if (reviewService.existsByUserIdAndBookId(loggedInUser.getId(), bookId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You have already reviewed this book.");
            return "redirect:/books/" + bookId;
        }
        
        Optional<Book> book = bookService.findById(bookId);
        if (!book.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
            return "redirect:/books";
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
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to edit reviews.");
            return "redirect:/login";
        }
        
        Optional<Review> reviewOpt = reviewService.findById(id);
        if (!reviewOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Review not found.");
            return "redirect:/reviews";
        }
        
        Review review = reviewOpt.get();
        
        // Check if user owns the review or is admin
        if (!review.getUser().getId().equals(loggedInUser.getId()) && !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own reviews.");
            return "redirect:/books/" + review.getBook().getId();
        }
        
        model.addAttribute("review", review);
        model.addAttribute("book", review.getBook());
        return "reviews/form";
    }

    @PostMapping("/save")
    public String saveReview(@ModelAttribute Review review, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to save reviews.");
            return "redirect:/login";
        }
        
        // For new reviews, ensure user owns the review
        if (review.getId() == null) {
            review.setUser(loggedInUser);
        } else {
            // For existing reviews, check ownership
            Optional<Review> existingReview = reviewService.findById(review.getId());
            if (existingReview.isPresent()) {
                Review existing = existingReview.get();
                if (!existing.getUser().getId().equals(loggedInUser.getId()) && !loggedInUser.isAdmin()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own reviews.");
                    return "redirect:/books/" + review.getBook().getId();
                }
            }
        }
        
        try {
            reviewService.save(review);
            redirectAttributes.addFlashAttribute("successMessage", "Review saved successfully!");
            return "redirect:/books/" + review.getBook().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving review. Please try again.");
            return "redirect:/reviews/new?bookId=" + review.getBook().getId();
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to delete reviews.");
            return "redirect:/login";
        }
        
        Optional<Review> reviewOpt = reviewService.findById(id);
        if (!reviewOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Review not found.");
            return "redirect:/reviews";
        }
        
        Review review = reviewOpt.get();
        
        // Check if user owns the review or is admin
        if (!review.getUser().getId().equals(loggedInUser.getId()) && !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only delete your own reviews.");
            return "redirect:/books/" + review.getBook().getId();
        }
        
        Long bookId = review.getBook().getId();
        try {
            reviewService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting review. Please try again.");
        }
        
        return "redirect:/books/" + bookId;
    }
}
