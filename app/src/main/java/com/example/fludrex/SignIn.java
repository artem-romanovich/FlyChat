package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.fludrex.ui.contacts.ContactsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javax.crypto.Cipher;

public class SignIn extends AppCompatActivity {

    //Если у пользователя есть аккаунт, в данном классе он вводит имя, пароль, адрес почты и успешно авторизуется

    EditText sign_set_password,
    //sign_set_email,
    //sign_name,
    sign_nic;
    Button sign_btn;

    private final String file_name = "file_username";
    private final String file_nic = "file_nic";
    private final String file_password = "file_password";
    private final String file_email = "file_email";

    private FirebaseAuth mAuth;

    public String name;
    public String nic;
    public String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        sign_set_password = findViewById(R.id.sign_password);
        //sign_set_email = findViewById(R.id.sign_email);
        //sign_name = findViewById(R.id.sign_name);
        sign_btn = findViewById(R.id.sign_btn);
        sign_nic = findViewById(R.id.sign_nic);
    }

    public void signIn(//String email,
                       String password,
                       // String name,
                       String nic) {

        BufferedReader br_f = null;
        try {
            br_f = new BufferedReader(new InputStreamReader(openFileInput("file_secret_field")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String secret_field;
        try {
            secret_field = br_f.readLine();
            DatabaseReference IsThisNick = FirebaseDatabase.getInstance().getReference(secret_field + "/Contacts/" + nic);
            String finalSecret_field = secret_field;
            IsThisNick.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.getValue(String.class);

                        //DatabaseReference GetPassword = FirebaseDatabase.getInstance().getReference(finalSecret_field + "/Users/" + nic + "/" + "password");
                        DatabaseReference GetPassword = FirebaseDatabase.getInstance().getReference(finalSecret_field + "/Users/" + nic + "/" + "not_encrypt_password");
                        GetPassword.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                                String get_password_from_base = dataSnapshot.getValue(String.class);
                                final String[] get_password = {null};

                                try {
                                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                            openFileOutput(file_nic, MODE_PRIVATE)));
                                    bw.flush();
                                    bw.write(nic);
                                    bw.close();
                                } catch (IOException e) {
                                    sign_btn.setClickable(true);
                                    Toast.makeText(SignIn.this, "Данные повреждены", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                                //Пробуем получить приватный ключ из памяти физустройства по id чата
                                PrivateKey privateKey;
                                try {
                                    privateKey = getPrivateKey(nic);

                                    //Обозначаем, что с помощью данного ключа будем декодировать полученное сообщение
                                    Cipher rsa = Cipher.getInstance("RSA");
                                    rsa.init(Cipher.DECRYPT_MODE, privateKey);

                                    //Преобразовываем сообщение в массив байт
                                    //byte[] encrypt_sent_message = Base64.getDecoder().decode(get_message);
                                    byte[] encrypt_password = Base64.getDecoder().decode(get_password_from_base);
                                    byte[] utf8 = rsa.doFinal(encrypt_password);

                                    //Декодируем сообщение
                                    get_password[0] = new String(utf8, StandardCharsets.UTF_8);
                                } catch (Exception e) {
                                    sign_btn.setClickable(true);
                                    Toast.makeText(SignIn.this, "Данные повреждены", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                                //if (get_password[0] != null && get_password[0].equals(password)) {
                                if (get_password_from_base != null && get_password_from_base.equals(password)) {

                                    DatabaseReference GetPassword = FirebaseDatabase.getInstance().getReference(finalSecret_field + "/NameEmail/" + name);
                                    GetPassword.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {

                                            String get_email_from_base = dataSnapshot.getValue(String.class);

                                            if (get_email_from_base != null) {
                                                //mAuth.signInWithEmailAndPassword(get_email_from_base, password)
                                                //        .addOnCompleteListener(SignIn.this, new OnCompleteListener<AuthResult>() {
                                                //            @Override
                                                //            public void onComplete(@NonNull Task<AuthResult> task) {
                                                //                if (task.isSuccessful()) {

                                                                    try {
                                                                        Toast.makeText(SignIn.this, "Авторизация успешна", Toast.LENGTH_SHORT).show();

                                                                        Intent intent = new Intent(SignIn.this, BottomNavigationActivity.class);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(intent);

                                                                        try {
                                                                            BufferedWriter bn = new BufferedWriter(new OutputStreamWriter(
                                                                                    openFileOutput(file_name + nic, MODE_PRIVATE)));
                                                                            bn.flush();
                                                                            bn.write(name);
                                                                            bn.close();

                                                                            BufferedWriter bp = new BufferedWriter(new OutputStreamWriter(
                                                                                    openFileOutput(file_password + nic, MODE_PRIVATE)));
                                                                            bp.flush();
                                                                            bp.write(password);
                                                                            bp.close();

                                                                            BufferedWriter be = new BufferedWriter(new OutputStreamWriter(
                                                                                    openFileOutput(file_email + nic, MODE_PRIVATE)));
                                                                            be.flush();
                                                                            be.write(get_email_from_base);
                                                                            be.close();

                                                                        } catch (IOException e) {
                                                                            e.printStackTrace();
                                                                        }

                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }

                                                        //        } else {
                                                        //            Toast.makeText(SignIn.this, "Авторизация не произошла. Проверьте подключение к интернету и вводимые данные.", Toast.LENGTH_SHORT).show();
                                                        //            sign_btn.setClickable(true);
                                                        //        }
                                                        //    }
                                                        //});
                                            } else {
                                                sign_btn.setClickable(true);
                                                Toast.makeText(SignIn.this, "Ошибка", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                            sign_btn.setClickable(true);
                                        }
                                    });
                                } else {
                                    sign_btn.setClickable(true);
                                    Toast.makeText(SignIn.this, "Неверный пароль", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                sign_btn.setClickable(true);
                            }
                        });
                    } else {
                        sign_btn.setClickable(true);
                        Toast.makeText(SignIn.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
            sign_btn.setClickable(true);
        } catch (IOException e) {
            sign_btn.setClickable(true);
            e.printStackTrace();
        }
    }

    public void signClick(View view) {
        if (hasConnection(this)) {

            mAuth = FirebaseAuth.getInstance();
            password = sign_set_password.getText().toString();
            //email = sign_set_email.getText().toString();
            //name = sign_name.getText().toString();
            nic = sign_nic.getText().toString();

            ReplaceRepeat replaceRepeat = new ReplaceRepeat();
            password = replaceRepeat.ReplaceRepeatStr(password);
            //email = replaceRepeat.ReplaceRepeatStr(email);
            //name = replaceRepeat.ReplaceRepeatStr(name);
            nic = replaceRepeat.ReplaceRepeatStr(nic);

            signIn(//email,
                    password,
                    //name,
                    nic);
            sign_btn.setClickable(false);
            //Handler handler = new Handler();
            //handler.postDelayed(new Runnable() {
            //    public void run() {
            //        sign_btn.setClickable(true);
            //    }
            //}, 500);
        } else {
            Toast.makeText(this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
        }
    }

    //Метод, получающий секретный ключ через SharedPreferences из памяти устройства
    private PrivateKey getPrivateKey(String my_nic) throws Exception {

        SharedPreferences sharedPreference_passwordprivateKey = this.getSharedPreferences("sharedPreference_passwordprivateKey" + my_nic, Context.MODE_PRIVATE);
        Gson gson_passwordprivateKey = new Gson();
        String json_passwordprivateKey = sharedPreference_passwordprivateKey.getString("task_passwordprivateKey" + my_nic, null);
        if (json_passwordprivateKey != null) {
            json_passwordprivateKey = "{" + json_passwordprivateKey + "}";
        }
        Type type_passwordprivateKey = new TypeToken<TreeMap<String, String>>() {
        }.getType();
        Map<String, String> map = gson_passwordprivateKey.fromJson(json_passwordprivateKey, type_passwordprivateKey);

        String str_key = "";
        List<String> help_array_messages = new ArrayList<>(map.values());
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equals(my_nic)) {
                str_key = help_array_messages.get(i);
                //Log.wtf("str_key", str_key);
            }
        }

        byte[] privateKeyBytes = Base64.getDecoder().decode(str_key);
        //byte[] privateKeyBytes = Base64.decode(str_key, Base64.NO_PADDING | Base64.NO_WRAP);
        //Log.wtf("pvt", "BYTE KEY" + privateKeyBytes);
        //Log.wtf("pvt", "FINAL OUTPUT" + privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privatekey = keyFactory.generatePrivate(privateKeySpec);

        privateKeyBytes = privatekey.getEncoded();
        //Log.wtf("publicKey (byte) e/d", Arrays.toString(privateKeyBytes));

        return privatekey;
    }

    //Метод, проверяющий соединение с интернетом
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