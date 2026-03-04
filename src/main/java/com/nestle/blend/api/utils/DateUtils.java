package com.nestle.blend.api.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    private static final Locale TH_TH = new Locale("th", "TH");

    private static final DateTimeFormatter THAI_DATE =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", TH_TH)
                    .withChronology(ThaiBuddhistChronology.INSTANCE);

    private static final DateTimeFormatter TIME_HH_MM =
            DateTimeFormatter.ofPattern("HH:mm");

    public static String localeDateToThaiStr(LocalDate date) {
        return date == null ? "" : date.format(THAI_DATE);
    }

    public static String localeDateTimeToThaiStr(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(THAI_DATE);
    }

    public static String localeDateTimeToTimeStr(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(TIME_HH_MM);
    }
}
