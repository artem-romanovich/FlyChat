package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class RegistrationActivity extends AppCompatActivity {

    Button btn_set_name, to_sign_click, btn_confirm;
    EditText set_name, set_password, set_email;

    private final String file_name = "file_username";
    private final String file_password = "file_password";
    private final String file_email = "file_email";

    public String user_name = null;
    public String user_password = null;
    public String user_email = null;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference User_name;
    DatabaseReference NewContact;
    DatabaseReference User_password;
    DatabaseReference User_email;

    private FirebaseAuth mAuth;
    public FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        btn_set_name = findViewById(R.id.btn_set_name);
        to_sign_click = findViewById(R.id.to_sign_in);
        set_name = findViewById(R.id.name);
        set_password = findViewById(R.id.password);
        set_email = findViewById(R.id.email);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_confirm.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        try {
            BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput(file_name)));
            user_name = br_n.readLine();
            BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput(file_password)));
            user_password = br_p.readLine();
            BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput(file_email)));
            user_email = br_e.readLine();

            signIn(user_email, user_password);

        } catch (IOException e) {
            e.printStackTrace();
        }

        to_sign_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(RegistrationActivity.this, SignIn.class);
                    startActivity(intent);

            }
        });
    }

    public void registrationClick(View v) {
        user_name = set_name.getText().toString();
        user_password = set_password.getText().toString();
        user_email = set_email.getText().toString();

        ReplaceRepeat replaceRepeat = new ReplaceRepeat();
        user_name = replaceRepeat.ReplaceRepeatStr(user_name);
        user_password = replaceRepeat.ReplaceRepeatStr(user_password);
        user_email = replaceRepeat.ReplaceRepeatStr(user_email);

        if (!user_email.equals("") &&
                !user_email.equals(" ") &&
                !user_email.equals("/n") &&
                !user_name.equals("") &&
                !user_name.equals(" ") &&
                !user_name.equals("/n")) {

        /*ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                // URL you want to redirect back to. The domain (www.example.com) for this
                // URL must be whitelisted in the Firebase Console.
                .setUrl("https://fludrex-dde50.firebaseapp.com/__/auth/action?mode=action&oobCode=code")
                // This must be true
                .setHandleCodeInApp(true)
                .setIOSBundleId("com.example.ios")
                .setAndroidPackageName("com.example.android", true, "16")
                .build();
        Log.wtf(TAG, actionCodeSettings.toString());*/

            if (user_name.length() <= 20) {
                if (user_password.length() >= 8) {
                    registration(user_email, user_password);
                    v.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getApplicationContext(), "Введите пароль подлиннее (минимум 8 символов)", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Слишком длинное имя", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getApplicationContext(), "Пустое поле ввода", Toast.LENGTH_SHORT).show();
    }

    private void sentEmailVerification(FirebaseUser user) {
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(RegistrationActivity.this,
                            //        "Авторизация произошла успешно", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RegistrationActivity.this, BottomNavigationActivity.class);
                            intent.putExtra("eT", user_name);
                            startActivity(intent);
                        }
                    }
                });
    }

    public void registration(String email, String password) {
        Toast.makeText(RegistrationActivity.this,
                "Перейдите по ссылке на почте", Toast.LENGTH_SHORT).show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            sentEmailVerification(currentUser);
                            to_sign_click.setVisibility(View.GONE);
                            btn_confirm.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(RegistrationActivity.this,
                                    "Регистрация оборвалась. Попробуйте еще раз", Toast.LENGTH_SHORT).show();
                            //FirebaseUser User = mAuth.getCurrentUser();
                            //User.delete();
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

    public void confirmClick(View view) {
        user_name = set_name.getText().toString();
        user_password = set_password.getText().toString();
        user_email = set_email.getText().toString();

        ReplaceRepeat replaceRepeat = new ReplaceRepeat();
        user_name = replaceRepeat.ReplaceRepeatStr(user_name);
        user_password = replaceRepeat.ReplaceRepeatStr(user_password);
        user_email = replaceRepeat.ReplaceRepeatStr(user_email);

        if (!user_email.equals("") &&
                !user_email.equals(" ") &&
                !user_email.equals("/n") &&
                !user_name.equals("") &&
                !user_name.equals(" ") &&
                !user_name.equals("/n")) {
            if (user_name.length() <= 20) {
                if (user_password.length() >= 8) {
                    Task<Void> usertask = Objects.requireNonNull(mAuth.getCurrentUser()).reload();
                    usertask.addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            currentUser = mAuth.getCurrentUser();
                        }
                    });

                    if (currentUser.isEmailVerified()) {
                        Toast.makeText(RegistrationActivity.this,
                                "Email подтвержден", Toast.LENGTH_SHORT).show();

                        String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        NewContact = database.getReference("Contacts/" + id);
                        NewContact.child(user_name).setValue(currentUser.getEmail());
                        User_name = database.getReference("Users/" + id);
                        User_name.child("name").setValue(user_name);
                        User_password = database.getReference("Users/" + id);
                        User_password.child("password").setValue(user_password);
                        User_email = database.getReference("Users/" + id);
                        User_email.child("email").setValue(user_email);

                        try {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput(file_name, MODE_PRIVATE)));
                            bw.write(user_name);
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput(file_password, MODE_PRIVATE)));
                            bw.write(user_password);
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput(file_email, MODE_PRIVATE)));
                            bw.write(user_email);
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(RegistrationActivity.this, BottomNavigationActivity.class);
                        intent.putExtra("eT", user_name);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Введите пароль подлиннее (минимум 8 символов)", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Слишком длинное имя", Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(getApplicationContext(), "Пустое поле ввода", Toast.LENGTH_SHORT).show();
        }
    }
}