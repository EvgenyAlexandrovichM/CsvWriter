package org.writer;

import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.model.Student;
import org.writer.writer.Writable;
import org.writer.writer.csv.CsvWriter;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Writable csvWriter = new CsvWriter();

        List<Person> people = Arrays.asList(
                Person.builder()
                        .firstName("Иван")
                        .lastName("Иванов")
                        .dayOfBirth(12)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1997).build(),
                Person.builder()
                        .firstName("Мария")
                        .lastName("Петрова")
                        .dayOfBirth(5)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1997).build()
        );
        csvWriter.writeToFile(people, "persons.csv");

        List<Student> students = Arrays.asList(
                Student.builder()
                        .name("Алексей")
                        .score(Arrays.asList("5", "4", "3")).build(),
                Student.builder()
                        .name("Ольга")
                        .score(Arrays.asList("4", "4", "5")).build()
        );
        csvWriter.writeToFile(students, "students.csv");
    }
}

