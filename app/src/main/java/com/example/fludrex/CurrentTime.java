package com.example.fludrex;

import android.util.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CurrentTime {

    //Класс имеет два метода, возвращающие точное время в разных вариантах
    //(в виде только цифр или в приятном глазу формате).
    //Вызываются из InternetActivity.

    public String currenttime = "";
    double delay_time;
    static final String DATEFORMAT = "yyyyMMddHHmmss";

    public String getCurrentTimeFromBaseNextLine(long delay_time) {
        //Формат - как в правом нижнем углу вашего экрана

        //Оценочное время равно сумме времени на физустройстве пользователя и отстройке времени между сервером Firebase и устройством
        long estimatedServerTimeMs = delay_time + System.currentTimeMillis();

        //Перевод времени в дату
        Date myDate = new Date(estimatedServerTimeMs);
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String dateText = dateFormat.format(myDate);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeText = timeFormat.format(myDate);
        currenttime = timeText + "\n" + dateText;

        Log.wtf("estimatedServerTimeMs", String.valueOf(estimatedServerTimeMs));
        Log.wtf("time_current", currenttime);

        return currenttime;

    }

    public String getCurrentTimeFromBase(long delay_time) {
        //Формат в виде последовательности цифр (удобен для сравнения)

        //Оценочное время равно сумме времени на физустройстве пользователя и отстройке времени между сервером Firebase и устройством
        long estimatedServerTimeMs = delay_time + System.currentTimeMillis();

        //Перевод времени в дату
        Date myDate = new Date(estimatedServerTimeMs);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        currenttime = dateFormat.format(myDate);

        Log.wtf("estimatedServerTimeMs", String.valueOf(estimatedServerTimeMs));
        Log.wtf("time_current", currenttime);

        return currenttime;

    }
}