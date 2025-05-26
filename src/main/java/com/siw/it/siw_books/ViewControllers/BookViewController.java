package com.siw.it.siw_books.ViewControllers;

import com.siw.it.siw_books.Model.Author;
import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Model.Review;
import com.siw.it.siw_books.Model.User;
import com.siw.it.siw_books.Service.AuthorService;
import com.siw.it.siw_books.Service.BookService;
import com.siw.it.siw_books.Service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/books")
public class BookViewController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    @Transactional(readOnly = true)
    public String getAllBooks(Model model, HttpSession session) {
        List<Book> books = bookService.findAllWithAuthors();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        // Calculate average ratings for each book
        List<Double> averageRatings = new ArrayList<>();
        List<Long> reviewCounts = new ArrayList<>();
        for (Book book : books) {
            Double avgRating = reviewService.findAverageRatingByBookId(book.getId());
            Long reviewCount = reviewService.countReviewsByBookId(book.getId());
            averageRatings.add(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
            reviewCounts.add(reviewCount != null ? reviewCount : 0);
        }
        
        model.addAttribute("books", books);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("reviewCounts", reviewCounts);
        model.addAttribute("loggedInUser", loggedInUser);
        return "books/list";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String getBookDetails(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Book> book = bookService.findByIdWithAuthors(id);
        if (book.isPresent()) {
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            boolean hasImages = bookService.hasImages(id);
            long imageCount = bookService.countImages(id);
            
            // Load reviews for this book
            List<Review> reviews = reviewService.findByBookIdOrderByRatingDesc(id);
            
            // Check if logged-in user has already reviewed this book
            Review userReview = null;
            if (loggedInUser != null) {
                Optional<Review> userReviewOpt = reviewService.findByUserIdAndBookId(loggedInUser.getId(), id);
                if (userReviewOpt.isPresent()) {
                    userReview = userReviewOpt.get();
                }
            }
            
            // Calculate review statistics
            Double averageRating = reviewService.findAverageRatingByBookId(id);
            Long reviewCount = reviewService.countReviewsByBookId(id);
            
            model.addAttribute("book", book.get());
            model.addAttribute("hasImages", hasImages);
            model.addAttribute("imageCount", imageCount);
            model.addAttribute("loggedInUser", loggedInUser);
            model.addAttribute("reviews", reviews);
            model.addAttribute("userReview", userReview);
            model.addAttribute("averageRating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);
            model.addAttribute("reviewCount", reviewCount != null ? reviewCount : 0);
            
            return "books/detail";
        }
        return "redirect:/books";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/books";
        }
        
        model.addAttribute("book", new Book());
        return "books/form";
    }

    @GetMapping("/{id}/edit")
    @Transactional
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/books";
        }
        
        Optional<Book> bookOpt = bookService.findByIdWithAuthors(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            // Force initialization of images collection to avoid lazy loading issues in template
            if (book.getImages() != null) {
                book.getImages().size(); // This will trigger lazy loading within the transaction
            }
            model.addAttribute("book", book);
            return "books/form";
        }
        return "redirect:/books";
    }

    @PostMapping("/save")
    public String saveBook(@ModelAttribute Book book, 
                         @RequestParam("imageFiles") MultipartFile[] imageFiles,
                         HttpSession session, 
                         RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/books";
        }
        
        try {
            // Convert uploaded files to byte arrays
            List<byte[]> imageDataList = new ArrayList<>();
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    imageDataList.add(file.getBytes());
                }
            }
            
            // If new images are uploaded, replace existing ones
            if (!imageDataList.isEmpty()) {
                book.setImages(imageDataList);
            }
            
            bookService.save(book);
            redirectAttributes.addFlashAttribute("successMessage", "Book saved successfully!");
            return "redirect:/books";
        } catch (IOException e) {
            // Handle file upload error
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading images. Please try again.");
            return "redirect:/books/new?error=upload";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/books";
        }
        
        try {
            bookService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting book. Please try again.");
        }
        return "redirect:/books";
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public String searchBooks(@RequestParam(required = false) String title, Model model, HttpSession session) {
        List<Book> books;
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (title != null && !title.trim().isEmpty()) {
            books = bookService.findByTitleContaining(title);
            model.addAttribute("searchTitle", title);
        } else {
            books = bookService.findAllWithAuthors();
        }
        
        // Calculate average ratings for each book
        List<Double> averageRatings = new ArrayList<>();
        List<Long> reviewCounts = new ArrayList<>();
        for (Book book : books) {
            Double avgRating = reviewService.findAverageRatingByBookId(book.getId());
            Long reviewCount = reviewService.countReviewsByBookId(book.getId());
            averageRatings.add(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
            reviewCounts.add(reviewCount != null ? reviewCount : 0);
        }
        
        model.addAttribute("books", books);
        model.addAttribute("averageRatings", averageRatings);
        model.addAttribute("reviewCounts", reviewCounts);
        model.addAttribute("loggedInUser", loggedInUser);
        return "books/list";
    }

    @GetMapping("/{id}/manage-authors")
    @Transactional(readOnly = true)
    public String manageAuthors(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/books";
        }

        Optional<Book> bookOpt = bookService.findByIdWithAuthors(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            List<Author> allAuthors = authorService.findAll();
            List<Long> bookAuthorIds = book.getAuthors().stream()
                                           .map(Author::getId)
                                           .collect(Collectors.toList());
            model.addAttribute("book", book);
            model.addAttribute("allAuthors", allAuthors);
            model.addAttribute("bookAuthorIds", bookAuthorIds);
            return "books/manage-authors";
        }
        return "redirect:/books";
    }

    @PostMapping("/{id}/manage-authors")
    public String saveBookAuthors(@PathVariable Long id, 
                                 @RequestParam(value = "selectedAuthorIds", required = false) List<Long> selectedAuthorIds,
                                 HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/books";
        }

        try {
            Optional<Book> bookOpt = bookService.findById(id);
            if (!bookOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Book not found.");
                return "redirect:/books";
            }

            // If no authors selected, pass empty list
            if (selectedAuthorIds == null) {
                selectedAuthorIds = new ArrayList<>();
            }

            // Use the BookService to set authors for the book
            Book updatedBook = bookService.setAuthors(id, selectedAuthorIds);
            if (updatedBook != null) {
                redirectAttributes.addFlashAttribute("successMessage", "Authors updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update authors.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating authors: " + e.getMessage());
        }

        return "redirect:/books/" + id;
    }
}
