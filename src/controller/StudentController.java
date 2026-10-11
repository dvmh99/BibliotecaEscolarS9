package controller;

import dao.StudentDAO;
import model.Student;
import model.User;

import java.util.List;

public class StudentController {

    private final StudentDAO studentDAO;

    public StudentController() {
        studentDAO = new StudentDAO();
    }

    public List<Student> getAllStudents(User user) {
        if (!canManage(user)) {
            return List.of();
        }

        return studentDAO.getAllStudents();
    }

    public Student getStudentById(User user, int id) {
        if (!canManage(user)) {
            return null;
        }

        return studentDAO.getStudentById(id);
    }

    public boolean addStudent(User user, Student student) {
        if (!canManage(user) || !isValid(student)) {
            return false;
        }

        return studentDAO.addStudent(student);
    }

    public boolean updateStudent(User user, Student student) {
        if (!canManage(user) || !isValid(student)) {
            return false;
        }

        return studentDAO.updateStudent(student);
    }

    public boolean deleteStudent(User user, int id) {
        if (!canManage(user) || id <= 0) {
            return false;
        }

        return studentDAO.deleteStudent(id);
    }

    private boolean canManage(User user) {
        return user != null && user.canManageBooks();
    }

    private boolean isValid(Student student) {
        return student != null
                && student.getName() != null
                && !student.getName().isBlank()
                && student.getRut() != null
                && !student.getRut().isBlank()
                && student.getEmail() != null
                && !student.getEmail().isBlank()
                && student.getCourse() != null
                && !student.getCourse().isBlank();
    }
}

