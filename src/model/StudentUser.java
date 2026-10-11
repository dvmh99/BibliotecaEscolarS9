package model;

public class StudentUser extends User {

    public StudentUser(int id, String name, String rut,
                       String email, String password) {
        super(id, name, rut, email, password, "estudiante");
    }

    @Override
    public boolean canManageBooks() {
        return false;
    }
}

