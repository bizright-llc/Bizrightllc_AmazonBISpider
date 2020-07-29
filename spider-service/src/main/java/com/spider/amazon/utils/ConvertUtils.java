package com.spider.amazon.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ConvertUtils {

    public static String VC_PROMOTION_DATETIME = "MMMM d, yyyy h:mm:ss a zzz";

    /**
     * Parse String to LocalDateTime
     * return null if parse failed
     * @param value
     * @param format
     * @return
     */
    public static LocalDateTime convertStringToLocalDateTime(String value, String format){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

        try {
            return LocalDateTime.parse(value, formatter);
        }catch (Exception ex){
            ex.printStackTrace();

            return null;
        }

    }

}
