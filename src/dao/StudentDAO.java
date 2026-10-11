package dao;

import connection.DatabaseConnection;
import model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public boolean addStudent(Student student) {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getRut());
            stmt.setString(3, student.getCourse());
            stmt.setString(4, student.getEmail());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        student.setId(keys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error al registrar estudiante: " + e.getMessage());
        }

        return false;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY id";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                students.add(createStudent(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar estudiantes: " + e.getMessage());
        }

        return students;
    }

    public Student getStudentById(int id) {
        String sql = "SELECT * FROM estudiantes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createStudent(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar estudiante: " + e.getMessage());
        }

        return null;
    }

    public Student getStudentByRut(String rut) {
        String sql = "SELECT * FROM estudiantes WHERE rut = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rut);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createStudent(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar estudiante por RUT: " + e.getMessage());
        }

        return null;
    }

    public boolean updateStudent(Student student) {
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getRut());
            stmt.setString(3, student.getCourse());
            stmt.setString(4, student.getEmail());
            stmt.setInt(5, student.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar estudiante: " + e.getMessage());
        }

        return false;
    }

    public boolean deleteStudent(int id) {
        String sql = "DELETE FROM estudiantes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar estudiante: " + e.getMessage());
        }

        return false;
    }

    private Student createStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("rut"),
                rs.getString("curso"),
                rs.getString("correo")
        );
    }
}

