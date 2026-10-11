package controller;

import dao.StudentDAO;
import dao.UserDAO;
import model.Student;
import model.User;

public class AuthController {

    private final UserDAO userDAO;
    private final StudentDAO studentDAO;

    public AuthController() {
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
    }

    public User login(String email, String password) {
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return null;
        }

        return userDAO.login(email.trim(), password);
    }

    public boolean isLibrarian(User user) {
        return user != null && user.canManageBooks();
    }

    public Student getLoggedStudent(User user) {
        if (user == null || isLibrarian(user)) {
            return null;
        }

        return studentDAO.getStudentByRut(user.getRut());
    }
}

