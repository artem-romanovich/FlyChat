package com.example.fludrex;

//Есть разница между двумя вариантами кодирования через Base64. Для версий
//android < 8 шифрование не работает должным образом. Пришлось поднять требования,
//с android версии 8.0 (Oreo) шифрование работает корректно.
//В коде можно посмотреть разницу между реализациями каждого варианта.

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//!
//import android.util.Base64;
import java.util.Base64;
//!
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.crypto.Cipher;

/*
    Наверное, самоая главная активность - InternetActivity.
    В ней происходит общение пользователей друг с другом.

    Имеет кнопку "Отправить", EditText для ввода текста, отображает список сообщений
    при помощи ListView, отправляет запрос от пользователю.
    (См. разметку)
*/

public class InternetActivity extends AppCompatActivity {

    public static int max_message_length = 2500;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public FirebaseUser currentUser;
    DatabaseReference ONLINE_USERS;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference USER;
    DatabaseReference USER_STORAGE;
    static DatabaseReference INTERLOCUTOR;
    DatabaseReference INTERLOCUTOR_STORAGE;
    DatabaseReference REQUEST;
    DatabaseReference NOTIFICATION;

    EditText get_message;
    TextView current_interlocutor;
    ImageButton btn_edit_message;
    Button top_delete, top_copy, top_cross;
    LinearLayout linlay_extra;
    public static ArrayList<String> MESSAGES;
    public static Map<String, MyMessage> map;
    public static ArrayList<MyMessage> messages = new ArrayList<>();

    public String sent_message;
    public String my_name; //!
    public String my_nic;
    public String interlocutor_name;
    public String interlocutor_nic;
    public String chatId;
    String mkey;

    private static final String CHANNEL_ID = "New channel";

    public ValueEventListener remove_message_listener;
    public static ChildEventListener childEventListener1;
    public static int listener_status = 1;

