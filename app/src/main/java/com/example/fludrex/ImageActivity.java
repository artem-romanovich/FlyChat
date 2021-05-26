package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ImageActivity extends AppCompatActivity {

    private final String file_name = "file_username";
    private final String file_password = "file_password";
    private final String file_email = "file_email";

    public String user_name;
    public String user_password;
    public String user_email;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (!hasConnection(ImageActivity.this)) {
                    Toast.makeText(ImageActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                    ImageActivity.this.finish();
                } else {

                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    user_email = "_";
                    user_password = "_";

                    try {
                        BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput(file_name)));
                        user_name = br_n.readLine();
                        BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput(file_password)));
                        user_password = br_p.readLine();
                        BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput(file_email)));
                        user_email = br_e.readLine();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    signIn(user_email, user_password);
                }
            }
        }, 1000);                                           //!!!!!
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ImageActivity.this,
                                    "Авторизация произошла успешно", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ImageActivity.this, BottomNavigationActivity.class);
                            intent.putExtra("eT", user_name);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ImageActivity.this,
                                    "Авторизация оборвалась. Попробуйте еще раз", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ImageActivity.this, RegistrationActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }
}