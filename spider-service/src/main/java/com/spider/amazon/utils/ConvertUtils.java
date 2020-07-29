package com.spider.amazon.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
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

    /**
     * Convert String to BigDecimal
     * return null if parse failed
     * @param value
     * @return
     */
    public static BigDecimal convertStringToBigDecimal(String value){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        String pattern = "#,##0.0#";
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);

        try{
            return ((BigDecimal) decimalFormat.parse(value)).setScale(2, BigDecimal.ROUND_HALF_UP);
        } catch (ParseException e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Parse string to int, if parse failed return null
     * @param value
     * @return
     */
    public static Integer convertStringToInteger(String value){
        try{
            return Integer.parseInt(value);
        }catch (NumberFormatException e){
            e.printStackTrace();
            return null;
        }
    }

}
