package service;

import dao.LoanDAO;
import model.Loan;

import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class LoanService {

    private final LoanDAO loanDAO;

    public LoanService() {
        this.loanDAO = new LoanDAO();
    }

    public void registerLoan(int studentId, int bookId,
                             Consumer<Boolean> callback) {

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {
                return loanDAO.addLoan(studentId, bookId);
            }

            @Override
            protected void done() {
                try {
                    callback.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    callback.accept(false);
                } catch (ExecutionException e) {
                    System.out.println("Error al registrar prestamo: "
                            + e.getMessage());
                    callback.accept(false);
                }
            }
        };

        worker.execute();
    }

    public void returnBook(int loanId, Consumer<Boolean> callback) {

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() {
                return loanDAO.returnBook(loanId);
            }

            @Override
            protected void done() {
                try {
                    callback.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    callback.accept(false);
                } catch (ExecutionException e) {
                    System.out.println("Error al devolver libro: "
                            + e.getMessage());
                    callback.accept(false);
                }
            }
        };

        worker.execute();
    }

    public List<Loan> getAllLoans() {
        return loanDAO.getAllLoans();
    }

    public List<Loan> getActiveLoans() {
        return loanDAO.getActiveLoans();
    }

    public List<Loan> getLoansByStudent(int studentId) {
        return loanDAO.getLoansByStudent(studentId);
    }

    public List<String> getMostBorrowedBooks() {
        return loanDAO.getMostBorrowedBooks();
    }
}

