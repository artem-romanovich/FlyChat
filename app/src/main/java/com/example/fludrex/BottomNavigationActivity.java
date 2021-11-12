package com.example.fludrex;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

/*
    BottomNavigationActivity. В ней происходит все основное действие.
*/

public class BottomNavigationActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Убрать ActionBar
        Objects.requireNonNull(getSupportActionBar()).hide();

        //Сборка воедино всех фрагментов, выбор начального
        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bluetooth,
                R.id.navigation_contacts,
                R.id.navigation_account).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        String t = getIntent().getStringExtra("toast");
        if (t != null) {
            if (t.equals("1")) {

                Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();

                WifiManager wifi = (WifiManager)
                        this.getSystemService(Context.WIFI_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                    startActivityForResult(panelIntent, 0);
                } else {
                    wifi.setWifiEnabled(true);
                }
                //Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
            }
            if (t.equals("2")) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        //Запись в файл поля, под которым хранится вся информация БД.
        try {
            BufferedWriter bs = new BufferedWriter(new OutputStreamWriter(
                    openFileOutput("file_secret_field", MODE_PRIVATE)));
            String secret_field = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAOc9PSHdGyL+SwYokr1xBo48GuBjaKLHQlvCA7PTY6WMllh9IJ31DbtJ08ATDwW+q0Pk7wM80d5kF1lUMXsUNkCAwEAAQ";
            bs.write(secret_field);
            bs.close();

            startService(secret_field);

        } catch (IOException e) {
            e.printStackTrace();
        }

        int MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
            switch (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:

                    //Если доступ запрещен к background-у запрещен, приложение проверяет, открыт ли доступ к местоположению
                    //в формате "только во время использования". Если да, уведомляет об этом пользователя и просит изменить
                    //формат разрешения
                    if (ContextCompat.checkSelfPermission(this.getBaseContext(),
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
                    }
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }

            //Если доступ запрещен, приложение для дальнейшей работы требует вручную перевести разрешение
            //в формат "в любом режиме" в настройках
            boolean hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!hasBackgroundLocationPermission) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
                //Toast.makeText(getApplicationContext(), "Необходим доступ к местоположению в любом режиме. Разрешите доступ вручную в настройках", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(BottomNavigationActivity.this, ImageActivity.class);
                intent.putExtra("BACKGROUND_LOCATION", "show");
                startActivity(intent);
                finish();
            }
        }
    }

    public void startService(String secret_field) {
        Intent serviceIntent = new Intent(this, MessageListeningService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, MessageListeningService.class);
        startService(serviceIntent);
    }

    public void allusers(View view) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), "Будет доступно позже", Toast.LENGTH_SHORT);
        toast.show();
    }
}