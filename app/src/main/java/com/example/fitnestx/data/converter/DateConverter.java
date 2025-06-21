package com.example.fitnestx.data.converter;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @TypeConverter
    public static Date toDate(String dateString) {
        if (dateString == null) return null;
        try {
            return DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            return null; // Handle parsing error gracefully
        }
    }

    @TypeConverter
    public static String fromDate(Date date) {
        if (date == null) return null;
        return DATE_FORMAT.format(date);
    }
}
