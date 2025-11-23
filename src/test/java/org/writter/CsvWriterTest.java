package org.writter;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.writer.csv.CsvWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteValidData() throws IOException {
        CsvWriter writer = new CsvWriter();
        List<Person> people = List.of(
                Person.builder()
                        .firstName("Иван")
                        .lastName("Иванов")
                        .dayOfBirth(12)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1997)
                        .build(),
                Person.builder()
                        .firstName("Мария")
                        .lastName("Петрова")
                        .dayOfBirth(5)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1997)
                        .build()
        );

        Path file = tempDir.resolve("people.csv");
        writer.writeToFile(people, file.toString());

        List<String> lines = Files.readAllLines(file);

        assertEquals("firstName,lastName,dayOfBirth,monthOfBirth,yearOfBirth", lines.get(0));

        assertEquals("Иван,Иванов,12,JANUARY,1997", lines.get(1));
        assertEquals("Мария,Петрова,5,MARCH,1997", lines.get(2));
    }

    @Test
    void testWriteEmptyList() {
        CsvWriter writer = new CsvWriter();
        assertDoesNotThrow(() -> writer.writeToFile(List.of(), "empty.csv"));
    }

    @Test
    void testWriteNullList() {
        CsvWriter writer = new CsvWriter();
        assertDoesNotThrow(() -> writer.writeToFile(null, "null.csv"));
    }
}