    long delay_time_long;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet);
        Objects.requireNonNull(getSupportActionBar()).hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Чтение поля, под которым хранится вся информация БД.
        try {
            BufferedReader br_f = new BufferedReader(new InputStreamReader(openFileInput("file_secret_field")));
            String secret_field = br_f.readLine();

            BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput("file_nic")));
            my_nic = br_nn.readLine();
            //Читаем файл, пробуем получить имя пользователя
            BufferedReader br_n;
            try {
                br_n = new BufferedReader(new InputStreamReader(openFileInput("file_username" + my_nic)));
                my_name = br_n.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

            btn_edit_message = findViewById(R.id.btn_edit_message);
            get_message = findViewById(R.id.get_message);
            current_interlocutor = findViewById(R.id.current_interlocutor);
            linlay_extra = findViewById(R.id.linlay_extra);
            top_copy = findViewById(R.id.top_copy);
            top_cross = findViewById(R.id.top_cross);
            top_delete = findViewById(R.id.top_delete);

            current_interlocutor.setVisibility(View.VISIBLE);
            linlay_extra.setVisibility(View.GONE);

            //Статус слушателей - включен
            listener_status = 1;

            //Создаем поток - при отсутствиея доступа к интернету приложение для дальнейшей работы рекомендует подключиться к сети
            Thread t = new Thread(() -> {
                while (true) {
                    if (!hasConnection(InternetActivity.this)) {

                        Intent intent = new Intent(InternetActivity.this, BottomNavigationActivity.class);
                        intent.putExtra("toast", "1");
                        startActivity(intent);
                        break;
                    }
                }
                finish();
            });
            t.start();

            //Получаем имя собесидника из ContactsFragment
            interlocutor_nic = getIntent().getStringExtra("current_interlocutor");
            chatId = getIntent().getStringExtra("current_chat");
            interlocutor_name = getIntent().getStringExtra("nameinterlocutor");

            Log.wtf("my_nic", my_nic);
            Log.wtf("chatId", chatId);
            Log.wtf("interlocutor_nic", interlocutor_nic);
            Log.wtf("interlocutor_name", interlocutor_name);

            //Делаем видимыми кнопку и EditText
            btn_edit_message.setVisibility(View.VISIBLE);
            get_message.setVisibility(View.VISIBLE);

            String s = "<b>" + interlocutor_name + "</b>";
            current_interlocutor.setText(Html.fromHtml(s));

            //Получение доступа к БД Firebase. Определение времени последнего посещения приложения собеседником.
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference last_time_userRef = database.getReference(secret_field + "/Online_Users/" + interlocutor_nic + "/" + interlocutor_nic);
            last_time_userRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        //Если данные - TimeStamp
                        try {
                            DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                            offsetRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat1 = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat3 = new SimpleDateFormat("HH:mm", Locale.getDefault());

                                    try {
                                        //Получаем TimeStamp (См. onStop у ContactsFragment) и переводим в дату.
                                        long date = dataSnapshot.getValue(Long.class);
                                        Date myDate = new Date(date);
                                        String last_time = dateFormat2.format(myDate);

                                        //Считываем отстройку по времени
                                        delay_time_long = snapshot.getValue(Long.class) + System.currentTimeMillis();

                                        Date todayDate = new Date(delay_time_long);
                                        Date yesterdayDate = new Date(delay_time_long - Long.parseLong("86400000"));
                                        Date dbyDate = new Date(delay_time_long - 2 * Long.parseLong("86400000"));
                                        String todaystr = dateFormat2.format(todayDate);
                                        todaystr = todaystr.substring(0, 8) + "000000";
                                        String yesterdaystr = dateFormat2.format(yesterdayDate);
                                        yesterdaystr = yesterdaystr.substring(0, 8) + "000000";
                                        String dbystr = dateFormat2.format(dbyDate);
                                        dbystr = dbystr.substring(0, 8) + "000000";

                                        if (Long.parseLong(last_time) >= Long.parseLong(todaystr)) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + "Был(а) в сети сегодня в " + dateFormat3.format(myDate);
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                        if (Long.parseLong(yesterdaystr) <= Long.parseLong(last_time) &&
                                                Long.parseLong(last_time) <= Long.parseLong(todaystr)) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + "Был(а) в сети вчера в " + dateFormat3.format(myDate);
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                        if (Long.parseLong(dbystr) <= Long.parseLong(last_time) &&
                                                Long.parseLong(last_time) <= Long.parseLong(yesterdaystr)) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + "Был(а) в сети позавчера в " + dateFormat3.format(myDate);
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                        if (Long.parseLong(last_time) <= Long.parseLong(dbystr)) {
                                            String s = "<b>" + interlocutor_name + "</b> <br>" + "Был(а) в сети в " + dateFormat1.format(myDate);
                                            current_interlocutor.setText(Html.fromHtml(s));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //Если данные - String
                        try {
                            //Вывод "онлайн"
                            String last_time = dataSnapshot.getValue(String.class);

                            if (chatId.equals("Chat_" + my_nic + "_" + my_nic)) {

                                listener_status = 0;
                                String s = "<b>Переписка с самим собой</b>";
                                current_interlocutor.setText(Html.fromHtml(s));

                            } else {
                                if (last_time.equals("online")) {
                                    String s = "<b>" + interlocutor_name + "</b> <br>" + "online";
                                    current_interlocutor.setText(Html.fromHtml(s));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String s = "<b>" + interlocutor_name + "</b>";
                        current_interlocutor.setText(Html.fromHtml(s));
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });

            //Получение доступа к БД Firebase. Установка разных путей.
            NOTIFICATION = database.getReference(secret_field + "/Notifications/" + interlocutor_nic);

            //Загружаем сообщения
            messages = new ArrayList<MyMessage>();
            //MESSAGES = new ArrayList<>();
            loadDataMessages(my_nic);

            //В памяти хранится все сообщения. Нас же интересуют только те, что относятся к данному чату.
            //Блок кода подгружает только те сообщения, которые нужны.
            List<MyMessage> help_array_messages = new ArrayList<MyMessage>(map.values());
            ArrayList<String> keys = new ArrayList<String>(map.keySet());
            Collections.sort(keys);
            /*for (Map.Entry entry : map.entrySet()) {
                Log.wtf("Вывод полностью map", "Key: " + entry.getKey() + " Value: " + entry.getValue());
            }*/
            for (int i = 0; i < keys.size(); i++) {
                help_array_messages.add(map.get(keys.get(i)));
                //Log.wtf("Соответствие map и MESSAGES", keys.get(i) + " - " + MESSAGES.get(i));
            }
            ArrayList<String> keys_after = new ArrayList<String>();
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i).contains(("_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nic))) {
                    Log.wtf("Вывод help_array_message", (help_array_messages.get(i)) + " (me_to_interlocutor) " + keys.get(i) + " " + i);
                    keys_after.add(keys.get(i));
                    messages.add(help_array_messages.get(i));
                } else if (keys.get(i).contains(("_(" + my_nic + ")_" + interlocutor_nic + "_" + my_nic))) {
                    Log.wtf("Вывод help_array_message", (help_array_messages.get(i)) + " (interlocutor_to_me) " + keys.get(i) + " " + i);
                    keys_after.add(keys.get(i));
                    messages.add(help_array_messages.get(i));
                }
            }
            MESSAGES.clear();
            for (int i = 0; i < keys_after.size(); i++) {
                MESSAGES.add(keys_after.get(i));
                Log.wtf("Вывод keys_after", keys_after.get(i));
            }

            //Обновляем adapter
            ListView messagesList = findViewById(R.id.messages_listview);
            //RecyclerView messagesList = findViewById(R.id.messages_listview);
            NewMessageAdapterMoreLays adapterMoreLays = new NewMessageAdapterMoreLays(InternetActivity.this, MESSAGES, messages, my_nic);

            messagesList.setAdapter(adapterMoreLays);
            adapterMoreLays.notifyDataSetChanged();
            messagesList.setSelection(keys_after.size());

            //Получение доступа к БД Firebase. Считываем публичный ключ собеседника, чтобы кодировать ему сообщение.
            final DatabaseReference key_public_Ref = database.getReference(secret_field + "/Internet_Messages/" + chatId + "/" + interlocutor_nic + "_key_public");
            key_public_Ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot11) {
                    if (!chatId.equals("Chat_" + my_nic + "_" + my_nic)) {

                            //При попытке отправки текста появляется кнопка "Начать общение" (Точнее говоря, на все той же кнопке меняется текст).
                            get_message.setVisibility(View.GONE);

                            //Отключение клавиатуры
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(btn_edit_message.getWindowToken(), 0);

                            //Появление Snackbar, предложение запроса.
                            Snackbar snackbar = Snackbar.make(btn_edit_message, "Пользователь еще не добавил вас в список контактов", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Запрос", new View.OnClickListener() { //Пользователь хочет общаться с собеседника
                                @Override
                                public void onClick(View v) {

                                    //Получение доступа к БД Firebase. Отправка запроса пользователю на добавление в список контактов.
                                    REQUEST = database.getReference(secret_field + "/Request/" + interlocutor_nic);
                                    InternetActivity.this.REQUEST.push().setValue(my_nic);

                                    //Toast.makeText(getApplicationContext(), interlocutor_name + " получит ваше приглашение", Toast.LENGTH_SHORT).show();
                                    btn_edit_message.setVisibility(View.GONE);
                                    get_message.setFocusable(false);

                                    Intent intent = new Intent(InternetActivity.this, BottomNavigationActivity.class);
                                    startActivity(intent);
                                }
                            });
                            snackbar.setTextColor(0XFFFFFFFF);
                            snackbar.setBackgroundTint(0XFF31708E);
                            snackbar.setActionTextColor(0XFFFFFFFF);
                            snackbar.show();
                        }
                    }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                }
            });
            //Обнуляем EditText
            get_message.setText("");

            //Установление слушателя событий нажатия кнопки "Отправить"
            btn_edit_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //При отсутствиея доступа к интернету приложение для дальнейшей работы рекомендует подключиться к сети
                    if (!hasConnection(InternetActivity.this)) {
                        Toast.makeText(InternetActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(InternetActivity.this, BottomNavigationActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        //Получаем сообщение, которое хочет отправить пользователь, от EditText
                        sent_message = get_message.getText().toString();

                        //Отладка
                        //Log.wtf("my_name", my_name);
                        //Log.wtf("interlocutor_name", interlocutor_name);
                        //Log.wtf("chatId", chatId);

                        //Очищаем переменные от лишнего мусора - пробелов в начале и конце
                        ReplaceRepeat replaceRepeat = new ReplaceRepeat();
                        sent_message = replaceRepeat.ReplaceRepeatStr(sent_message);

                        //Проверяем, не пустое ли сообщение (в различных конфигурациях)
                        if (sent_message.equals("") ||
                                sent_message.equals(" ") ||
                                sent_message.equals("/n") ||
                                (sent_message.length() > max_message_length)) {

                            //Если сообщение слишком большое, пользователь об этом до недавнего времени просто уведомлялся.
                            //Однако теперь в приложении невозможно написать текст длиною более, чем max_message_length = 2500 символов
                            if (sent_message.length() > max_message_length) {
                                Toast.makeText(getApplicationContext(),
                                        "Слишком большое (либо пустое) сообщение", Toast.LENGTH_SHORT).show();
                            }
                        } else { //Если сообщение не пустое

                            //Получение доступа к БД Firebase. Считываем отстройку времени между сервером Firebase и физустройством.
                            DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                            offsetRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    //Считываем отстройку по времени
                                    delay_time_long = snapshot.getValue(Long.class);

                                    //Вызываем экземпляр класса CurrentTime, передаем отстройку, получаем текущее время в необходимом формате.
                                    CurrentTime currentTime = new CurrentTime();
                                    String currenttime = currentTime.getCurrentTimeFromBase(delay_time_long);

                                    //Разбираем строку, преобразовываем в смотрибельный вид
                                    String get_time_To_look =
                                            currenttime.substring(8, 10) + "." +
                                                    currenttime.substring(10, 12) + "\n" +
                                                    currenttime.substring(6, 8) + "." +
                                                    currenttime.substring(4, 6) + "." +
                                                    currenttime.substring(0, 4);

                                    //Получение доступа к БД Firebase. Считываем публичный ключ собеседника, чтобы кодировать ему сообщение.
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    final DatabaseReference key_public_Ref = database.getReference(secret_field + "/Internet_Messages/" + chatId + "/" + interlocutor_nic + "_key_public");
                                    key_public_Ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NotNull DataSnapshot dataSnapshot11) {
                                            if (!dataSnapshot11.exists()) { //Если публичного ключа собеседника в базе данных нет. Это свидетельствует о том, что он нас в список контактов еще не добавил.
                                                if (!chatId.equals("Chat_" + my_nic + "_" + my_nic)) {

                                                    //При попытке отправки текста появляется кнопка "Начать общение" (Точнее говоря, на все той же кнопке меняется текст).
                                                    get_message.setVisibility(View.GONE);

                                                    //Отключение клавиатуры
                                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                                                    //Появление Snackbar, предложение запроса.
                                                    Snackbar snackbar = Snackbar.make(v, "Пользователь еще не добавил вас в список контактов", Snackbar.LENGTH_INDEFINITE);
                                                    snackbar.setAction("Запрос", new View.OnClickListener() { //Пользователь хочет общаться с собеседника
                                                        @Override
                                                        public void onClick(View v) {

                                                            //Получение доступа к БД Firebase. Отправка запроса пользователю на добавление в список контактов.
                                                            REQUEST = database.getReference(secret_field + "/Request/" + interlocutor_nic);
                                                            InternetActivity.this.REQUEST.push().setValue(my_nic);

                                                            //Toast.makeText(getApplicationContext(), interlocutor_name + " получит ваше приглашение", Toast.LENGTH_SHORT).show();
                                                            btn_edit_message.setVisibility(View.GONE);
                                                            get_message.setFocusable(false);

                                                            Intent intent = new Intent(InternetActivity.this, BottomNavigationActivity.class);
                                                            startActivity(intent);
                                                        }
                                                    });
                                                    snackbar.setTextColor(0XFFFFFFFF);
                                                    snackbar.setBackgroundTint(0XFF31708E);
                                                    snackbar.setActionTextColor(0XFFFFFFFF);
                                                    snackbar.show();

                                                } else {
                                                    //Добавление сообщения и обновление адаптера. Разумеется, в чистом, некодированном, виде.
                                                    messages.add(new MyMessage(my_name, sent_message, get_time_To_look));
                                                    MESSAGES.add(currenttime + "_(" + my_nic + ")_" + my_nic + "_" + my_nic);
                                                    map.put(currenttime + "_(" + my_nic + ")_" + my_nic + "_" + my_nic, new MyMessage(my_name, sent_message, get_time_To_look));
                                                    saveDataMessages(my_nic, InternetActivity.this);
                                                    adapterMoreLays.notifyDataSetChanged();
                                                    messagesList.smoothScrollToPosition(MESSAGES.size());
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                        }
                                    });
                                    //Обнуляем EditText
                                    get_message.setText("");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    }
                }
            });

            messagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    MyMessage message = adapterMoreLays.getItem(position);

                    /*//Появление Snackbar, предложение запроса.
                    Snackbar snackbar = Snackbar.make(view, "Скопировать текст сообщения?", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("Да", new View.OnClickListener() { //Пользователь хочет общаться с собеседника
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(),
                                    "Текст скопирован", Toast.LENGTH_SHORT).show();

                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("", message.getText());
                            clipboard.setPrimaryClip(clip);
                        }
                    });
                    snackbar.setTextColor(0XFFFFFFFF);
                    snackbar.setBackgroundTint(0XFF31708E);
                    snackbar.setActionTextColor(0XFFFFFFFF);
                    snackbar.show();*/
                    current_interlocutor.setVisibility(View.GONE);
                    linlay_extra.setVisibility(View.VISIBLE);

                    top_cross.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            current_interlocutor.setVisibility(View.VISIBLE);
                            linlay_extra.setVisibility(View.GONE);
                        }
                    });
                    top_copy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            current_interlocutor.setVisibility(View.VISIBLE);
                            linlay_extra.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),
                                    "Текст скопирован", Toast.LENGTH_SHORT).show();
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("", message.getText());
                            clipboard.setPrimaryClip(clip);
                        }
                    });
                    top_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            current_interlocutor.setVisibility(View.VISIBLE);
                            linlay_extra.setVisibility(View.GONE);

                            AlertDialog.Builder alert = new AlertDialog.Builder(InternetActivity.this);
                            alert.setTitle("Удаление");
                            alert.setMessage("Вы точно хотите удалить данное сообщение без возможности восстановления? У вашего собеседника данное сообщение останется");
                            alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    MyMessage delete_message = adapterMoreLays.getItem(position);

                                    Log.wtf("removemsg", String.valueOf(delete_message));
                                    messages.remove(delete_message);
                                    MESSAGES.remove(position);

                                    ArrayList<String> keys = new ArrayList<String>(map.keySet());
                                    for (int i = 0; i < keys.size(); i++) {
                                        if (map.get(keys.get(i)) == delete_message) {
                                            map.remove(keys.get(i));
                                            Log.wtf("msg", String.valueOf(keys.get(i)));
                                        }
                                    }

                                    adapterMoreLays.notifyDataSetChanged();
                                    adapterMoreLays.notifyDataSetInvalidated();
                                    saveDataMessages(my_nic, InternetActivity.this);
                                }
                            });
                            alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alert.show();
                        }
                    });
                }
            });

            //Установление слушателя сообщений, следящего за добавлением новых позиций собеседником
            INTERLOCUTOR = database.getReference(secret_field + "/Internet_Messages/" + "/" + chatId + "/" + interlocutor_nic);
            childEventListener1 = new ChildEventListener() {
                @Override
                public void onChildAdded(@NotNull DataSnapshot datasnapshot, String previousChildName) {

                    //Получаем сообщение
                    String get_message = datasnapshot.getValue(String.class);
                    //Log.i("location(InternetActivity)", get_message);

                    assert get_message != null;
                    int index_left = get_message.indexOf("<");
                    mkey = get_message.substring(0, index_left);
                    get_message = get_message.substring(index_left + 1);

                    //Разбираем строку, получаем время отправки и зашифрованный текст
                    String get_time = get_message.substring(0, 14);
                    get_message = get_message.substring(14);
                    //Log.wtf("decode_message", get_time + " " + get_message);

                    try {
                        //Пробуем получить приватный ключ из памяти физустройства по id чата
                        PrivateKey privateKey = getPrivateKey(chatId, my_nic);

                        //Обозначаем, что с помощью данного ключа будем декодировать полученное сообщение
                        Cipher rsa = Cipher.getInstance("RSA");
                        rsa.init(Cipher.DECRYPT_MODE, privateKey);

                        //Преобразовываем сообщение в массив байт
                        //byte[] encrypt_sent_message = Base64.getDecoder().decode(get_message);
                        byte[] encrypt_sent_message = Base64.getDecoder().decode(get_message);
                        byte[] utf8 = rsa.doFinal(encrypt_sent_message);

                        //Декодируем сообщение
                        get_message = new String(utf8, "UTF8");
                        //Log.wtf("get_message)", get_message);

                        //Разбираем строку, получаем дату и время в удобном для представления формате
                        String get_time_To_look = get_time.substring(8, 10) + "." + get_time.substring(10, 12) + "\n" +
                                get_time.substring(6, 8) + "." + get_time.substring(4, 6) + "." + get_time.substring(0, 4);

                        //Log.wtf("get_time_To_look", get_time_To_look);

                        if (listener_status != 0) {

                            //Если приложение не остановлено
                            //Отображаем сообщение
                            messages.add(new MyMessage(interlocutor_name, get_message, get_time_To_look));
                            MESSAGES.add(get_time + "_(" + my_nic + ")_" + interlocutor_nic + "_" + my_nic);
                            //map.put(get_time + "_" + interlocutor_name + "_" + my_name, new MyMessage(interlocutor_name, get_message, get_time_To_look));

                            //Обновляем адаптер
                            adapterMoreLays.notifyDataSetChanged();
                            messagesList.smoothScrollToPosition(MESSAGES.size());

                            //Получение доступа к БД Firebase. Удаление полученного сообщения с БД.
                            DatabaseReference MessageRemove = FirebaseDatabase.getInstance().getReference(secret_field + "/Internet_Messages/");
                            remove_message_listener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                                        //Log.wtf("location(InternetActivity)", String.valueOf(postsnapshot));
                                        dataSnapshot.getRef().child(mkey).removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            };
                            MessageRemove.child(chatId).child(interlocutor_nic).orderByKey().addListenerForSingleValueEvent(remove_message_listener);

                            //Добавляем сообщение в map и сохраняем сообщения
                            map.put(get_time + "_(" + my_nic + ")_" + interlocutor_nic + "_" + my_nic, new MyMessage(interlocutor_name, get_message, get_time_To_look));

                            saveDataMessages(my_nic, InternetActivity.this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            };
            INTERLOCUTOR.addChildEventListener(childEventListener1);

        } catch (Exception e) {
            e.printStackTrace();
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

    //Метод, сохраняющий сообщения
    public static void saveDataMessages(String my_nic, Context context) {

        //Данные загружаются с использованием SharedPreferences. Хранятся в формате Json.
        //Сохраняем Map<String, MyMessage> map, ArrayList<String> MESSAGES.
        /*SharedPreferences sharedPreferences = this.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(messages);
        if (json != null) {
            json = json.substring(1, json.length() - 1);
        }
        editor.putString("taskm", json);
        editor.apply();*/

        SharedPreferences sharedPreferencesarray = context.getSharedPreferences("sharedPreferencesarray" + my_nic, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarray", 0).edit().clear().apply();
        SharedPreferences.Editor editorarray = sharedPreferencesarray.edit();
        Gson gsonarray = new Gson();
        String jsonarray = gsonarray.toJson(MESSAGES);
        if (jsonarray != null) {
            jsonarray = jsonarray.substring(1, jsonarray.length() - 1);
        }
        editorarray.putString("taskarray" + my_nic, jsonarray);
        editorarray.apply();

        SharedPreferences sharedPreferencesarrayall = context.getSharedPreferences("sharedPreferencesarrayall" + my_nic, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarrayall", 0).edit().clear().apply();
        SharedPreferences.Editor editorarrayall = sharedPreferencesarrayall.edit();
        Gson gsonarrayall = new Gson();
        String jsonarrayall = gsonarrayall.toJson(map);
        if (jsonarrayall != null) {
            jsonarrayall = jsonarrayall.substring(1, jsonarrayall.length() - 1);
        }
        editorarrayall.putString("taskarrayall" + my_nic, jsonarrayall);
        editorarrayall.apply();

        //Log.wtf("messages json", json);
        //Log.wtf("MESSAGES json", jsonarray);
        //Log.wtf("map json", jsonarrayall);
    }

    //Метод, загружающий сообщения
    public void loadDataMessages(String my_nic) {

        //Данные берутся из SharedPreferences. Хранятся в формате Json.
        //Загружаем Map<String, MyMessage> map, ArrayList<String> MESSAGES.

        SharedPreferences sharedPreferencesarray = this.getSharedPreferences("sharedPreferencesarray" + my_nic, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarray", 0).edit().clear().apply();
        Gson gsonarray = new Gson();
        String jsonarray = sharedPreferencesarray.getString("taskarray" + my_nic, null);
        if (jsonarray != null) {
            jsonarray = "[" + jsonarray + "]";
        }
        Type typearray = new TypeToken<ArrayList<String>>() {
        }.getType();
        MESSAGES = gsonarray.fromJson(jsonarray, typearray);
        if (MESSAGES == null) {
            MESSAGES = new ArrayList<>();
        }

        SharedPreferences sharedPreferencesarrayall = this.getSharedPreferences("sharedPreferencesarrayall" + my_nic, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarrayall", 0).edit().clear().apply();
        Gson gsonarrayall = new Gson();
        String jsonarrayall = sharedPreferencesarrayall.getString("taskarrayall" + my_nic, null);
        if (jsonarrayall != null) {
            jsonarrayall = "{" + jsonarrayall + "}";
        }
        Type typearrayall = new TypeToken<TreeMap<String, MyMessage>>() {
        }.getType();
        map = gsonarrayall.fromJson(jsonarrayall, typearrayall);
        if (map == null) {
            map = (Map<String, MyMessage>) new TreeMap<String, MyMessage>();
        }

        //Log.wtf("messages json", json);
        //Log.wtf("MESSAGES json", jsonarray);
        //Log.wtf("map json", jsonarrayall);
    }

    //Метод, вызывающийся при остановке приложения. В нашем случае он также вызывает finish();
    @Override
    public void onStop() {
        super.onStop();

        //DatabaseReference MessageRemove = FirebaseDatabase.getInstance().getReference(secret_field+"/Internet_Messages/");
        //MessageRemove.removeEventListener(remove_message_listener);

        //Обнуление всех слушателей, ибо активность закрывается
        listener_status = 0;

        if (INTERLOCUTOR != null && childEventListener1 != null) {
            INTERLOCUTOR.removeEventListener(childEventListener1);
        }

        finish();
    }

    //Метод, получающий секретный ключ из памяти устройства
    private PrivateKey getPrivateKey(String chatId, String my_nic) throws
            NoSuchAlgorithmException, InvalidKeySpecException /*throws Exception*/ {

        //Получающем ключ через SharedPreferences
        SharedPreferences sharedPreference_privateKey = this.getSharedPreferences("sharedPreference_privateKey" + my_nic, Context.MODE_PRIVATE);
        Gson gson_privateKey = new Gson();
        String json_privateKey = sharedPreference_privateKey.getString("task_privateKey" + my_nic, null);

        //Для сохранения в формате Json необходимо соблюсте определенные требования.
        //К строке с двух сторон добавляются фигурные скобочки
        if (json_privateKey != null) {
            json_privateKey = "{" + json_privateKey + "}";
        }

        Type type_privateKey = new TypeToken<TreeMap<String, String>>() {
        }.getType();
        Map<String, String> map = gson_privateKey.fromJson(json_privateKey, type_privateKey);

        //В памяти хранятся ключи для каждого диалога.
        //Получаем секретный ключ из map, необходимый для общения с конкретным собеседником.
        String str_key = "";
        List<String> help_array_messages;
        if (map != null) {
            help_array_messages = new ArrayList<>(map.values());
            List<String> keys;
            keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i).equals(chatId)) {
                    str_key = help_array_messages.get(i);
                    //.wtf("str_key", str_key);
                }
            }
        }

        //Преобразуем ключ в массив байт
        byte[] privateKeyBytes = Base64.getDecoder().decode(str_key);
        //Log.wtf("pvt", "BYTE KEY" + Arrays.toString(privateKeyBytes));
        //Log.wtf("pvt", "FINAL OUTPUT" + Arrays.toString(privateKeyBytes));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privatekey = keyFactory.generatePrivate(privateKeySpec);

        privateKeyBytes = privatekey.getEncoded();
        //Log.wtf("privatekey (byte) e/d", Arrays.toString(privateKeyBytes));

        //Возвращаем ключ сразу в формате "PrivateKey"
        return privatekey;
    }
}