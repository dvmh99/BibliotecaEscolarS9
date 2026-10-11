package model;

import java.time.LocalDate;

public class Loan {

    private int id;
    private int studentId;
    private int bookId;
    private LocalDate loanDate;
    private LocalDate returnDate;
    private boolean returned;

    public Loan(int id, int studentId, int bookId,
                LocalDate loanDate, LocalDate returnDate,
                boolean returned) {
        this.id = id;
        this.studentId = studentId;
        this.bookId = bookId;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
        this.returned = returned;
    }

    public boolean isOverdue() {
        return !returned && returnDate != null
                && LocalDate.now().isAfter(returnDate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }
}

