package controller;

import dao.BookDAO;
import dao.CategoryDAO;
import model.Book;
import model.Category;
import model.User;

import java.util.List;

public class BookController {

    private final BookDAO bookDAO;
    private final CategoryDAO categoryDAO;

    public BookController() {
        bookDAO = new BookDAO();
        categoryDAO = new CategoryDAO();
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public Book getBookById(int id) {
        return bookDAO.getBookById(id);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public boolean addBook(User user, Book book) {
        if (!canManage(user) || !isValid(book)) {
            return false;
        }

        return bookDAO.addBook(book);
    }

    public boolean updateBook(User user, Book book) {
        if (!canManage(user) || !isValid(book)) {
            return false;
        }

        return bookDAO.updateBook(book);
    }

    public boolean deleteBook(User user, int id) {
        if (!canManage(user) || id <= 0) {
            return false;
        }

        return bookDAO.deleteBook(id);
    }

    private boolean canManage(User user) {
        return user != null && user.canManageBooks();
    }

    private boolean isValid(Book book) {
        return book != null
                && book.getTitle() != null
                && !book.getTitle().isBlank()
                && book.getAuthor() != null
                && !book.getAuthor().isBlank()
                && book.getIsbn() != null
                && !book.getIsbn().isBlank()
                && book.getStock() >= 0
                && book.getCategoryId() > 0;
    }
}

