package uniblox.ai.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private DateUtil() {}

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public static String formatIso(LocalDateTime dateTime) {
        return dateTime.format(ISO_FORMATTER);
    }

    public static LocalDateTime parseIso(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
    }
}
