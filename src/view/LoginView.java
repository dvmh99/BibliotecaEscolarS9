package view;

import controller.AuthController;
import model.User;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private final AuthController auth = new AuthController();
    private final JTextField email = new JTextField(22);
    private final JPasswordField password = new JPasswordField(22);
    private final JButton loginButton = new JButton("Ingresar");

    public LoginView() {
        setTitle("Biblioteca Escolar - Ingreso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel form = new JPanel(new GridLayout(4, 1, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(15, 28, 15, 28));
        JLabel title = new JLabel("Biblioteca Escolar", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 19f));
        form.add(title);
        form.add(labeled("Correo", email));
        form.add(labeled("Contraseña", password));
        form.add(loginButton);
        add(form);
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(e -> login());
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        JLabel text = new JLabel(label);
        text.setPreferredSize(new Dimension(85, 30));
        row.add(text, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private void login() {
        loginButton.setEnabled(false);
        String mail = email.getText().trim();
        String pass = new String(password.getPassword());
        new SwingWorker<User, Void>() {
            @Override protected User doInBackground() {
                return auth.login(mail, pass);
            }
            @Override protected void done() {
                loginButton.setEnabled(true);
                try {
                    User user = get();
                    if (user == null) {
                        JOptionPane.showMessageDialog(LoginView.this, "Credenciales incorrectas o conexion no disponible.");
                    } else {
                        dispose();
                        new MainView(user).setVisible(true);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginView.this, "No se pudo iniciar sesion: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
