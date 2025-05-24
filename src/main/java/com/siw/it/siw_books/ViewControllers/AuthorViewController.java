package com.siw.it.siw_books.ViewControllers;

import com.siw.it.siw_books.Model.Author;
import com.siw.it.siw_books.Model.Book;
import com.siw.it.siw_books.Model.User;
import com.siw.it.siw_books.Service.AuthorService;
import com.siw.it.siw_books.Service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/{id}/associate-books")
    public String showAssociateBooksPage(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/authors/" + id;
        }
        
        Optional<Author> author = authorService.findById(id);
        if (!author.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Author not found.");
            return "redirect:/authors";
        }
        
        List<Book> allBooks = bookService.findAll();
        model.addAttribute("author", author.get());
        model.addAttribute("books", allBooks);
        model.addAttribute("loggedInUser", loggedInUser);
        return "authors/associate-books";
    }

    @PostMapping("/{id}/associate-books")
    public String associateBooks(@PathVariable Long id, 
                                @RequestParam(value = "selectedBooks", required = false) List<Long> selectedBookIds,
                                HttpSession session, 
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !loggedInUser.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/authors/" + id;
        }
        
        Optional<Author> authorOpt = authorService.findById(id);
        if (!authorOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Author not found.");
            return "redirect:/authors";
        }
        
        Author author = authorOpt.get();
        
        if (selectedBookIds != null && !selectedBookIds.isEmpty()) {
            try {
                // Get the selected books and associate them with the author
                for (Long bookId : selectedBookIds) {
                    Optional<Book> bookOpt = bookService.findByIdWithAuthors(bookId);
                    if (bookOpt.isPresent()) {
                        Book book = bookOpt.get();
                        // Add author to book if not already associated
                        if (!book.getAuthors().contains(author)) {
                            book.getAuthors().add(author);
                            bookService.save(book);
                        }
                    }
                }
                redirectAttributes.addFlashAttribute("successMessage", "Books associated successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error associating books. Please try again.");
            }
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "No books were selected.");
        }
        
        return "redirect:/authors/" + id;
    }
}
