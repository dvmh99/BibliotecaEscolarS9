package model;

public class LibrarianUser extends User {

    public LibrarianUser(int id, String name, String rut,
                         String email, String password) {
        super(id, name, rut, email, password, "bibliotecario");
    }

    @Override
    public boolean canManageBooks() {
        return true;
    }
}

