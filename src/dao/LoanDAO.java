package dao;

import connection.DatabaseConnection;
import model.Loan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    public synchronized boolean addLoan(int studentId, int bookId) {
        String checkStock = "SELECT stock FROM libros WHERE id = ? FOR UPDATE";
        String insertLoan = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) VALUES (?, ?, ?, ?, FALSE)";
        String updateStock = "UPDATE libros SET stock = stock - 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                try (PreparedStatement stmt = conn.prepareStatement(checkStock)) {
                    stmt.setInt(1, bookId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next() || rs.getInt("stock") <= 0) {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(insertLoan)) {
                    stmt.setInt(1, studentId);
                    stmt.setInt(2, bookId);
                    stmt.setDate(3, Date.valueOf(LocalDate.now()));
                    stmt.setDate(4, Date.valueOf(LocalDate.now().plusDays(7)));
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(updateStock)) {
                    stmt.setInt(1, bookId);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("No se pudo actualizar el stock.");
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error al registrar prestamo: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Error de conexion en prestamo: " + e.getMessage());
        }

        return false;
    }

    public synchronized boolean returnBook(int loanId) {
        String findLoan = "SELECT id_libro, devuelto FROM prestamos WHERE id = ? FOR UPDATE";
        String updateLoan = "UPDATE prestamos SET devuelto = TRUE WHERE id = ?";
        String updateStock = "UPDATE libros SET stock = stock + 1 WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            conn.setAutoCommit(false);

            try {
                int bookId;

                try (PreparedStatement stmt = conn.prepareStatement(findLoan)) {
                    stmt.setInt(1, loanId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next() || rs.getBoolean("devuelto")) {
                            conn.rollback();
                            return false;
                        }

                        bookId = rs.getInt("id_libro");
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(updateLoan)) {
                    stmt.setInt(1, loanId);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("No se pudo registrar la devolucion.");
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(updateStock)) {
                    stmt.setInt(1, bookId);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("No se pudo devolver el stock.");
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error al devolver libro: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Error de conexion en devolucion: " + e.getMessage());
        }

        return false;
    }

    public List<Loan> getAllLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM prestamos ORDER BY id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                loans.add(createLoan(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar prestamos: " + e.getMessage());
        }

        return loans;
    }

    public List<Loan> getLoansByStudent(int studentId) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM prestamos WHERE id_estudiante = ? ORDER BY fecha_prestamo DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(createLoan(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar historial: " + e.getMessage());
        }

        return loans;
    }

    public List<Loan> getActiveLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM prestamos WHERE devuelto = FALSE ORDER BY fecha_devolucion";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                loans.add(createLoan(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar prestamos activos: " + e.getMessage());
        }

        return loans;
    }

    public List<String> getMostBorrowedBooks() {
        List<String> report = new ArrayList<>();

        String sql = """
                SELECT l.titulo, COUNT(p.id) AS total
                FROM libros l
                JOIN prestamos p ON l.id = p.id_libro
                GROUP BY l.id, l.titulo
                ORDER BY total DESC, l.titulo
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                report.add(
                        rs.getString("titulo") +
                                " - Prestamos: " +
                                rs.getInt("total")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error al generar reporte: " + e.getMessage());
        }

        return report;
    }

    private Loan createLoan(ResultSet rs) throws SQLException {
        Date loanDate = rs.getDate("fecha_prestamo");
        Date returnDate = rs.getDate("fecha_devolucion");

        return new Loan(
                rs.getInt("id"),
                rs.getInt("id_estudiante"),
                rs.getInt("id_libro"),
                loanDate != null ? loanDate.toLocalDate() : null,
                returnDate != null ? returnDate.toLocalDate() : null,
                rs.getBoolean("devuelto")
        );
    }
}

