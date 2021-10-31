package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.crypto.Cipher;

import static android.content.ContentValues.TAG;

/*
    RegistrationActivity создана для регистрации.

    Имеет кнопку "Получить сылку", "Подтвердить аккаунт" и "У меня уже есть аккаунт";
    EditText для ввода параметров пользователя, отправляет запрос на email-адрес пользователю.
    (См. разметку)
*/

public class RegistrationActivity extends AppCompatActivity {

    Button //btn_get_link,
            to_sign_click, btn_confirm, btn_cancel, btn_view_password;
    EditText set_name, set_password, set_email, set_nic;
    TextView tmp_password, tmp_email, tmp_name, tmp_nick, info_title, red1, red2;

    private final String file_name = "file_username";
    private final String file_password = "file_password";
    private final String file_nic = "file_nic";
    private final String file_email = "file_email";

    public String user_name = null;
    public String user_password = null;
    public String user_email = null;
    public String user_nic = null;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference CAPABLE;
    DatabaseReference VERSION;
    DatabaseReference User_name;
    DatabaseReference User_nic;
    DatabaseReference NewContact;
    DatabaseReference NameEmail;
    DatabaseReference User_password;
    DatabaseReference User_email;

    public int my_version = 5;  //!!!

    private FirebaseAuth mAuth;
    public FirebaseUser currentUser;

    String secret_field;

    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //btn_get_link = findViewById(R.id.btn_get_link);
        to_sign_click = findViewById(R.id.to_sign_in);
        set_name = findViewById(R.id.name);
        set_password = findViewById(R.id.password);
        set_email = findViewById(R.id.email);
        set_nic = findViewById(R.id.nic);
        btn_confirm = findViewById(R.id.btn_confirm);
        tmp_password = findViewById(R.id.tmp_password);
        tmp_email = findViewById(R.id.tmp_email);
        tmp_name = findViewById(R.id.tmp_name);
        tmp_nick = findViewById(R.id.tmp_nick);
        info_title = findViewById(R.id.info_title);
        red1 = findViewById(R.id.red1);
        red2 = findViewById(R.id.red2);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_view_password = findViewById(R.id.btn_view_password);

        btn_cancel.setVisibility(View.GONE);
        tmp_password.setVisibility(View.GONE);
        tmp_email.setVisibility(View.GONE);
        tmp_name.setVisibility(View.GONE);
        tmp_nick.setVisibility(View.GONE);

