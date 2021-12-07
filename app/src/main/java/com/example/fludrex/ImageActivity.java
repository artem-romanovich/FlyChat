package com.example.fludrex;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/*
    Ранее активность представляла собой загрузочный экран с логотипом.
    За время демонстрации иконки выполнялась вся подготовительная работа, которая теперь перенесена в ContactsFragment.
    Идея использовать экран загрузки, отнимающий время пользователя (хотя пользователь прекрасно знает, в какое приложение он заходит),
    не кажется разработчику такой уж хорошей.
    Посему теперь первая активность - BottomNavigationActivity.

    P.S. Выдержка со статейки на habr:

    Вы будете удивлены, узнав что сторонники Google используют Splash Screen. Это описано прямо вот тут, в спецификации к Material Design.
    Так было не всегда. Google был против Splash Screen, и даже назвал его анти-паттерном.
    Я считаю, что Google не противоречит сам себе. Старый совет и новые рекомендации хорошо сочетаются.
    (Тем не менее, все-таки не очень хорошая идея использовать экран загрузки который отнимает время пользователя. Пожалуйста, не делайте так)
    Однако, Android приложениям требуется некоторое количество времени для запуска, особенно при холодном запуске.
    Существует задержка которую вы не можете избежать. Вместо того чтобы показывать пустой экран, почему бы не показать пользователю что-то хорошее?
    Именно за этот подход Google и выступает.
    Не стоит тратить время пользователя, но не показывайте ему пустой, ненастроенный раздел приложения, когда он запускает его впервые.
    Количество времени, которые вы тратите на просмотр Splash Screen, точно соответствует количеству времени, которое требуется приложению для запуска.
    При холодном запуске, это означает что Splash Screen будет виден дольше. А если приложение уже закэшировано, заставка исчезнет почти сразу.

    Именно так в приложении FlyChat организован ProgressBar в ContactsFragment.
*/

public class ImageActivity extends AppCompatActivity {

    VideoView videoPlayer;

    public String my_email;
    public String my_name;
    public String my_nic;
    public String my_password;

    private final String file_name = "file_username";
    private final String file_nic = "file_nic";
    private final String file_password = "file_password";
    private final String file_email = "file_email";

    LinearLayout linlay_gone, linlay_show, linlay_location;
    Button btn_to_settings;

    int do_intent = 1;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_image);

        linlay_show = findViewById(R.id.linlay_show);
        linlay_gone = findViewById(R.id.linlay_gone);
        linlay_location = findViewById(R.id.linlay_location);
        btn_to_settings = findViewById(R.id.btn_to_settings);

        linlay_gone.setVisibility(View.VISIBLE);
        linlay_show.setVisibility(View.GONE);
        linlay_location.setVisibility(View.GONE);

        String show_or_not = getIntent().getStringExtra("BACKGROUND_LOCATION");

        if (show_or_not != null) {
            if (show_or_not.equals("show")) {

                linlay_gone.setVisibility(View.GONE);
                linlay_location.setVisibility(View.VISIBLE);

                AutoStartHelper.getInstance().getAutoStartPermission(ImageActivity.this);

                btn_to_settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                btn_to_settings.setText("Продолжить");
                            }
                        }, 5000);

                        boolean hasForegroundLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                        boolean hasBackgroundLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

                        if (hasForegroundLocationPermission) {
                            if (hasBackgroundLocationPermission) {

                                Toast.makeText(getApplicationContext(), "Доступ разрешен", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ImageActivity.this, BottomNavigationActivity.class);
                                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(ImageActivity.this,
                                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                                startActivity(intent, bundle);
                                finish();
                            } else {
                                ActivityCompat.requestPermissions(ImageActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                        1);
                            }
                        } else {
                            ActivityCompat.requestPermissions(ImageActivity.this,
                                    new String[]{
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    2);
                        }
                    }
                });
            }
        } else {

            //Чтение из файлов данных пользователя и БД
            try {
                BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput(file_nic)));
                my_nic = br_nn.readLine();
                BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput(file_name + my_nic)));
                my_name = br_n.readLine();
                BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput(file_password + my_nic)));
                my_password = br_p.readLine();
                BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput(file_email + my_nic)));
                my_email = br_e.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (my_email == null || my_name == null || my_password == null) { //если данные некорректны для авторизации

                linlay_show.setVisibility(View.VISIBLE);
                linlay_gone.setVisibility(View.GONE);

                videoPlayer = findViewById(R.id.videoPlayer);
                Uri myVideoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fch_splash_screen);
                videoPlayer.setVideoURI(myVideoUri);
                videoPlayer.requestFocus(0);
                videoPlayer.setZOrderOnTop(true);
                videoPlayer.start();

                /*linlay_show.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        do_intent = 0;
                        Intent intent = new Intent(ImageActivity.this, BottomNavigationActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });*/

                if (do_intent == 1) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {

                            Intent intent = new Intent(ImageActivity.this, BottomNavigationActivity.class);
                            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(ImageActivity.this,
                                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                            startActivity(intent, bundle);
                            finish();
                        }
                    }, 5000);
                }

            } else {
                Intent intent = new Intent(ImageActivity.this, BottomNavigationActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initOPPO() {
        try {

            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.floatwindow.FloatWindowListActivity"));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
            try {

                Intent intent = new Intent("action.coloros.safecenter.FloatWindowListActivity");
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity"));
                startActivity(intent);
            } catch (Exception ee) {

                ee.printStackTrace();
                try{

                    Intent i = new Intent("com.coloros.safecenter");
                    i.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"));
                    startActivity(i);
                }catch (Exception e1){

                    e1.printStackTrace();
                }
            }

        }
    }
    private void initXiaomi() {
        try {

            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.floatwindow.FloatWindowListActivity"));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
            try {

                Intent intent = new Intent("action.coloros.safecenter.FloatWindowListActivity");
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity"));
                startActivity(intent);
            } catch (Exception ee) {

                ee.printStackTrace();
                try{

                    Intent i = new Intent("com.coloros.safecenter");
                    i.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.sysfloatwindow.FloatWindowListActivity"));
                    startActivity(i);
                }catch (Exception e1){

                    e1.printStackTrace();
                }
            }

        }
    }
}