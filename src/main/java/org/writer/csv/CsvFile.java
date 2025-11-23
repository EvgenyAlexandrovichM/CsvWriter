package org.writer.csv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines global CSV configuration for a class.
 * <p>Allows setting the delimiter between columns.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CsvFile {

    String columnDelimiter() default ",";

}
