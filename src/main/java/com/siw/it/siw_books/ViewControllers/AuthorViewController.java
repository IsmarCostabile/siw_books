package com.siw.it.siw_books.ViewControllers;

import com.siw.it.siw_books.Model.Author;
import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Model.User;
import com.siw.it.siw_books.Service.AuthorService;
import com.siw.it.siw_books.Service.BookService;
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
@RequestMapping("/authors")
public class AuthorViewController {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private BookService bookService;

    @GetMapping
    public String getAllAuthors(Model model, HttpSession session) {
        List<Author> authors = authorService.findAll();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        model.addAttribute("authors", authors);
        model.addAttribute("loggedInUser", loggedInUser);
        return "authors/list";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String getAuthorDetails(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Author> author = authorService.findById(id);
        if (author.isPresent()) {
            User loggedInUser = (User) session.getAttribute("loggedInUser");
            model.addAttribute("author", author.get());
            model.addAttribute("loggedInUser", loggedInUser);
            return "authors/detail";
        }
        return "redirect:/authors";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/authors";
        }
        
        model.addAttribute("author", new Author());
        return "authors/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/authors";
        }
        
        Optional<Author> author = authorService.findById(id);
        if (author.isPresent()) {
            model.addAttribute("author", author.get());
            return "authors/form";
        }
        return "redirect:/authors";
    }

    @PostMapping("/save")
    public String saveAuthor(@ModelAttribute Author author, 
                           @RequestParam("photoFile") MultipartFile photoFile,
                           HttpSession session, 
                           RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/authors";
        }
        
        try {
            // Convert uploaded photo to byte array if provided
            if (!photoFile.isEmpty()) {
                author.setPhoto(photoFile.getBytes());
            }
            
            authorService.save(author);
            redirectAttributes.addFlashAttribute("successMessage", "Author saved successfully!");
            return "redirect:/authors";
        } catch (IOException e) {
            // Handle file upload error
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading photo. Please try again.");
            return "redirect:/authors/new?error=upload";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/authors";
        }
        
        try {
            authorService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Author deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting author. Please try again.");
        }
        return "redirect:/authors";
    }

    @GetMapping("/search")
    public String searchAuthors(@RequestParam(required = false) String name, Model model, HttpSession session) {
        List<Author> authors;
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (name != null && !name.trim().isEmpty()) {
            authors = authorService.findByNameContaining(name);
            model.addAttribute("searchName", name);
        } else {
            authors = authorService.findAll();
        }
        model.addAttribute("authors", authors);
        model.addAttribute("loggedInUser", loggedInUser);
        return "authors/list";
    }

    @GetMapping("/{id}/manage-books")
    @Transactional(readOnly = true)
    public String manageBooksForAuthor(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/authors";
        }
        
        Optional<Author> authorOpt = authorService.findByIdWithBooks(id);
        if (authorOpt.isPresent()) {
            Author author = authorOpt.get();
            List<Book> allBooks = bookService.findAll(); // Get all books
            List<Long> authorBookIds = author.getBooks().stream()
                                           .map(Book::getId)
                                           .collect(Collectors.toList());
            model.addAttribute("author", author);
            model.addAttribute("allBooks", allBooks); // Add all books to the model
            model.addAttribute("authorBookIds", authorBookIds); // Add IDs of books by this author
            return "authors/manage-books";
        }
        return "redirect:/authors";
    }

    @PostMapping("/{id}/manage-books")
    public String saveAuthorBooks(@PathVariable Long id, 
                                @RequestParam(value = "selectedBookIds", required = false) List<Long> selectedBookIds,
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/authors";
        }

        try {
            Optional<Author> authorOpt = authorService.findByIdWithBooks(id);
            if (!authorOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Author not found.");
                return "redirect:/authors";
            }

            // If no books selected, pass empty list
            if (selectedBookIds == null) {
                selectedBookIds = new ArrayList<>();
            }

            // Use the AuthorService to set books for the author
            Author updatedAuthor = authorService.setBooks(id, selectedBookIds);
            if (updatedAuthor != null) {
                redirectAttributes.addFlashAttribute("successMessage", "Books updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to update books.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating books: " + e.getMessage());
        }

        return "redirect:/authors/" + id;
    }
}
