package com.siw.it.siw_books.Controller;

import com.siw.it.siw_books.Service.AuthorService;
import com.siw.it.siw_books.Service.BookService;
import com.siw.it.siw_books.Service.ReviewService;
import com.siw.it.siw_books.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/")
    public String home(Model model) {
        // Get actual counts for statistics
        int totalBooks = bookService.findAll().size();
        int totalAuthors = authorService.findAll().size();
        int totalUsers = userService.findAll().size();
        int totalReviews = reviewService.findAll().size();

        // Add statistics to the model
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalAuthors", totalAuthors);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalReviews", totalReviews);

        return "index";
    }

    @GetMapping("/home")
    public String homepage(Model model) {
        // Get actual counts for statistics
        int totalBooks = bookService.findAll().size();
        int totalAuthors = authorService.findAll().size();
        int totalUsers = userService.findAll().size();
        int totalReviews = reviewService.findAll().size();

        // Add statistics to the model
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalAuthors", totalAuthors);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalReviews", totalReviews);

        return "index";
    }
}
