package com.example.fludrex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CurrentTime {

    public String currenttime = "";

    static final String DATEFORMAT = "yyyyMMddHHmmss";

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

        //currenttime = GetUTCdatetimeAsDate().toString();
        //Log.wtf("time_current", GetUTCdatetimeAsDate().toString());

        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double currenttime = (System.currentTimeMillis() + offset);
                Log.wtf("time_current", String.valueOf(currenttime));
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
        Date currentTime = Calendar.getInstance().getTime();

        Log.wtf("time_current", String.valueOf(currentTime));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Date GetUTCdatetimeAsDate() {
        //note: doesn't check for null
        return StringDateToDate(GetUTCdatetimeAsString());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String GetUTCdatetimeAsString() {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT, Locale.getDefault(Locale.Category.FORMAT));
        sdf.setTimeZone(TimeZone.
                getTimeZone("Novosibirsk"));
                //getDefault());
        return sdf.format(new Date());
    }

    public static Date StringDateToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        try {
            dateToReturn = (Date)dateFormat.parse(StrDate);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return dateToReturn;
    }

    public static long getCurrentNetworkTime(Context context) {
        LocationManager locMan = (LocationManager) context.getSystemService(InternetActivity.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        long networkTS = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime();
        return networkTS;
    }

    public static final String TIME_SERVER = "time-a.nist.gov";

    /*public static long getCurrentNetworkTime() {
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
        TimeInfo timeInfo = timeClient.getTime(inetAddress);
        //long returnTime = timeInfo.getReturnTime();   //local device time
        long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();   //server time

        Date time = new Date(returnTime);
        Log.d(TAG, "Time from " + TIME_SERVER + ": " + time);

        return returnTime;
    }*/
}
