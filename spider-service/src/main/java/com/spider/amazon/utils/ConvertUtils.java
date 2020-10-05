package com.spider.amazon.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ConvertUtils {

    static Logger log = LoggerFactory.getLogger(ConvertUtils.class);

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

    /**
     *
     * @param numberStr
     * @return
     */
    public static Float convertNumberStrToFloat(String numberStr){
        if(numberStr == null || StringUtils.isEmpty(numberStr)){
            return null;
        }

        if (numberStr.equalsIgnoreCase("—") || numberStr.equalsIgnoreCase("â€”")){
            return 0f;
        }

        String str = numberStr.replaceAll("[^0-9.-]", "");

        try{
            return Float.parseFloat(str);
        }catch (Exception e){
            log.error("[convertNumberStrToFloat] number {} convert failed", str, e);
            return null;
        }
    }

}
