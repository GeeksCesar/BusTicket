package com.smartgeeks.busticket.utils;


import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Helpers {

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(calendar.getTime());
    }

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String hora = dateFormat.format(calendar.getTime());
        return hora;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static String getTime() {
        // Hora del telefono
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String hora = dateFormat.format(calendar.getTime());
        return hora;
    }

    public static String getDate() {
        Calendar calendar = Calendar.getInstance();
        String date = String.format("%d-%d-%d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        return date;
    }

    public static String getDateTicket() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR) - 2000;
        return String.format(Locale.getDefault(), "%d-%d-%d", day,month, year);
    }

    public static String getTimeTicket() {
        // Hora del telefono
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("HHmmss");
        String hora = dateFormat.format(calendar.getTime());
        return hora;
    }

    public static String formatTwelveHours(String hour){
        String time;
        String[] split = hour.split(":");
        int hora = Integer.parseInt(split[0]);

        if (hora > 12){
            time = (hora-12)+":"+split[1]+":"+split[2]+ " PM";
        } else {
            if (hora == 12){
                time = hour + " PM";
            } else {
                time = hour + " AM";
            }
        }

        return time;
    }

    public static String setString2DateVoucher(String str_date) {
        String[] valuesDate = str_date.split("-");
        return valuesDate[2]+valuesDate[1]+valuesDate[0].substring(2);
    }

    public static String setString2HourVoucher(String str_hour) {
        String[] valuesHour = str_hour.split(":");
        return valuesHour[0]+valuesHour[1]+valuesHour[2];
    }

}
