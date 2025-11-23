package org.writer.csv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be included in CSV output with custom configuration.
 * <p>Allows overriding column name, order, and delimiter for collections.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {

    String name() default "";

    int order() default Integer.MAX_VALUE;

    String collectionDelimiter() default ";";
}
