package port;

import model.Student;

import java.util.ArrayList;

public interface StudentRepository {

    ArrayList<Student> findAll();

    Student findById(String userId);

    void save(Student student);
}
