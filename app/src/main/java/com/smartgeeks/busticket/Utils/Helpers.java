package com.smartgeeks.busticket.Utils;


import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Helpers {

    static Calendar calendar = Calendar.getInstance();

    public static String getCurrentDate() {
        String fecha = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        return fecha;
    }

    public static String getCurrentTime() {
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
        // Obtener la fecha y hora del telefono
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

}
