package view;

import controller.AuthController;
import controller.BookController;
import controller.LoanController;
import controller.StudentController;
import dao.CategoryDAO;
import dao.UserDAO;
import model.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainView extends JFrame {
    private final User user;
    private final BookController books = new BookController();
    private final StudentController students = new StudentController();
    private final LoanController loans = new LoanController();
    private final AuthController auth = new AuthController();
    private final CategoryDAO categories = new CategoryDAO();
    private final UserDAO users = new UserDAO();
    private final JTabbedPane tabs = new JTabbedPane();
    private final DefaultTableModel bookModel = table("ID", "Titulo", "Autor", "ISBN", "Editorial", "Stock", "Categoria");
    private final DefaultTableModel studentModel = table("ID", "Nombre", "RUT", "Curso", "Correo");
    private final DefaultTableModel loanModel = table("ID", "Estudiante", "Libro", "Prestamo", "Vencimiento", "Estado");
    private final DefaultTableModel accountModel = table("ID", "Nombre", "RUT", "Correo", "Rol");
    private final JTable bookTable = new JTable(bookModel);
    private final JTable studentTable = new JTable(studentModel);
    private final JTable loanTable = new JTable(loanModel);
    private final JTable accountTable = new JTable(accountModel);
    private final JTextArea reports = new JTextArea();
    private final boolean admin;

    public MainView(User user) {
        this.user = user;
        this.admin = user.canManageBooks();
        setTitle("Biblioteca Escolar - " + user.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 640);
        setMinimumSize(new Dimension(790, 490));
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel welcome = new JLabel("Biblioteca Escolar   |   " + user.getName() + " (" + user.getRole() + ")");
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 15f));
        header.add(welcome, BorderLayout.WEST);
        JButton logout = new JButton("Cerrar sesion");
        logout.addActionListener(e -> { dispose(); new LoginView().setVisible(true); });
        header.add(logout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        tabs.addTab("Libros", booksPanel());
        if (admin) {
            tabs.addTab("Estudiantes", studentsPanel());
            tabs.addTab("Usuarios", usersPanel());
        }
        tabs.addTab("Prestamos", loansPanel());
        if (admin) tabs.addTab("Reportes", reportsPanel());
        add(tabs);
        tabs.addChangeListener(e -> refreshSelected());
        refreshSelected();
    }

    private static DefaultTableModel table(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    private JPanel base(JTable table, JPanel actions) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private JButton button(JPanel panel, String text, Runnable action) {
        JButton b = new JButton(text);
        b.addActionListener(e -> action.run());
        panel.add(b);
        return b;
    }

    private JPanel booksPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (admin) {
            button(actions, "Agregar", () -> editBook(null));
            button(actions, "Editar", () -> {
                int id = selectedId(bookTable);
                if (id > 0) editBook(books.getBookById(id));
            });
            button(actions, "Eliminar", () -> {
                int id = selectedId(bookTable);
                if (id > 0 && confirm("Eliminar libro seleccionado?")) {
                    message(books.deleteBook(user, id) ? "Libro eliminado." : "No se pudo eliminar. Puede tener prestamos asociados.");
                    refreshBooks();
                }
            });
        }
        button(actions, "Actualizar", this::refreshBooks);
        return base(bookTable, actions);
    }

    private void editBook(Book book) {
        JTextField title = new JTextField(book == null ? "" : book.getTitle());
        JTextField author = new JTextField(book == null ? "" : book.getAuthor());
        JTextField isbn = new JTextField(book == null ? "" : book.getIsbn());
        JTextField publisher = new JTextField(book == null ? "" : book.getPublisher());
        JTextField stock = new JTextField(book == null ? "0" : String.valueOf(book.getStock()));
        List<Category> options = categories.getAllCategories();
        JComboBox<Category> category = new JComboBox<>(options.toArray(new Category[0]));
        if (book != null) for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getId() == book.getCategoryId()) category.setSelectedIndex(i);
        }
        if (form(book == null ? "Agregar libro" : "Editar libro",
                "Titulo", title, "Autor", author, "ISBN", isbn,
                "Editorial", publisher, "Stock", stock, "Categoria", category)) {
            try {
                Category selected = (Category) category.getSelectedItem();
                if (selected == null) { message("Primero debe existir una categoria."); return; }
                Book item = new Book(book == null ? 0 : book.getId(), title.getText().trim(),
                        author.getText().trim(), isbn.getText().trim(), publisher.getText().trim(),
                        Integer.parseInt(stock.getText().trim()), selected.getId());
                boolean ok = book == null ? books.addBook(user, item) : books.updateBook(user, item);
                message(ok ? "Libro guardado." : "No se pudo guardar. Revise los datos.");
                refreshBooks();
            } catch (NumberFormatException ex) { message("Stock debe ser un numero entero."); }
        }
    }

    private JPanel studentsPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        button(actions, "Agregar", () -> editStudent(null));
        button(actions, "Editar", () -> {
            int id = selectedId(studentTable);
            if (id > 0) editStudent(students.getStudentById(user, id));
        });
        button(actions, "Eliminar", () -> {
            int id = selectedId(studentTable);
            if (id > 0 && confirm("Eliminar estudiante seleccionado?")) {
                message(students.deleteStudent(user, id) ? "Estudiante eliminado." : "No se pudo eliminar. Puede tener prestamos.");
                refreshStudents();
            }
        });
        button(actions, "Actualizar", this::refreshStudents);
        return base(studentTable, actions);
    }

    private void editStudent(Student s) {
        JTextField name = new JTextField(s == null ? "" : s.getName());
        JTextField rut = new JTextField(s == null ? "" : s.getRut());
        JTextField course = new JTextField(s == null ? "" : s.getCourse());
        JTextField email = new JTextField(s == null ? "" : s.getEmail());
        if (form(s == null ? "Agregar estudiante" : "Editar estudiante", "Nombre", name,
                "RUT", rut, "Curso", course, "Correo", email)) {
            Student student = new Student(s == null ? 0 : s.getId(), name.getText().trim(),
                    rut.getText().trim(), course.getText().trim(), email.getText().trim());
            boolean ok = s == null ? students.addStudent(user, student) : students.updateStudent(user, student);
            message(ok ? "Estudiante guardado. Recuerde crearle una cuenta en Usuarios si necesita acceder." : "No se pudo guardar.");
            refreshStudents();
        }
    }

    private JPanel usersPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        button(actions, "Crear cuenta", () -> editUser(null));
        button(actions, "Editar", () -> {
            int id = selectedId(accountTable);
            if (id > 0) editUser(users.getUserById(id));
        });
        button(actions, "Eliminar", () -> {
            int id = selectedId(accountTable);
            if (id == user.getId()) { message("No puedes borrar tu propia cuenta en uso."); return; }
            if (id > 0 && confirm("Eliminar usuario?")) {
                message(users.deleteUser(id) ? "Usuario eliminado." : "No se pudo eliminar.");
                refreshUsers();
            }
        });
        button(actions, "Actualizar", this::refreshUsers);
        return base(accountTable, actions);
    }

    private void editUser(User u) {
        JTextField name = new JTextField(u == null ? "" : u.getName());
        JTextField rut = new JTextField(u == null ? "" : u.getRut());
        JTextField email = new JTextField(u == null ? "" : u.getEmail());
        JPasswordField password = new JPasswordField();
        JComboBox<String> role = new JComboBox<>(new String[]{"estudiante", "bibliotecario"});
        if (u != null) role.setSelectedItem(u.getRole());
        if (form(u == null ? "Crear cuenta" : "Editar cuenta (clave obligatoria)",
                "Nombre", name, "RUT", rut, "Correo", email, "Clave", password, "Rol", role)) {
            String secret = new String(password.getPassword());
            if (secret.isBlank()) { message("Ingrese una clave."); return; }
            User account = "bibliotecario".equals(role.getSelectedItem())
                    ? new LibrarianUser(u == null ? 0 : u.getId(), name.getText().trim(), rut.getText().trim(), email.getText().trim(), secret)
                    : new StudentUser(u == null ? 0 : u.getId(), name.getText().trim(), rut.getText().trim(), email.getText().trim(), secret);
            boolean ok = u == null ? users.addUser(account) : users.updateUser(account);
            message(ok ? "Cuenta guardada." : "No se pudo guardar la cuenta.");
            refreshUsers();
        }
    }

    private JPanel loansPanel() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        button(actions, "Solicitar prestamo", this::requestLoan);
        button(actions, "Devolver seleccionado", () -> {
            int id = selectedId(loanTable);
            if (id <= 0 || !confirm("Registrar la devolucion del prestamo " + id + "?")) return;
            setEnabled(false);
            loans.returnBook(user, id, ok -> {
                setEnabled(true);
                message(ok ? "Devolucion registrada." : "Libro ya devuelto.");
                refreshLoans();
                refreshBooks();
            });
        });
        button(actions, "Actualizar", this::refreshLoans);
        return base(loanTable, actions);
    }

    private void requestLoan() {
        List<Book> available = books.getAllBooks().stream().filter(b -> b.getStock() > 0).toList();
        if (available.isEmpty()) { message("No hay libros disponibles."); return; }
        JComboBox<String> bookChoice = new JComboBox<>();
        for (Book b : available) bookChoice.addItem(b.getTitle() + " (" + b.getStock() + " disponibles)");
        Student currentStudent = admin ? null : auth.getLoggedStudent(user);
        List<Student> people = admin ? students.getAllStudents(user) : currentStudent == null ? List.of() : List.of(currentStudent);
        if (people.isEmpty()) { message("No existe una ficha de estudiante para este usuario."); return; }
        JComboBox<Student> studentChoice = new JComboBox<>(people.toArray(new Student[0]));
        if (!form("Solicitar prestamo", "Libro", bookChoice, "Estudiante", studentChoice)) return;
        Student s = (Student) studentChoice.getSelectedItem();
        Book b = available.get(bookChoice.getSelectedIndex());
        if (s == null) return;
        setEnabled(false);
        loans.registerLoan(user, s.getId(), b.getId(), ok -> {
            setEnabled(true);
            message(ok ? "Prestamo registrado. Plazo: 7 dias." : "No se pudo prestar el libro. Revise stock y permisos.");
            refreshLoans();
            refreshBooks();
        });
    }

    private JPanel reportsPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        reports.setEditable(false);
        reports.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        p.add(new JScrollPane(reports), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));

        button(actions, "Libros mas prestados", () -> {
            reports.setText(String.join("\n", loans.getMostBorrowedBooks(user)));
        });

        button(actions, "Prestamos activos", () -> {
            StringBuilder sb = new StringBuilder();

            List<Book> bookList = books.getAllBooks();
            List<Student> studentList = students.getAllStudents(user);

            for (Loan loan : loans.getActiveLoans(user)) {
                String bookTitle = "No encontrado";
                String studentName = "No encontrado";

                for (Book book : bookList) {
                    if (book.getId() == loan.getBookId()) {
                        bookTitle = book.getTitle();
                        break;
                    }
                }

                for (Student student : studentList) {
                    if (student.getId() == loan.getStudentId()) {
                        studentName = student.getName();
                        break;
                    }
                }

                long daysLate = 0;

                if (loan.isOverdue()) {
                    daysLate = ChronoUnit.DAYS.between(
                            loan.getReturnDate(), LocalDate.now()
                    );
                }

                sb.append("Prestamo: ").append(loan.getId())
                        .append("\nEstudiante: ").append(studentName)
                        .append("\nLibro: ").append(bookTitle)
                        .append("\nFecha prestamo: ").append(loan.getLoanDate())
                        .append("\nVencimiento: ").append(loan.getReturnDate())
                        .append("\nEstado: ").append(loan.isOverdue() ? "Atrasado" : "Activo")
                        .append("\nDias de atraso: ").append(daysLate)
                        .append("\n-----------------------------\n");
            }

            reports.setText(sb.length() == 0
                    ? "No hay prestamos activos."
                    : sb.toString());
        });

        button(actions, "Historial por estudiante", () -> {
            List<Student> people = students.getAllStudents(user);

            if (people.isEmpty()) {
                message("Sin estudiantes.");
                return;
            }

            Student choice = (Student) JOptionPane.showInputDialog(
                    this, "Estudiante:", "Historial",
                    JOptionPane.QUESTION_MESSAGE, null,
                    people.toArray(), people.get(0)
            );

            if (choice == null) return;

            StringBuilder sb = new StringBuilder();
            List<Book> bookList = books.getAllBooks();

            for (Loan loan : loans.getLoans(user)) {
                if (loan.getStudentId() != choice.getId()) {
                    continue;
                }

                String bookTitle = "No encontrado";

                for (Book book : bookList) {
                    if (book.getId() == loan.getBookId()) {
                        bookTitle = book.getTitle();
                        break;
                    }
                }

                String status = loan.isReturned() ? "Devuelto"
                        : loan.isOverdue() ? "Atrasado" : "Activo";

                long daysLate = 0;

                if (loan.isOverdue()) {
                    daysLate = ChronoUnit.DAYS.between(
                            loan.getReturnDate(), LocalDate.now()
                    );
                }

                sb.append("Prestamo: ").append(loan.getId())
                        .append("\nLibro: ").append(bookTitle)
                        .append("\nFecha prestamo: ").append(loan.getLoanDate())
                        .append("\nVencimiento: ").append(loan.getReturnDate())
                        .append("\nEstado: ").append(status)
                        .append("\nDias de atraso: ")
                        .append(loan.isReturned() ? "No registrado" : daysLate)
                        .append("\n-----------------------------\n");
            }

            reports.setText(sb.length() == 0
                    ? "Este estudiante no tiene prestamos."
                    : sb.toString());
        });

        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private boolean form(String title, Object... items) {
        JPanel panel = new JPanel(new GridLayout(items.length / 2, 2, 9, 8));
        for (int i = 0; i < items.length; i += 2) {
            panel.add(new JLabel(String.valueOf(items[i])));
            panel.add((Component) items[i + 1]);
        }
        return JOptionPane.showConfirmDialog(this, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    private int selectedId(JTable t) {
        int viewRow = t.getSelectedRow();
        if (viewRow < 0) { message("Selecciona un registro primero."); return -1; }
        return Integer.parseInt(t.getModel().getValueAt(t.convertRowIndexToModel(viewRow), 0).toString());
    }

    private boolean confirm(String prompt) {
        return JOptionPane.showConfirmDialog(this, prompt, "Confirmacion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void message(String msg) { JOptionPane.showMessageDialog(this, msg); }

    private void refreshSelected() {
        String name = tabs.getTitleAt(tabs.getSelectedIndex());
        switch (name) {
            case "Libros" -> refreshBooks();
            case "Estudiantes" -> refreshStudents();
            case "Usuarios" -> refreshUsers();
            case "Prestamos" -> refreshLoans();
            default -> { }
        }
    }

    private void refreshBooks() {
        bookModel.setRowCount(0);

        List<Category> categories = books.getAllCategories();

        for (Book book : books.getAllBooks()) {
            String categoryName = "Sin categoría";

            for (Category category : categories) {
                if (category.getId() == book.getCategoryId()) {
                    categoryName = category.getName();
                    break;
                }
            }

            bookModel.addRow(new Object[]{
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getIsbn(),
                    book.getPublisher(),
                    book.getStock(),
                    categoryName
            });
        }
    }

    private void refreshStudents() {
        studentModel.setRowCount(0);
        for (Student s : students.getAllStudents(user)) studentModel.addRow(new Object[]{s.getId(), s.getName(), s.getRut(), s.getCourse(), s.getEmail()});
    }

    private void refreshUsers() {
        accountModel.setRowCount(0);
        for (User u : users.getAllUsers()) accountModel.addRow(new Object[]{u.getId(), u.getName(), u.getRut(), u.getEmail(), u.getRole()});
    }

    private void refreshLoans() {
        loanModel.setRowCount(0);

        List<Book> bookList = books.getAllBooks();
        List<Student> studentList;

        if (admin) {
            studentList = students.getAllStudents(user);
        } else {
            Student currentStudent = auth.getLoggedStudent(user);
            studentList = currentStudent == null ? List.of() : List.of(currentStudent);
        }

        for (Loan loan : loans.getLoans(user)) {
            String studentName = "Estudiante no encontrado";
            String bookTitle = "Libro no encontrado";

            for (Student student : studentList) {
                if (student.getId() == loan.getStudentId()) {
                    studentName = student.getName();
                    break;
                }
            }

            for (Book book : bookList) {
                if (book.getId() == loan.getBookId()) {
                    bookTitle = book.getTitle();
                    break;
                }
            }

            String status = loan.isReturned() ? "Devuelto"
                    : loan.isOverdue() ? "Atrasado" : "Activo";

            loanModel.addRow(new Object[]{
                    loan.getId(), studentName, bookTitle,
                    loan.getLoanDate(), loan.getReturnDate(), status
            });
        }
    }
}
