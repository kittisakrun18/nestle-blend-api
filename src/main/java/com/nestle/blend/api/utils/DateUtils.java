package com.nestle.blend.api.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

    public static final String DEFAULT_OUT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DEFAULT_IN_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_OUT_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String DEFAULT_IN_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String localeDateToStr(LocalDate date) throws DateTimeParseException {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_OUT_DATE_FORMAT);
        String formattedDate = date.format(formatter);

        return formattedDate;
    }

    public static String localeDateToStr(LocalDate date, String format) throws DateTimeParseException {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String formattedDate = date.format(formatter);

        return formattedDate;
    }

    public static String localeDateTimeToStr(LocalDateTime date) throws DateTimeParseException {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_OUT_DATETIME_FORMAT);
        String formattedDate = date.format(formatter);

        return formattedDate;
    }

    public static String localeDateTimeToStr(LocalDateTime date, String format) throws DateTimeParseException {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String formattedDate = date.format(formatter);

        return formattedDate;
    }

    public static LocalDate strToLocalDate(String inputDate) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_IN_DATE_FORMAT);
        LocalDate date = LocalDate.parse(inputDate, formatter);

        return date;
    }

    public static LocalDate strToLocalDate(String inputDate, String format) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDate date = LocalDate.parse(inputDate, formatter);

        return date;
    }

    public static LocalDateTime strToLocalDateTime(String inputDate) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_IN_DATETIME_FORMAT);
        LocalDateTime date = LocalDateTime.parse(inputDate, formatter);

        return date;
    }

    public static LocalDateTime strToLocalDateTime(String inputDate, String format) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime date = LocalDateTime.parse(inputDate, formatter);

        return date;
    }

}
