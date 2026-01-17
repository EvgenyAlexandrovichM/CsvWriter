package org.writer.writer.csv;

import org.writer.csv.CsvColumn;
import org.writer.csv.CsvFile;
import org.writer.csv.CsvIgnore;
import org.writer.exception.CsvFieldAccessException;
import org.writer.exception.CsvWriteException;
import org.writer.writer.Writable;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Utility class for writing annotated Java objects into CSV files.
 * <p>Supports field-level configuration via {@link CsvColumn} and class-level
 * configuration via {@link CsvFile}. Fields annotated with {@link CsvIgnore}
 * are excluded from output.</p>
 *
 * <p>Collections and maps are serialized using custom delimiters. Primitive
 * types and enums are converted via {@link Object#toString()}.</p>
 *
 * <p>Throws {@link org.writer.exception.CsvWriteException} if writing fails,
 * and {@link org.writer.exception.CsvFieldAccessException} if field access
 * is not possible.</p>
 */
public class CsvWriter implements Writable {

    private static final Logger logger = Logger.getLogger(CsvWriter.class.getName());

    /**
     * Writes a list of objects into a CSV file.
     * <p>
     * The header row is generated from field names or {@link CsvColumn#name()}.
     * Each object is serialized into a row according to field order.
     *
     * @param data     list of objects to serialize
     * @param fileName target file path
     * @throws CsvWriteException if writing fails
     */
    @Override
    public void writeToFile(List<?> data, String fileName) {
        if (!validateInput(data)) return;

        Class<?> clazz = data.get(0).getClass();
        List<Field> fields = prepareFields(clazz);

        CsvFile fileAnnotation = clazz.getAnnotation(CsvFile.class);
        String columnDelimiter = (fileAnnotation != null) ? fileAnnotation.columnDelimiter() : getDefaultColumnDelimiter();

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.append(buildHeader(fields, columnDelimiter)).append('\n');

            for (Object obj : data) {
                writer.append(buildRow(obj, fields, columnDelimiter)).append('\n');
            }
            logger.info("CSV file created successfully: " + fileName);

        } catch (IOException e) {
            logger.severe("Error writing CSV file: " + e.getMessage());
            throw new CsvWriteException("Error writing CSV file: " + fileName, e);
        }
    }

    /**
     * Validates input list before serialization.
     *
     * @param data list of objects
     * @return true if list is non-null and non-empty
     */
    private boolean validateInput(List<?> data) {
        if (data == null || data.isEmpty()) {
            logger.warning("No data to write");
            return false;
        }
        return true;
    }

    /**
     * Collects fields of the given class for serialization.
     * <p>
     * Excludes fields annotated with {@link CsvIgnore} and sorts them
     * according to {@link CsvColumn#order()}.
     *
     * @param clazz object class
     * @return sorted list of accessible fields
     */
    private List<Field> prepareFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.getAnnotation(CsvIgnore.class) == null)
                .peek(f -> f.setAccessible(true))
                .sorted(Comparator.comparingInt(f -> {
                    CsvColumn column = f.getAnnotation(CsvColumn.class);
                    return column != null ? column.order() : Integer.MAX_VALUE;
                }))
                .collect(Collectors.toList());
    }

    /**
     * Builds the CSV header row.
     *
     * @param fields          fields to include
     * @param columnDelimiter delimiter between columns
     * @return header row string
     */
    private String buildHeader(List<Field> fields, String columnDelimiter) {
        return fields.stream()
                .map(this::getColumnName)
                .collect(Collectors.joining(columnDelimiter));
    }

    /**
     * Builds a CSV row for a single object.
     *
     * @param obj             object to serialize
     * @param fields          fields to include
     * @param columnDelimiter delimiter between columns
     * @return row string
     */
    private String buildRow(Object obj, List<Field> fields, String columnDelimiter) {
        return fields.stream()
                .map(f -> getFieldValueAsString(obj, f))
                .collect(Collectors.joining(columnDelimiter));
    }

    /**
     * Resolves column name for a field.
     * <p>
     * Uses {@link CsvColumn#name()} if present, otherwise defaults to field name.
     *
     * @param field field to resolve
     * @return column name
     */
    private String getColumnName(Field field) {
        CsvColumn column = field.getAnnotation(CsvColumn.class);
        return (column != null && !column.name().isEmpty()) ? column.name() : field.getName();
    }

    /**
     * Retrieves string value of a field from an object.
     * <p>
     * Supports collections and maps with custom delimiters.
     *
     * @param obj   object containing the field
     * @param field field to access
     * @return string representation of field value
     * @throws CsvFieldAccessException if field access fails
     */
    private String getFieldValueAsString(Object obj, Field field) {
        try {
            Object value = field.get(obj);
            if (value instanceof Collection) {
                CsvColumn column = field.getAnnotation(CsvColumn.class);
                String delimiter = column != null ? column.collectionDelimiter() : getDefaultCollectionDelimiter();
                return serialize((Collection<?>) value, delimiter);
            }

            if (value instanceof Map) {
                CsvColumn column = field.getAnnotation(CsvColumn.class);
                String delimiter = column != null ? column.collectionDelimiter() : getDefaultCollectionDelimiter();
                return serialize((Map<?, ?>) value, delimiter);
            }
            return value != null ? value.toString() : "";
        } catch (IllegalAccessException e) {
            logger.severe("Error access to field: " + field.getName());
            throw new CsvFieldAccessException(field.getName(), e);
        }
    }

    /**
     * Returns default collection delimiter from {@link CsvColumn}.
     */
    private static String getDefaultCollectionDelimiter() {
        return (String) Arrays.stream(CsvColumn.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("collectionDelimiter"))
                .findFirst().get() // мы уверены, что метод существует, но, возможно, лучше воспользоваться orElseThrow() для самодостаточности метода
                .getDefaultValue();
    }

    /**
     * Returns default column delimiter from {@link CsvFile}.
     */
    private static String getDefaultColumnDelimiter() {
        return (String) Arrays.stream(CsvFile.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("columnDelimiter"))
                .findFirst().get() // мы уверены, что метод существует, но, возможно, лучше воспользоваться orElseThrow() для самодостаточности метода
                .getDefaultValue();
    }

    /**
     * Converts a {@link Collection} into a string joined by the given delimiter.
     * Null elements are represented as empty strings.
     */
    private String serialize(Collection<?> collection, String delimiter) {
        return collection.stream()
                .map(e -> e == null ? "" : e.toString())
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Converts a {@link Map} into a string of key=value pairs joined by the given delimiter.
     * Null keys or values are represented as empty strings.
     */
    private String serialize(Map<?, ?> map, String delimiter) {
        return map.entrySet().stream()
                .map(e -> new StringBuilder().append(e.getKey() == null
                        ? ""
                        : e.getKey()).append("=").append(e.getValue() == null ? "" : e.getValue()).toString())
                .collect(Collectors.joining(delimiter));
    }
}
