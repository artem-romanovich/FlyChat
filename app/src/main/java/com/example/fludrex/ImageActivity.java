package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fludrex.ui.bluetooth.BluetoothFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
    public int my_version = 1;              //!!!

    public int boot_time = 0;
    public int last_version;
    public String status;

    private FirebaseAuth mAuth;
    int flag = 0;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference CAPABLE;
    DatabaseReference VERSION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        String my_name = null;
        String my_password = null;
        String my_email = null;

        try {
            BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput(file_name)));
            my_name = br_n.readLine();
            BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput(file_password)));
            my_password = br_p.readLine();
            BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput(file_email)));
            my_email = br_e.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (my_email == null || my_name == null || my_password == null) {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        } else {
            mAuth = FirebaseAuth.getInstance();
            //signIn(my_email, my_password);

            Intent intent = new Intent(ImageActivity.this, BottomNavigationActivity.class);
            startActivity(intent);
        }

    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

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