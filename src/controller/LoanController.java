package controller;

import dao.StudentDAO;
import model.Loan;
import model.Student;
import model.User;
import service.LoanService;

import java.util.List;
import java.util.function.Consumer;

public class LoanController {

    private final LoanService loanService;
    private final StudentDAO studentDAO;

    public LoanController() {
        loanService = new LoanService();
        studentDAO = new StudentDAO();
    }

    public void registerLoan(User user, int studentId,
                             int bookId, Consumer<Boolean> callback) {

        if (user == null || studentId <= 0 || bookId <= 0
                || !canAccessStudent(user, studentId)) {
            callback.accept(false);
            return;
        }

        loanService.registerLoan(studentId, bookId, callback);
    }

    public void returnBook(User user, int loanId,
                           Consumer<Boolean> callback) {

        if (user == null || loanId <= 0) {
            callback.accept(false);
            return;
        }

        if (!user.canManageBooks()) {
            Student student = studentDAO.getStudentByRut(user.getRut());

            if (student == null) {
                callback.accept(false);
                return;
            }

            boolean ownsLoan = loanService.getLoansByStudent(student.getId())
                    .stream()
                    .anyMatch(loan -> loan.getId() == loanId);

            if (!ownsLoan) {
                callback.accept(false);
                return;
            }
        }

        loanService.returnBook(loanId, callback);
    }

    public List<Loan> getLoans(User user) {
        if (user == null) {
            return List.of();
        }

        if (user.canManageBooks()) {
            return loanService.getAllLoans();
        }

        Student student = studentDAO.getStudentByRut(user.getRut());

        if (student == null) {
            return List.of();
        }

        return loanService.getLoansByStudent(student.getId());
    }

    public List<Loan> getActiveLoans(User user) {
        return getLoans(user).stream()
                .filter(loan -> !loan.isReturned())
                .toList();
    }

    public List<String> getMostBorrowedBooks(User user) {
        if (user == null || !user.canManageBooks()) {
            return List.of();
        }

        return loanService.getMostBorrowedBooks();
    }

    private boolean canAccessStudent(User user, int studentId) {
        if (user.canManageBooks()) {
            return true;
        }

        Student student = studentDAO.getStudentByRut(user.getRut());

        return student != null && student.getId() == studentId;
    }
}