        try {
            BufferedReader br_f = new BufferedReader(new InputStreamReader(openFileInput("file_secret_field")));
            secret_field = br_f.readLine();

            BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_nic")));
            String tmp = br_nn.readLine();
            br_nn.close();
            if (tmp != null && !tmp.equals("")) {
                btn_confirm.setText("подтвердить аккаунт");
                to_sign_click.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btn_view_password.getWindowToken(), 0);

                try {
                    user_nic = tmp;
                    BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_name")));
                    user_name = br_n.readLine();
                    br_n.close();
                    BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_password")));
                    user_password = br_p.readLine();
                    br_p.close();
                    BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_email")));
                    user_email = br_e.readLine();
                    br_e.close();
                    Log.wtf("all_params", user_nic + user_name + user_password + user_email);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                info_title.setText("Перейдите по ссылке, отправленной на вашу электронную почту");
                info_title.setTextColor(Color.parseColor("#FF2819"));

                red1.setTextColor(Color.parseColor("#FF000000"));
                red2.setTextColor(Color.parseColor("#FF000000"));

                btn_confirm.setText("подтвердить аккаунт");
                to_sign_click.setVisibility(View.GONE);

                btn_cancel.setVisibility(View.VISIBLE);

                tmp_email.setText(user_email);
                tmp_name.setText(user_name);
                tmp_nick.setText(user_nic);
                char[] chars = new char[user_password.length()];
                Arrays.fill(chars, '*');
                tmp_password.setText(new String(chars));

                set_email.setVisibility(View.GONE);
                set_name.setVisibility(View.GONE);
                set_nic.setVisibility(View.GONE);
                set_password.setVisibility(View.GONE);
                btn_view_password.setVisibility(View.GONE);
                tmp_email.setVisibility(View.VISIBLE);
                tmp_name.setVisibility(View.VISIBLE);
                tmp_nick.setVisibility(View.VISIBLE);
                tmp_password.setVisibility(View.VISIBLE);

                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(RegistrationActivity.this);
                        alert.setTitle("Вы уверены?");
                        alert.setMessage("Вы точно хотите прервать создание вашего аккаунта? Вы сможете заново создать его в будущем");
                        alert.setPositiveButton("Да", new DialogInterface.OnClickListener() { //Принятие приглашения
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                try {
                                    BufferedWriter brnn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_nic", MODE_PRIVATE)));
                                    brnn.flush();
                                    brnn.close();
                                    BufferedWriter brn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_name", MODE_PRIVATE)));
                                    brn.flush();
                                    brn.close();
                                    BufferedWriter brp = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_password", MODE_PRIVATE)));
                                    brp.flush();
                                    brp.close();
                                    BufferedWriter bre = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_email", MODE_PRIVATE)));
                                    bre.flush();
                                    bre.close();
                                    Log.wtf("all_params", user_nic + user_name + user_password + user_email);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                Intent intent = new Intent(RegistrationActivity.this, BottomNavigationActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        alert.setNegativeButton("Нет", new DialogInterface.OnClickListener() { //Отказ
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        alert.show();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        int two_digits = (int) (Math.random() * 100);
        View.OnTouchListener touchlisten = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    if (set_email != null) {
                        String idea_nick = set_email.getText().toString();
                        if (!idea_nick.equals("")) {
                            int indexet = idea_nick.indexOf("@");
                            if (indexet != -1) {
                                idea_nick = idea_nick.substring(0, indexet);
                            }
                            String finalIdea_nick = idea_nick;
                            set_nic.setHint("Пример: " + finalIdea_nick.toLowerCase().replaceAll("\\d", "") + two_digits);
                        } else {
                            set_nic.setHint("Придумайте никнейм");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };
        set_nic.setOnTouchListener(touchlisten);

        //Получение экземпляра FirebaseAuth (аутентификация), получение текущего пользователя
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        btn_view_password.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_UP:
                        set_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        set_password.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/montserrat_bold.ttf"));
                        break;

                    case MotionEvent.ACTION_DOWN:
                        set_password.setInputType(InputType.TYPE_CLASS_TEXT);
                        set_password.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/montserrat_bold.ttf"));
                        break;

                }
                return true;
            }
        });

        //Попытка чтения из файлов данных пользователя
        try {
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput(file_nic)));
            user_nic = br_nn.readLine();
            BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput(file_name + user_nic)));
            user_name = br_n.readLine();
            BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput(file_password + user_nic)));
            user_password = br_p.readLine();
            BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput(file_email + user_nic)));
            user_email = br_e.readLine();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registrationClick(View view) throws Exception {
        //Отключаем кнопку
        //btn_get_link.setClickable(false);

        //Преобразуем данные в String переменные
        user_name = set_name.getText().toString();
        user_password = set_password.getText().toString();
        user_email = set_email.getText().toString();
        user_nic = set_nic.getText().toString();

        //Очищаем переменные от лишнего мусора - пробелов в начале и конце
        ReplaceRepeat replaceRepeat = new ReplaceRepeat();
        user_name = replaceRepeat.ReplaceRepeatStr(user_name);
        user_password = replaceRepeat.ReplaceRepeatStr(user_password);
        user_email = replaceRepeat.ReplaceRepeatStr(user_email);
        user_nic = replaceRepeat.ReplaceRepeatStr(user_nic);
        try {
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_nic")));
            String tmp = br_nn.readLine();
            br_nn.close();
            if (tmp != null && !tmp.equals("")) {
                btn_confirm.setText("подтвердить аккаунт");
                to_sign_click.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btn_view_password.getWindowToken(), 0);


                user_nic = tmp;
                BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_name")));
                user_name = br_n.readLine();
                br_n.close();
                BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_password")));
                user_password = br_p.readLine();
                br_p.close();
                BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_email")));
                user_email = br_e.readLine();
                br_e.close();
                Log.wtf("all_params", user_nic + user_name + user_password + user_email);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Если поле не пустое (в различных конфигурациях)
        if (!user_email.equals("") &&
                !user_email.equals(" ") &&
                !user_email.equals("/n") &&
                !user_password.equals("") &&
                !user_password.equals(" ") &&
                !user_password.equals("/n") &&
                !user_nic.equals("") &&
                !user_nic.equals(" ") &&
                !user_nic.equals("/n") &&

                !user_name.equals("") &&
                !user_name.equals(" ") &&
                !user_name.equals("/n")) {

            //Если все данные позволяют зарегестрировать пользователя
            if (user_email.contains("mail") ||
                    user_email.contains("gmail") ||
                    user_email.contains("list")) {
                if (user_nic.length() <= 20) {
                    if (user_name.length() <= 20) {
                        if (user_password.length() >= 8) {

                            //Появление Snackbar
                            Snackbar snackbar = Snackbar.make(view, "Убедитесь, что вы сохранили свой пароль", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Я запомнил\nпароль", new View.OnClickListener() { //Пользователь хочет общаться с собеседника
                                @Override
                                public void onClick(View v) {

                                    DatabaseReference IsThisNick = FirebaseDatabase.getInstance().getReference(secret_field + "/Contacts/" + user_nic);
                                    IsThisNick.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (!snapshot.exists()) {
                                                DatabaseReference IsThisName = FirebaseDatabase.getInstance().getReference(secret_field + "/NameEmail/" + user_name);
                                                IsThisName.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot33) {
                                                        if (!snapshot33.exists()) {

                                                            info_title.setText("Перейдите по ссылке, отправленной на вашу электронную почту");
                                                            info_title.setTextColor(Color.parseColor("#FF2819"));

                                                            red1.setTextColor(Color.parseColor("#FF000000"));
                                                            red2.setTextColor(Color.parseColor("#FF000000"));

                                                            btn_confirm.setText("подтвердить аккаунт");
                                                            to_sign_click.setVisibility(View.GONE);

                                                            btn_cancel.setVisibility(View.VISIBLE);

                                                            tmp_email.setText(user_email);
                                                            tmp_name.setText(user_name);
                                                            tmp_nick.setText(user_nic);
                                                            char[] chars = new char[user_password.length()];
                                                            Arrays.fill(chars, '*');
                                                            tmp_password.setText(new String(chars));

                                                            set_email.setVisibility(View.GONE);
                                                            set_name.setVisibility(View.GONE);
                                                            set_nic.setVisibility(View.GONE);
                                                            set_password.setVisibility(View.GONE);
                                                            btn_view_password.setVisibility(View.GONE);
                                                            tmp_email.setVisibility(View.VISIBLE);
                                                            tmp_name.setVisibility(View.VISIBLE);
                                                            tmp_nick.setVisibility(View.VISIBLE);
                                                            tmp_password.setVisibility(View.VISIBLE);

                                                            //Появление Snackbar
                                                            Snackbar snackbar = Snackbar.make(view, "Перейдите по отправленной на электронную почту ссылке", Snackbar.LENGTH_INDEFINITE);
                                                            snackbar.setAction("Перейти", new View.OnClickListener() { //Пользователь хочет общаться с собеседника
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                                                    intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    startActivity(intent);
                                                                }
                                                            });
                                                            snackbar.setTextColor(0XFFFFFFFF);
                                                            snackbar.setBackgroundTint(0XFF31708E);
                                                            snackbar.setActionTextColor(0XFFFFFFFF);
                                                            snackbar.show();

                                                            mAuth.createUserWithEmailAndPassword(user_email, user_password)
                                                                    .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                                            if (task.isSuccessful()) {

                                                                                currentUser = mAuth.getCurrentUser();
                                                                                //Запись в файлы пользовательских данных.
                                                                                try {
                                                                                    BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                                                                            openFileOutput("tmp_file_nic", MODE_PRIVATE)));
                                                                                    bnn.write(user_nic);
                                                                                    bnn.close();

                                                                                    BufferedWriter bn = new BufferedWriter(new OutputStreamWriter(
                                                                                            openFileOutput("tmp_file_name", MODE_PRIVATE)));
                                                                                    bn.write(user_name);
                                                                                    bn.close();

                                                                                    BufferedWriter bp = new BufferedWriter(new OutputStreamWriter(
                                                                                            openFileOutput("tmp_file_password", MODE_PRIVATE)));
                                                                                    bp.write(user_password);
                                                                                    bp.close();

                                                                                    BufferedWriter be = new BufferedWriter(new OutputStreamWriter(
                                                                                            openFileOutput("tmp_file_email", MODE_PRIVATE)));
                                                                                    be.write(user_email);
                                                                                    be.close();
                                                                                    Log.wtf("all_params", user_nic + user_name + user_password + user_email);
                                                                                } catch (IOException e) {
                                                                                    e.printStackTrace();
                                                                                }

                                                                                ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                                                                                        .setUrl("https://www.example.com/?email=" + user_email)
                                                                                        .setHandleCodeInApp(true)
                                                                                        .setAndroidPackageName("com.example.fludrex", false, null)
                                                                                        .build();

                                                                                ActionCodeSettings actioncodesettings = ActionCodeSettings.newBuilder()
                                                                                        //.setUrl(url)
                                                                                        //.setIOSBundleId("com.example.ios")
                                                                                        //.setHandleCodeInApp(true)
                                                                                        //.setAndroidPackageName("com.example.android", false, null)
                                                                                        //.build();

                                                                                        .setUrl("http://www.example.com/verify?uid=" + user_email)
                                                                                        .setHandleCodeInApp(true)
                                                                                        .setAndroidPackageName(Objects.requireNonNull(actionCodeSettings.getAndroidPackageName()),
                                                                                                actionCodeSettings.getAndroidInstallApp(),
                                                                                                actionCodeSettings.getAndroidMinimumVersion())
                                                                                        .setIOSBundleId("com.example.ios")
                                                                                        .build();

                                                                                if (currentUser != null) {
                                                                                    currentUser.sendEmailVerification().addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                        }
                                                                                    });
                                                                                }

                                                                                btn_cancel.setOnClickListener(new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View v) {
                                                                                        AlertDialog.Builder alert = new AlertDialog.Builder(RegistrationActivity.this);
                                                                                        alert.setTitle("Вы уверены?");
                                                                                        alert.setMessage("Вы точно хотите прервать создание вашего аккаунта? Вы сможете заново создать его в будущем");
                                                                                        alert.setPositiveButton("Да", new DialogInterface.OnClickListener() { //Принятие приглашения
                                                                                            @Override
                                                                                            public void onClick(DialogInterface dialog, int which) {

                                                                                                try {
                                                                                                    BufferedWriter brnn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_nic", MODE_PRIVATE)));
                                                                                                    brnn.flush();
                                                                                                    brnn.close();
                                                                                                    BufferedWriter brn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_name", MODE_PRIVATE)));
                                                                                                    brn.flush();
                                                                                                    brn.close();
                                                                                                    BufferedWriter brp = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_password", MODE_PRIVATE)));
                                                                                                    brp.flush();
                                                                                                    brp.close();
                                                                                                    BufferedWriter bre = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_email", MODE_PRIVATE)));
                                                                                                    bre.flush();
                                                                                                    bre.close();
                                                                                                    Log.wtf("all_params", user_nic + user_name + user_password + user_email);
                                                                                                } catch (Exception e) {
                                                                                                    e.printStackTrace();
                                                                                                }

                                                                                                Intent intent = new Intent(RegistrationActivity.this, BottomNavigationActivity.class);
                                                                                                startActivity(intent);
                                                                                                finish();
                                                                                            }
                                                                                        });
                                                                                        alert.setNegativeButton("Нет", new DialogInterface.OnClickListener() { //Отказ
                                                                                            @Override
                                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                                dialog.dismiss();
                                                                                            }
                                                                                        });
                                                                                        alert.show();
                                                                                    }
                                                                                });

                                                                                //Отображение кнопки для подтверждении адреса эл. почты
                                                                                //to_sign_click.setVisibility(View.GONE);
                                                                                //btn_confirm.setVisibility(View.VISIBLE);

                                                                            } else {
                                                                                Toast.makeText(RegistrationActivity.this,
                                                                                        "Регистрация оборвалась. Попробуйте выбрать другой адрес электроной почты", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                            //v.setVisibility(View.GONE);
                                                        } else {
                                                            Toast.makeText(RegistrationActivity.this, "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                                    }
                                                });

                                            } else {
                                                Toast.makeText(RegistrationActivity.this, "Пользователь с таким никнеймом уже существует", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                        }
                                    });
                                }
                            });
                            snackbar.setTextColor(0XFFFFFFFF);
                            snackbar.setBackgroundTint(0XFF31708E);
                            snackbar.setActionTextColor(0XFFFFFFFF);
                            snackbar.show();

                        } else {
                            Toast.makeText(getApplicationContext(), "Введите пароль подлиннее (минимум 8 символов)", Toast.LENGTH_SHORT).show();
                            //btn_get_link.setClickable(true);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Слишком длинное имя", Toast.LENGTH_SHORT).show();
                        //btn_get_link.setClickable(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Введите ник покороче", Toast.LENGTH_SHORT).show();
                    //btn_get_link.setClickable(true);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Подходят адреса от \"mail\", \"list\" или \"gmail\"", Toast.LENGTH_SHORT).show();
                //btn_get_link.setClickable(true);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Пустое поле ввода", Toast.LENGTH_SHORT).show();
            //btn_get_link.setClickable(true);
        }
    }

    //Метод, вызываемый при подтверждении эл. адреса при помощи ссылки
    public void confirmClick(View view) {

        if (!hasConnection(RegistrationActivity.this)) {
            Toast.makeText(RegistrationActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
        } else {

            //Получение доступа к БД Firebase. Определяем статус БД.
            CAPABLE = database.getReference(secret_field + "/Status/Capable");
            final ChildEventListener childEventListener1 = CAPABLE.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                    if (datasnapshot.exists()) {
                        try {
                            //Получаем статус
                            String status = datasnapshot.getValue(String.class);
                            Log.wtf("Capable", status);

                            //Если на сервере техн. режим, пользователю запрещается доступ к приложению
                            if (status.equals("NO")) {
                                Toast.makeText(getApplicationContext(), "Ведутся работы, приложение временно недоступно", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegistrationActivity.this, BottomNavigationActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                if (!status.equals("YES")) {
                                    Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
                                }
                            }
                            if (status.equals("YES")) {//Если работы на сервере не ведутся

                                //Получение доступа к БД Firebase. Определяем последнюю версию приложения.
                                VERSION = database.getReference(secret_field + "/Status/Version");
                                final ChildEventListener childEventListener2 = VERSION.addChildEventListener(new ChildEventListener() {
                                    @SuppressLint("ResourceType")
                                    @Override
                                    public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {

                                        //Получаем последнюю версию
                                        int last_version = datasnapshot.getValue(Integer.class);
                                        Log.wtf("Version", String.valueOf(last_version));

                                        //Данные массивы нужны просто для корретного отображения информация через Toast.
                                        String[] versionnumber = {" ", "(первая) ", "(вторая) ", "(третья) ", "(четвертая) ", "(пятая) "};
                                        String[] versionnumber2 = {"", "первой", "второй", "третьей", "четвертой", "пятой"};

                                        if (last_version != my_version) {

                                            //Если версия не последняя, пользователь об этом уведомляется. Доступ запрещается
                                            //в связи с возможными проблемами при несовместимости версий
                                            if (last_version < versionnumber2.length) {
                                                Toast.makeText(getApplicationContext(), "Неактуальная " + versionnumber[my_version] + "версия приложения. " +
                                                        "Требуется обновление до " + versionnumber2[last_version], Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(RegistrationActivity.this, BottomNavigationActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Неактуальная версия приложения. Требуется обновление", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(RegistrationActivity.this, BottomNavigationActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        } else {

                                            currentUser = mAuth.getCurrentUser();

                                            //Обновляем текущего пользователя Firebase
                                            Task<Void> usertask;
                                            if (currentUser != null) {
                                                usertask = currentUser.reload();
                                                usertask.addOnSuccessListener(new OnSuccessListener() {
                                                    @Override
                                                    public void onSuccess(Object o) {
                                                        currentUser = mAuth.getCurrentUser();
                                                    }
                                                });
                                            }

                                            ///user_name = set_name.getText().toString();
                                            ///user_password = set_password.getText().toString();
                                            ///user_email = set_email.getText().toString();
                                            ///user_nic = set_nic.getText().toString();

                                            /////Очищаем переменные от лишнего мусора - пробелов в начале и конце
                                            ///ReplaceRepeat replaceRepeat = new ReplaceRepeat();
                                            ///user_name = replaceRepeat.ReplaceRepeatStr(user_name);
                                            ///user_password = replaceRepeat.ReplaceRepeatStr(user_password);
                                            ///user_email = replaceRepeat.ReplaceRepeatStr(user_email);
                                            ///user_nic = replaceRepeat.ReplaceRepeatStr(user_nic);

                                            //if (!user_email.equals("") &&
                                            //        !user_email.equals(" ") &&
                                            //        !user_email.equals("/n") &&

//                                        //        !user_password.equals("") &&
                                            //        !user_password.equals(" ") &&
                                            //        !user_password.equals("/n") &&

//                                        //        !user_nic.equals("") &&
                                            //        !user_nic.equals(" ") &&
                                            //        !user_nic.equals("/n") &&

//                                        //        !user_name.equals("") &&
                                            //        !user_name.equals(" ") &&
                                            //        !user_name.equals("/n")) {
                                            //    if (user_name.length() <= 20) {
                                            //        if (user_password.length() >= 8) {

                                            //Если email подтвержден
                                            if (currentUser != null) {
                                                if (currentUser.isEmailVerified()) {
                                                    if (toast != null) {
                                                        toast.cancel();
                                                    }
                                                    toast = Toast.makeText(RegistrationActivity.this, "Аккаунт успешно создан", Toast.LENGTH_LONG);
                                                    toast.show();

                                                    try {
                                                        //Чтение поля, под которым хранится вся информация БД.
                                                        BufferedReader br_f = new BufferedReader(new InputStreamReader(openFileInput("file_secret_field")));
                                                        secret_field = br_f.readLine();
                                                        br_f.close();

                                                        BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_nic")));
                                                        user_nic = br_nn.readLine();
                                                        br_nn.close();
                                                        BufferedReader br_n = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_name")));
                                                        user_name = br_n.readLine();
                                                        br_n.close();
                                                        BufferedReader br_p = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_password")));
                                                        user_password = br_p.readLine();
                                                        br_p.close();
                                                        BufferedReader br_e = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_email")));
                                                        user_email = br_e.readLine();
                                                        br_e.close();

                                                        Log.wtf("all_params", user_nic + user_name + user_password + user_email);

                                                        generateKeys("RSA", 2048, user_password, user_nic);

                                                        //Получение доступа к БД Firebase. Отправка пользовательских данных.
                                                        NewContact = database.getReference(secret_field + "/Contacts/");
                                                        NewContact.child(user_nic).setValue(user_name);
                                                        NameEmail = database.getReference(secret_field + "/NameEmail/");
                                                        NameEmail.child(user_name).setValue(user_email);
                                                        User_name = database.getReference(secret_field + "/Users/" + user_nic);
                                                        User_name.child("name").setValue(user_name);
                                                        User_nic = database.getReference(secret_field + "/Users/" + user_nic);
                                                        User_nic.child("nickname").setValue(user_nic);
                                                        User_email = database.getReference(secret_field + "/Users/" + user_nic);
                                                        User_email.child("email").setValue(user_email);

                                                        //Запись в файлы пользовательских данных.
                                                        BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                                                openFileOutput(file_nic, MODE_PRIVATE)));
                                                        bnn.write(user_nic);
                                                        bnn.close();
                                                        BufferedWriter bn = new BufferedWriter(new OutputStreamWriter(
                                                                openFileOutput(file_name + user_nic, MODE_PRIVATE)));
                                                        bn.write(user_name);
                                                        bn.close();
                                                        BufferedWriter bp = new BufferedWriter(new OutputStreamWriter(
                                                                openFileOutput(file_password + user_nic, MODE_PRIVATE)));
                                                        bp.write(user_password);
                                                        bp.close();
                                                        BufferedWriter be = new BufferedWriter(new OutputStreamWriter(
                                                                openFileOutput(file_email + user_nic, MODE_PRIVATE)));
                                                        be.write(user_email);
                                                        be.close();

                                                        BufferedWriter brnn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_nic", MODE_PRIVATE)));
                                                        BufferedWriter brn = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_name", MODE_PRIVATE)));
                                                        BufferedWriter brp = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_password", MODE_PRIVATE)));
                                                        BufferedWriter bre = new BufferedWriter(new OutputStreamWriter(openFileOutput("tmp_file_email", MODE_PRIVATE)));
                                                        brnn.flush();
                                                        brnn.close();
                                                        brn.flush();
                                                        brn.close();
                                                        brp.flush();
                                                        brp.close();
                                                        bre.flush();
                                                        bre.close();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                    //Переход в основную акивность BottomNavigationActivity
                                                    Intent intent = new Intent(RegistrationActivity.this, BottomNavigationActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    intent.putExtra("eT", user_name);
                                                    startActivity(intent);
                                                    finish();

                                                } else {
                                                    if (toast != null) {
                                                        toast.cancel();
                                                    }
                                                    btn_confirm.setText("подтвердить аккаунт");
                                                    toast = Toast.makeText(getApplicationContext(), "Если вы перешли по ссылке, нажмите повторно", Toast.LENGTH_LONG);
                                                    toast.show();
                                                }
                                            } else {
                                                try {
                                                    registrationClick(btn_confirm);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            //        } else {
                                            //            Toast.makeText(getApplicationContext(), "Введите пароль подлиннее (минимум 8 символов)", Toast.LENGTH_SHORT).show();
                                            //        }
                                            //    } else {
                                            //        Toast.makeText(getApplicationContext(), "Слишком длинное имя", Toast.LENGTH_SHORT).show();
                                            //    }
                                            //} else {
                                            //    Toast.makeText(getApplicationContext(), "Пустое поле ввода", Toast.LENGTH_SHORT).show();
                                            //}
                                        }
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                                    }

                                    @Override
                                    public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                }

                @Override
                public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                }

                @Override
                public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
        }
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

    //Метод, создающий key pair (шифрование - RSA Algorithm)
    private void generateKeys(String keyAlgorithm, int numBits, String user_password, String user_nic) {
        try {
            //Создаем key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
            keyGen.initialize(numBits);
            KeyPair keyPair = keyGen.genKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            //Сохраняем key pair
            saveKeyPair(privateKey, publicKey, user_password, user_nic);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Метод, сохраняющий key pair
    public byte[] saveKeyPair(PrivateKey privateKey, PublicKey
            publicKey, String user_password, String
                                      user_nic) throws Exception {

        //Кодировка в байты
        byte[] privateKeyBytes = privateKey.getEncoded();
        byte[] publicKeyBytes = publicKey.getEncoded();
        Log.wtf("privateKey (byte)", Arrays.toString(privateKeyBytes));
        Log.wtf("publicKey (byte)", Arrays.toString(publicKeyBytes));

        Log.wtf("pvt", "BYTE KEY " + Arrays.toString(privateKeyBytes));
        String str_key_pvt = Base64.getEncoder().encodeToString(privateKeyBytes);
        //String str_key_pvt = Base64.encodeToString(privateKeyBytes, Base64.NO_PADDING | Base64.NO_WRAP);
        Log.wtf("pvt", "STRING KEY" + str_key_pvt);

        //Сохраняем секретный ключ в память устройства
        Map<String, String> map_pvt = (Map<String, String>) new TreeMap<String, String>();
        map_pvt.put(user_nic, str_key_pvt);
        SharedPreferences sharedPreference_passwordprivateKey = getSharedPreferences("sharedPreference_passwordprivateKey" + user_nic, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_passwordprivateKey = sharedPreference_passwordprivateKey.edit();
        Gson gson_passwordprivateKey = new Gson();
        String json_passwordprivateKey = gson_passwordprivateKey.toJson(map_pvt);
        if (json_passwordprivateKey != null) {
            json_passwordprivateKey = json_passwordprivateKey.substring(1, json_passwordprivateKey.length() - 1);
        }
        editor_passwordprivateKey.putString("task_passwordprivateKey" + user_nic, json_passwordprivateKey);
        editor_passwordprivateKey.apply();

        Log.wtf("pbl", "BYTE KEY " + Arrays.toString(publicKeyBytes));
        String str_key_pbl = Base64.getEncoder().encodeToString(publicKeyBytes);
        //String str_key_pbl = Base64.encodeToString(publicKeyBytes, Base64.NO_PADDING | Base64.NO_WRAP);
        Log.wtf("pbl", "STRING KEY " + str_key_pbl);

        //Сохраняем публичный ключ в память устройства
        Map<String, String> map_pbl = (Map<String, String>) new TreeMap<String, String>();
        map_pbl.put(user_nic, str_key_pbl);
        SharedPreferences sharedPreference_passwordpublicKey = getSharedPreferences("sharedPreference_passwordpublicKey" + user_nic, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_passwordpublicKey = sharedPreference_passwordpublicKey.edit();
        Gson gson_passwordpublicKey = new Gson();
        String json_passwordpublicKey = gson_passwordpublicKey.toJson(map_pbl);
        if (json_passwordpublicKey != null) {
            json_passwordpublicKey = json_passwordpublicKey.substring(1, json_passwordpublicKey.length() - 1);
        }
        editor_passwordpublicKey.putString("task_passwordpublicKey" + user_nic, json_passwordpublicKey);
        editor_passwordpublicKey.apply();


        //Определяем, что мы с помощью данного ключа будем шифровать сообщение
        Cipher rsa = Cipher.getInstance("RSA");
        rsa.init(Cipher.ENCRYPT_MODE, publicKey);

        //И непосредственно это и делаем - шифруем сообщение, переводим в строку
        String encrypt_user_password = Base64.getEncoder().encodeToString(rsa.doFinal(user_password.getBytes()));
        Log.wtf("encrypt_user_password", encrypt_user_password);

        User_password = database.getReference(secret_field + "/Users/" + user_nic);
        User_password.child("password").setValue(encrypt_user_password);
        User_password.child("not_encrypt_password").setValue(user_password);

        return privateKeyBytes;
    }

    public void click_to_signin(View view) {

        to_sign_click.setClickable(false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                to_sign_click.setClickable(true);
            }
        }, 100);

        Intent intent = new Intent(RegistrationActivity.this, SignIn.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput("tmp_file_nic")));
            String tmp = br_nn.readLine();
            br_nn.close();
            if (tmp != null && !tmp.equals("")) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(btn_view_password.getWindowToken(), 0);

                btn_confirm.setText("подтвердить аккаунт");
                to_sign_click.setVisibility(View.GONE);

                info_title.setText("Перейдите по ссылке, отправленной на вашу электронную почту");
                info_title.setTextColor(Color.parseColor("#FF2819"));

                red1.setTextColor(Color.parseColor("#FF000000"));
                red2.setTextColor(Color.parseColor("#FF000000"));

                btn_confirm.setText("подтвердить аккаунт");
                to_sign_click.setVisibility(View.GONE);

                btn_cancel.setVisibility(View.VISIBLE);

                tmp_email.setText(user_email);
                tmp_name.setText(user_name);
                tmp_nick.setText(user_nic);
                char[] chars = new char[user_password.length()];
                Arrays.fill(chars, '*');
                tmp_password.setText(new String(chars));

                set_email.setVisibility(View.GONE);
                set_name.setVisibility(View.GONE);
                set_nic.setVisibility(View.GONE);
                set_password.setVisibility(View.GONE);
                btn_view_password.setVisibility(View.GONE);
                tmp_email.setVisibility(View.VISIBLE);
                tmp_name.setVisibility(View.VISIBLE);
                tmp_nick.setVisibility(View.VISIBLE);
                tmp_password.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}