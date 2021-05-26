package com.example.fludrex;

import android.annotation.SuppressLint;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CurrentTime {

    public String currenttime = "";

    public void getCurrentTime() {
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeText = timeFormat.format(currentDate);
        currenttime = timeText + "\n" + dateText;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getCurrentTimeComma() {
        /*Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        DateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        String timeText = timeFormat.format(currentDate);
        currenttime = timeText +"_"+ dateText;*/

        /*LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        currenttime = dtf.format(now);*/

        Date myDate = new Date();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        currenttime = dateFormat.format(myDate);

        /*DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double currenttime = (System.currentTimeMillis() + offset);
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
        Date currentTime = Calendar.getInstance().getTime();*/
    }
}
