package model;

public class Student {

    private int id;
    private String name;
    private String rut;
    private String course;
    private String email;

    public Student(int id, String name, String rut,
                   String course, String email) {
        this.id = id;
        this.name = name;
        this.rut = rut;
        this.course = course;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return name + " - " + rut;
    }
}

