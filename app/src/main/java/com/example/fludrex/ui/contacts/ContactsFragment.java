package com.example.fludrex.ui.contacts;

//Есть разница между двумя вариантами кодирования через Base64. Для версий
//android < 8 шифрование не работает должным образом. Пришлось поднять требования,
//с android версии 8.0 (Oreo) шифрование работает корректно.
//В коде можно посмотреть разницу между реализациями каждого варианта.

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
//!
//import android.util.Base64;
import java.util.Base64;
//!
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fludrex.BottomNavigationActivity;
import com.example.fludrex.InternetActivity;
import com.example.fludrex.MyContacts;
import com.example.fludrex.MyMessage;
import com.example.fludrex.NewContactsAdapter;
import com.example.fludrex.R;
import com.example.fludrex.RegistrationActivity;
import com.example.fludrex.ReplaceRepeat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.ContentValues.TAG;
import static android.content.Context.ACTIVITY_SERVICE;

/*
    Всего в приложении используются 3 фрагмента. Все они прикреплены к BottomNavigationActivity.
    ContactsFragment - один из них. Отвечает за поиск и хранение контаков, отображение данных о них, переход к диалогу.
    Доступен по нажатию на соответствующую иконку в BottomNavigation.

    Имеет кнопку "Искать", два EditText для ввода имени и фамилии, отображает список найденных пользователей
    при помощи ListView, присылает запрос от пользователя.
    (См. разметку)
*/

public class ContactsFragment extends Fragment {

    EditText find_username1;
    Button btn_find_user1;
    LinearLayout linlay_bar, linlay_list, linlay_btn;
    ListView listView;

    public String user_nic;
    ArrayList<String> CONTACTS;
    ArrayList<String> CHAT_NUMBER;
    ArrayList<MyContacts> contacts;

    public String name;
    public String my_name;
    public String my_password;
    public String my_nic;
    public String my_email;
    public int flag;
    public int FLAG;
    public String chatId;

    private final String file_name = "file_username";
    private final String file_password = "file_password";
    private final String file_email = "file_email";
    private final String file_nic = "file_nic";

    public int my_version = 5;  //!!!
    public int last_version;
    public String status;

    SwipeRefreshLayout mySwipeRefreshLayout;

    Toast toast;

    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    public FirebaseUser currentUser;
    DatabaseReference CAPABLE;
    DatabaseReference VERSION;
    DatabaseReference CHAT_users;
    DatabaseReference LISTEN_REQUEST;

    private String secret_field;

    Map<String, MyMessage> map;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        //Возвращение объекта класса View по id из разметки
        find_username1 = rootView.findViewById(R.id.find_username1);
        btn_find_user1 = rootView.findViewById(R.id.btn_find_user1);
        linlay_bar = rootView.findViewById(R.id.linlay_bar);
        linlay_btn = rootView.findViewById(R.id.linlay_btn);
        linlay_list = rootView.findViewById(R.id.linlay_list);
        mySwipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        listView = rootView.findViewById(R.id.contacts_listview1);

        btn_find_user1.setBackgroundResource(R.drawable.btn_selector);

        //Всего используются 3 linlay. Так как последующая большАя часть кода требует некоторого количества времени для
        //получения всех данных с БД, читки файлов и отображения всех компонентов, в начале пользователь видит загрузку:
        //отображается разметка с circular progress bar. Впоследствии отображается главный экран.
        linlay_bar.setVisibility(View.VISIBLE);
        linlay_list.setVisibility(View.GONE);
        linlay_btn.setVisibility(View.GONE);

        if (hasConnection(requireActivity())) {

            //При отсутствиея доступа к интернету приложение для дальнейшей работы рекомендует подключиться к сети
            Intent intent = new Intent(requireActivity(), BottomNavigationActivity.class);
            intent.putExtra("toast", "1");
            startActivity(intent);
            requireActivity().finish();

        } else {

            //Чтение из файлов данных пользователя и БД
            try {
                BufferedReader br_nn = new BufferedReader(new InputStreamReader(requireActivity().openFileInput(file_nic)));
                my_nic = br_nn.readLine();
                BufferedReader br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput(file_name + my_nic)));
                my_name = br_n.readLine();
                BufferedReader br_p = new BufferedReader(new InputStreamReader(requireActivity().openFileInput(file_password + my_nic)));
                my_password = br_p.readLine();
                BufferedReader br_e = new BufferedReader(new InputStreamReader(requireActivity().openFileInput(file_email + my_nic)));
                my_email = br_e.readLine();

                BufferedReader br_f = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_secret_field")));
                secret_field = br_f.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (my_email == null || my_name == null || my_password == null || my_nic == null) { //если данные некорректны для авторизации

                //Открытие RegistrationActivity и закрытие BottomNavigationActivity
                Intent intent = new Intent(requireActivity(), RegistrationActivity.class);
                startActivity(intent);
                requireActivity().finish();

            } else {

                //Получение экземпляра FirebaseAuth (аутентификация), метод signIn
                mAuth = FirebaseAuth.getInstance();
                signIn(my_email, my_password);

                //Если авторизация прошла успешно, мы получаем текущего пользователя.
                //Ставим статус - пользователь "онлайн".
                currentUser = mAuth.getCurrentUser();

                //Получение доступа к БД Firebase. Определяем статус БД.
                CAPABLE = database.getReference(secret_field + "/Status/Capable");
                final ChildEventListener childEventListener1 = CAPABLE.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                        if (datasnapshot.exists()) {

                            //Получаем статус
                            status = datasnapshot.getValue(String.class);
                            Log.wtf("Capable", status);

                            //Если на сервере техн. режим, пользователю запрещается доступ к приложению
                            if (status.equals("NO")) {
                                Toast.makeText(requireActivity(),
                                        "Ведутся работы, приложение временно недоступно", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(requireActivity(), BottomNavigationActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            }
                            if (status.equals("YES")) {

                                //Если работы на сервере не ведутся

                                //Получение доступа к БД Firebase. Определяем последнюю версию приложения.
                                VERSION = database.getReference(secret_field + "/Status/Version");
                                final ChildEventListener childEventListener2 = VERSION.addChildEventListener(new ChildEventListener() {
                                    @SuppressLint("ResourceType")
                                    @Override
                                    public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {

                                        //Получаем последнюю версию
                                        last_version = datasnapshot.getValue(Integer.class);
                                        Log.wtf("Version", String.valueOf(last_version));

                                        //Данные массивы нужны просто для корретного отображения информация через Toast.
                                        String[] versionnumber = {" ", "(первая) ", "(вторая) ", "(третья) ", "(четвертая) ", "(пятая) "};
                                        String[] versionnumber2 = {"", "первой", "второй", "третьей", "четвертой", "пятой"};

                                        if (last_version != my_version) {

                                            //Если версия не последняя, пользователь об этом уведомляется. Доступ запрещается
                                            //в связи с возможными проблемами при несовместимости версий
                                            if (last_version < versionnumber2.length) {
                                                Toast.makeText(requireActivity(),
                                                        "Неактуальная " + versionnumber[my_version] + "версия приложения. " +
                                                                "Требуется обновление до " + versionnumber2[last_version], Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(requireActivity(), BottomNavigationActivity.class);
                                                startActivity(intent);
                                                requireActivity().finish();
                                            } else {
                                                Toast.makeText(requireActivity(),
                                                        "Неактуальная версия приложения. Требуется обновление", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(requireActivity(), BottomNavigationActivity.class);
                                                startActivity(intent);
                                                requireActivity().finish();
                                            }
                                        } else { //Если версия актуальная

                                            //загружаем сохраненные данные о контактах
                                            loadDataContacts(my_nic);
                                            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                            //contacts = new ArrayList<>();
                                            //CONTACTS = new ArrayList<>();

                                            ArrayList<MyMessage> messages = new ArrayList<MyMessage>();
                                            //MESSAGES = new ArrayList<>();
                                            loadDataMessages(my_nic);

                                            String interlocutor_nic = "67889";

                                            //В памяти хранится все сообщения. Нас же интересуют только те, что относятся к данному чату.
                                            //Блок кода подгружает только те сообщения, которые нужны.
                                            List<MyMessage> help_array_messages = new ArrayList<MyMessage>(map.values());
                                            ArrayList<String> keys = new ArrayList<String>(map.keySet());
                                            Collections.sort(keys);
                                            for (int i = 0; i < keys.size(); i++) {
                                                help_array_messages.add(map.get(keys.get(i)));
                                            }
                                            ArrayList<String> keys_after = new ArrayList<String>();
                                            for (int i = 0; i < keys.size(); i++) {
                                                if (keys.get(i).contains(("_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nic))) {
                                                    keys_after.add(keys.get(i));
                                                    messages.add(help_array_messages.get(i));
                                                } else if (keys.get(i).contains(("_(" + my_nic + ")_" + interlocutor_nic + "_" + my_nic))) {
                                                    keys_after.add(keys.get(i));
                                                    messages.add(help_array_messages.get(i));
                                                }
                                            }

                                            if (keys_after.size() != 0) {
                                                MyMessage a = map.get(keys_after.get(keys_after.size() - 1));
                                                if (a != null) {
                                                    String b = a.getText();
                                                }
                                            }

                                            //Установление адаптера для вывода информации о контактах
                                            NewContactsAdapter adapter = null;
                                            try {
                                                //adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item, contacts, CONTACTS);
                                                adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item_new, contacts, CONTACTS);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            listView.setAdapter(adapter);
                                            if (adapter != null) {
                                                adapter.notifyDataSetChanged();
                                            }

                                            //А вот теперь убираем разметку загрузки, отображавщуюся все это время, и начинаем демонстрировать главный экран
                                            linlay_bar.setVisibility(View.GONE);
                                            linlay_list.setVisibility(View.VISIBLE);
                                            linlay_btn.setVisibility(View.VISIBLE);

                                            LISTEN_REQUEST = database.getReference(secret_field + "/Request/" + my_nic);
                                            NewContactsAdapter finalAdapter2 = adapter;
                                            final ChildEventListener childEventListener = LISTEN_REQUEST.addChildEventListener(new ChildEventListener() {
                                                @RequiresApi(api = Build.VERSION_CODES.O)
                                                @Override
                                                public void onChildAdded(@NotNull DataSnapshot datasnapshotLR, String previousChildName) {

                                                    String new_user_nic = datasnapshotLR.getValue(String.class);
                                                    Log.wtf("заявки", "новая заявка - " + new_user_nic);

                                                    try {
                                                        DatabaseReference IsThisNick = FirebaseDatabase.getInstance().getReference(secret_field + "/Contacts/" + new_user_nic);
                                                        IsThisNick.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    String new_user_name = snapshot.getValue(String.class);
                                                                    Log.wtf("заявки", "новая заявка - " + new_user_name);

                                                                    //Получаем почту
                                                                    //Создание предупреждения, уточняем намерения пользователя
                                                                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                                                    alert.setTitle("Новый запрос");
                                                                    alert.setMessage("Пользователь " + new_user_name + " (под ником " + new_user_nic + ") " +
                                                                            "желает добавить вас в свой список контактов. Принять запрос?");
                                                                    alert.setPositiveButton("Принять", new DialogInterface.OnClickListener() { //Принятие приглашения
                                                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {

                                                                            if (!CONTACTS.contains(new_user_nic)) { //Если в списке контактов такого пользователя нет

                                                                                //Добавление в список контактов, обновление адаптера
                                                                                contacts.add(new MyContacts(new_user_name, "offline"));
                                                                                CONTACTS.add(new_user_nic);
                                                                                finalAdapter2.notifyDataSetChanged();

                                                                                //Составление id чата (подробнее - ниже при самосотоятельном добавлении), создание публичного ключа, отправление его в БД, сохранение
                                                                                chatId = "Chat_" + new_user_nic + "_" + my_nic;
                                                                                //chatId = "Chat_" + new_user_nic + "_" + my_name;
                                                                                CHAT_NUMBER.add(chatId);
                                                                                CHAT_users = database.getReference(secret_field + "/Internet_Messages/" + chatId);
                                                                                generateKeys("RSA", 2048, CHAT_users, chatId, my_name, my_nic);
                                                                                saveDataContacts(my_nic);
                                                                                Log.wtf(TAG, String.valueOf(CHAT_NUMBER));

                                                                                //Обновление адаптера
                                                                                ListView listView = rootView.findViewById(R.id.contacts_listview1);
                                                                                listView.setAdapter(finalAdapter2);
                                                                                finalAdapter2.notifyDataSetChanged();

                                                                            } else { //Если данный пользователь уже есть в списке контактов
                                                                                Toast.makeText(getActivity(), "Пользователь уже есть в вашем списке", Toast.LENGTH_SHORT).show();
                                                                            }

                                                                            removerequest(secret_field, my_nic);
                                                                        }
                                                                    });
                                                                    alert.setNegativeButton("Отклонить", new DialogInterface.OnClickListener() { //Отказ
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {

                                                                            removerequest(secret_field, my_nic);
                                                                            dialog.dismiss();
                                                                        }
                                                                    });
                                                                    alert.show();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
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

                                            //Установление слушателя событий нажатия кнопки "Искать"
                                            NewContactsAdapter finalAdapter = adapter;
                                            btn_find_user1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    try {
                                                        if (hasConnection(requireActivity())) { //Если интернета нет, найти через него пользователя не получится
                                                            Toast.makeText(requireActivity(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                                                        } else {

                                                            //Временно отключаем кнопку, чтобы при множественном нажатии не добавлялись тысячи одинаковых контактов,
                                                            //ссылающихся на один и тот же чат. Спасибо Никите Козорезу за нахождение сей ошибки.
                                                            btn_find_user1.setClickable(false);

                                                            //Получаем имя, фамилию от Edittext
                                                            user_nic = find_username1.getText().toString();
                                                            //user_email = find_useremail1.getText().toString();

                                                            //Очищаем переменные от лишнего мусора - пробелов в начале и конце
                                                            ReplaceRepeat replaceRepeat = new ReplaceRepeat();
                                                            //user_email = replaceRepeat.ReplaceRepeatStr(user_email);
                                                            user_nic = replaceRepeat.ReplaceRepeatStr(user_nic);

                                                            Log.wtf("usernic", user_nic);
                                                            Log.wtf("mynic", my_nic);

                                                            if (//!user_email.equals("") &&
                                                                //!user_email.equals(" ") &&
                                                                //!user_email.equals("/n") &&
                                                                    !user_nic.equals("") &&
                                                                            !user_nic.equals(" ") &&
                                                                            !user_nic.equals("/n")) { //Если поле не пустое (в различных конфигарациях)

                                                                if (user_nic.equals(my_nic)) { //Себя добавить в список контактов нельзя
                                                                    if (!CONTACTS.contains(user_nic)) {

                                                                        //Появление Snackbar
                                                                        Snackbar snackbar = Snackbar.make(v, "Здесь можно хранить приватную информацию", Snackbar.LENGTH_INDEFINITE);
                                                                        snackbar.setAction("Понятно", new View.OnClickListener() { //Пользователь хочет общаться с собеседника
                                                                            @Override
                                                                            public void onClick(View v) {

                                                                                //Добавление в спиок контактов, поднятие флажка
                                                                                contacts.add(new MyContacts(my_name, "itsme"));
                                                                                CONTACTS.add(user_nic);
                                                                                finalAdapter.notifyDataSetChanged();
                                                                                flag = 1;
                                                                                find_username1.setText("");
                                                                                //Добавляем в массив, создаем key pair, сохраняем.
                                                                                CHAT_NUMBER.add("Chat_" + my_nic + "_" + my_nic);
                                                                                saveDataContacts(my_nic);
                                                                                Log.wtf(TAG, String.valueOf(CHAT_NUMBER));
                                                                            }
                                                                        });
                                                                        snackbar.setTextColor(0XFFFFFFFF);
                                                                        snackbar.setBackgroundTint(0XFF31708E);
                                                                        snackbar.setActionTextColor(0XFFFFFFFF);
                                                                        snackbar.show();

                                                                    } else {
                                                                        Toast.makeText(getActivity(), "Вами уже была создана переписка с самим собой", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    btn_find_user1.setClickable(true);
                                                                    //Toast.makeText(getActivity(), "Невозможно добавить себя в список контактов", Toast.LENGTH_SHORT).show();

                                                                } else {

                                                                    //Если такого контакто еще нет
                                                                    if (!CONTACTS.contains(user_nic)) {
                                                                        flag = 0;
                                                                        FLAG = 0;

                                                                        //Получение доступа к БД Firebase. Проверяем, есть ли такой зарегестрированный пользователь в БД.
                                                                        DatabaseReference UserSearch = FirebaseDatabase.getInstance().getReference(secret_field + "/Contacts/" + user_nic);
                                                                        UserSearch.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot data_Snapshot) {

                                                                                //Получение его email-адреса. Данные хранятся в таком формате: Артем Романович: "artrom170@gmail.com".
                                                                                //Поэтому находим по имени и смотрим, как соотносятся почта БД с Edittext-ом.
                                                                                String get_name_from_base = data_Snapshot.getValue(String.class);

                                                                                if (data_Snapshot.exists()) {

                                                                                    //Если адреса совпадают
                                                                                    //if (user_email.equals(get_email_from_base)) {
                                                                                    String status = "offline";

                                                                                    //Добавление в спиок контактов, поднятие флажка
                                                                                    contacts.add(new MyContacts(get_name_from_base, status));
                                                                                    CONTACTS.add(user_nic);
                                                                                    finalAdapter.notifyDataSetChanged();
                                                                                    flag = 1;
                                                                                    find_username1.setText("");

                                                                                    //Получение доступа к БД Firebase. Ищем чат.
                                                                                    chatId = "Chat_" + my_nic + "_" + user_nic;
                                                                                    DatabaseReference ChatSearch1 = FirebaseDatabase.getInstance().getReference(secret_field + "/Internet_Messages");
                                                                                    DatabaseReference ChatSearchRef = ChatSearch1.child("Chat_" + user_nic + "_" + my_nic);
                                                                                    ValueEventListener eventListener = new ValueEventListener() {
                                                                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                            if (!dataSnapshot.exists()) { //Если чата нет (мы первые нашли пользователя) - создаем
                                                                                                CHAT_users = database.getReference(secret_field + "/Internet_Messages/" + chatId);
                                                                                                //ContactsFragment.this.CHAT_users.push().setValue(user_nic);
                                                                                                //ContactsFragment.this.CHAT_users.push().setValue(my_nic);
                                                                                                chatId = "Chat_" + my_nic + "_" + user_nic;
                                                                                            } else { //Если чат уже существует (наш собеседник реньше нас его создал) - получаем его id
                                                                                                chatId = "Chat_" + user_nic + "_" + my_nic;
                                                                                            }
                                                                                            //Добавляем в массив, создаем key pair, сохраняем.
                                                                                            CHAT_NUMBER.add(chatId);
                                                                                            generateKeys("RSA", 2048, CHAT_users, chatId, my_name, my_nic);
                                                                                            saveDataContacts(my_nic);
                                                                                            Log.wtf(TAG, String.valueOf(CHAT_NUMBER));
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {
                                                                                        }
                                                                                    };
                                                                                    ChatSearchRef.addListenerForSingleValueEvent(eventListener);
                                                                                    //}
                                                                                }

                                                                                if (flag == 0) { //По состоянию флага уведомляем пользователя об успехе операции
                                                                                    Toast.makeText(getActivity(), "Пользователь с ником \"" + user_nic + "\" не найден", Toast.LENGTH_SHORT).show();
                                                                                    btn_find_user1.setClickable(true);
                                                                                } else {
                                                                                    Toast.makeText(getActivity(), "Пользователь " + get_name_from_base + " успешно найден!", Toast.LENGTH_SHORT).show();
                                                                                    ListView listView = rootView.findViewById(R.id.contacts_listview1);
                                                                                    NewContactsAdapter adapter = null;
                                                                                    try {
                                                                                        adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item_new, contacts, CONTACTS);
                                                                                    } catch (IOException e) {
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                    listView.setAdapter(adapter);
                                                                                    adapter.notifyDataSetChanged();
                                                                                    adapter.notifyDataSetInvalidated();
                                                                                    btn_find_user1.setClickable(true);
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NotNull DatabaseError databaseError) {
                                                                                btn_find_user1.setClickable(true);
                                                                            }
                                                                        });
                                                                    } else { //Если пользователь уже есть, уведомляем
                                                                        Toast.makeText(getActivity(), "Пользователь уже есть в вашем списке", Toast.LENGTH_SHORT).show();
                                                                        btn_find_user1.setClickable(true);
                                                                    }
                                                                }
                                                            } else { //Если поле пустое, уведомляем
                                                                Toast.makeText(getActivity(), "Пустое поле ввода", Toast.LENGTH_SHORT).show();
                                                                btn_find_user1.setClickable(true);
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        btn_find_user1.setClickable(true);
                                                    }
                                                }
                                            });

                                            //В приложение добавлен SwipeRefreshLayout. Фактически, в нем нет необходимости. Все данные о пользователях
                                            //из списка контактов и так автоматически обновляются (listener расположен в NewContactsAdapter).
                                            //Однако добавление может оказаться полезным в возможных непредусмотренных ситуациях.
                                            mySwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.parseColor("#FFFFFFFF"));
                                            mySwipeRefreshLayout.setColorSchemeColors(Color.parseColor("#FF5085A5"));
                                            mySwipeRefreshLayout.setOnRefreshListener(
                                                    new SwipeRefreshLayout.OnRefreshListener() {
                                                        @Override
                                                        public void onRefresh() { //Обновляем адаптер, статус пользователя, с небольшой задержкой отключаем SwipeRefreshLayout.
                                                            ListView listView = rootView.findViewById(R.id.contacts_listview1);
                                                            NewContactsAdapter adapter = null;
                                                            try {
                                                                //adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item, contacts, CONTACTS);
                                                                adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item_new, contacts, CONTACTS);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                            listView.setAdapter(adapter);
                                                            adapter.notifyDataSetChanged();

                                                            Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                public void run() {
                                                                    mySwipeRefreshLayout.setRefreshing(false);
                                                                }
                                                            }, 700);
                                                        }
                                                    }
                                            );

                                            //Условие длительного нажатия на элемент списка
                                            NewContactsAdapter finalAdapter1 = adapter;
                                            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                                @Override
                                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                                    removeItemFromList(position);
                                                    return true;
                                                }

                                                private void removeItemFromList(int position) {
                                                    //Уточняем намерения пользователя
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                                    alert.setTitle("Удаление");
                                                    alert.setMessage("Вы точно хотите удалить данного пользователя из списка своих контактов?");
                                                    alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() { //Пользователь хочет удалить контакт
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            //Впоследствии можно дореализовать удаление сообщений, пока что они хранятся.
                                                            /*InternetActivity internetActivity = new InternetActivity();
                                                            internetActivity.loadDataMessages();

                                                            List<String> keys = internetActivity.getKeys();
                                                            //ArrayList<MyMessage> messages = internetActivity.getMessages();
                                                            Map<String, MyMessage> map = internetActivity.getMap();

                                                            //ArrayList<MyContacts> contactsList = adapter.getContactsList();
                                                            //MyContacts contacts_from = contactsList.get(position);
                                                            //String name_interlocutor_to_remove = contacts_from.getName();

                                                            for (int i = 0; i < keys.size(); i++) {
                                                                if (keys.get(i).contains(CONTACTS.get(position))) {
                                                                    map.remove(keys.get(i));
                                                                }
                                                            }*/

                                                            //Удаление, сохранение
                                                            contacts.remove(finalAdapter1.getItem(position));
                                                            CONTACTS.remove(position);
                                                            CHAT_NUMBER.remove(position);

                                                            ListView listView = rootView.findViewById(R.id.contacts_listview1);
                                                            NewContactsAdapter adapter = null;
                                                            try {
                                                                adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item_new, contacts, CONTACTS);
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                            listView.setAdapter(adapter);
                                                            adapter.notifyDataSetChanged();
                                                            adapter.notifyDataSetInvalidated();
                                                            saveDataContacts(my_nic);
                                                        }
                                                    });
                                                    alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() { //Пользователь передумал
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            //Отмена
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    alert.show();
                                                }
                                            });

                                            //Условие простого нажатия на элемент списка (клика)
                                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {

                                                    listView.setClickable(false);

                                                    if (hasConnection(requireActivity())) {
                                                        Toast.makeText(getActivity(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                                                    } else { //При наличии интернета запускается InternetActivity

                                                        LISTEN_REQUEST = database.getReference(secret_field + "/Request/" + CONTACTS.get(position));

                                                        DatabaseReference CHANGE_FIELD;
                                                        CHANGE_FIELD = database.getReference(secret_field + "/Request/" + CONTACTS.get(position));
                                                        CHANGE_FIELD.child("changing_field").setValue(String.valueOf(Math.random() * Long.parseLong("1000000000000000")));

                                                        final int[] listen_to_internet = {1};

                                                        final ChildEventListener childEventListener = LISTEN_REQUEST.addChildEventListener(new ChildEventListener() {
                                                            @Override
                                                            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot123, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                                                                if (snapshot123.exists()) { //Если приглашение имеется

                                                                    String somebody_nic = snapshot123.getValue(String.class);

                                                                    assert somebody_nic != null;
                                                                    if (!somebody_nic.equals(my_nic)) {

                                                                        DatabaseReference IsThisNick = FirebaseDatabase.getInstance().getReference(secret_field + "/Contacts/" + CONTACTS.get(position));
                                                                        IsThisNick.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                                                if (snapshot.exists()) {
                                                                                    String interlocutor_name = snapshot.getValue(String.class);
                                                                                    if (listen_to_internet[0] == 1) {

                                                                                        Intent intent = new Intent(ContactsFragment.this.requireActivity(), InternetActivity.class);

                                                                                        String current_interlocutor = CONTACTS.get(position);
                                                                                        intent.putExtra("current_interlocutor", current_interlocutor);

                                                                                        intent.putExtra("nameinterlocutor", interlocutor_name);

                                                                                        String current_chat = CHAT_NUMBER.get(position);
                                                                                        intent.putExtra("current_chat", current_chat);
                                                                                        startActivity(intent);

                                                                                        linlay_bar.setVisibility(View.VISIBLE);
                                                                                        linlay_list.setVisibility(View.GONE);
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                                                            }
                                                                        });
                                                                    } else {
                                                                        listen_to_internet[0] = 0;

                                                                        if (toast != null) {
                                                                            toast.cancel();
                                                                        }
                                                                        toast = Toast.makeText(getActivity(), "Вы отправили пользователю запрос, ожидайте его принятия", Toast.LENGTH_SHORT);
                                                                        toast.show();
                                                                    }
                                                                } else {

                                                                    DatabaseReference IsThisNick = FirebaseDatabase.getInstance().getReference(secret_field + "/Contacts/" + CONTACTS.get(position));
                                                                    IsThisNick.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                                            if (snapshot.exists()) {
                                                                                String interlocutor_name = snapshot.getValue(String.class);

                                                                                if (listen_to_internet[0] == 1) {

                                                                                    Intent intent = new Intent(ContactsFragment.this.requireActivity(), InternetActivity.class);

                                                                                    String current_interlocutor = CONTACTS.get(position);
                                                                                    intent.putExtra("current_interlocutor", current_interlocutor);

                                                                                    intent.putExtra("nameinterlocutor", interlocutor_name);

                                                                                    String current_chat = CHAT_NUMBER.get(position);
                                                                                    intent.putExtra("current_chat", current_chat);
                                                                                    startActivity(intent);

                                                                                    linlay_bar.setVisibility(View.VISIBLE);
                                                                                    linlay_list.setVisibility(View.GONE);
                                                                                }
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                                        }
                                                                    });
                                                                }
                                                            }

                                                            @Override
                                                            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot123, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
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
                                            });
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
                                });
                            } else {
                                if (!status.equals("NO")) {
                                    Toast.makeText(requireActivity(), status, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(requireActivity(), BottomNavigationActivity.class);
                                    startActivity(intent);
                                    requireActivity().finish();
                                }
                            }
                        } else {
                            Log.wtf("Capable", "!incapable!");
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
                });
            }
        }

        //Отрисовка пользовательского интерфейса для фрагмента
        return rootView;
    }

    //Метод, проверяющий соединение с интернетом
    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return false;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return false;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo == null || !wifiInfo.isConnected();
    }

    //Метод, авторизующий пользователя по email и паролю
    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {

                            //Если, имея данные пользователя, авторизоваться не удалось, пользователю предлагается
                            //занново их ввести - отправление в RegistrationActivity (впоследствии SignIn)

                            Toast.makeText(requireActivity(),
                                    "Авторизация оборвалась. Попробуйте еще раз", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ContactsFragment.this.requireActivity(), RegistrationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    }
                });
    }

    //Метод, сохраняющий информацию о контактах
    private void saveDataContacts(String my_nic) {

        //Данные загружаются с использованием SharedPreferences. Хранятся в формате Json.
        //Сохраняем ArrayList<MyContacts> contacts, ArrayList<String> CONTACTS и ArrayList<String> CHAT_NUMBER.

        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("sharedPreferences" + my_nic, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contacts);
        if (json != null) {
            json = json.substring(1, json.length() - 1);
        }
        editor.putString("task" + my_nic, json);
        editor.apply();

        SharedPreferences sharedPreferencesstr = this.requireActivity().getSharedPreferences("sharedPreferencesstr" + my_nic, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesstr", 0).edit().clear().apply();
        SharedPreferences.Editor editorstr = sharedPreferencesstr.edit();
        Gson gsonstr = new Gson();
        String jsonstr = gsonstr.toJson(CONTACTS);
        editorstr.putString("taskstr" + my_nic, jsonstr);
        editorstr.apply();

        SharedPreferences sharedPreferenceschatid = this.requireActivity().getSharedPreferences("sharedPreferenceschatid" + my_nic, Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferenceschatid", 0).edit().clear().apply();
        SharedPreferences.Editor editorchatid = sharedPreferenceschatid.edit();
        Gson gsonchatid = new Gson();
        String jsonchatid = gsonchatid.toJson(CHAT_NUMBER);
        editorchatid.putString("taskchatid" + my_nic, jsonchatid);
        editorchatid.apply();

        Log.wtf(TAG, json);
        Log.wtf(TAG, jsonstr);
        Log.wtf(TAG, jsonchatid);
    }

    //Метод, загружающий информацию о контактах
    private void loadDataContacts(String my_nic) {
        try {
            //Данные берутся из SharedPreferences. Хранятся в формате Json.
            //Загружаем ArrayList<MyContacts> contacts, ArrayList<String> CONTACTS и ArrayList<String> CHAT_NUMBER.

            SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("sharedPreferences" + my_nic, Context.MODE_PRIVATE);
            //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
            Gson gson = new Gson();
            String json = sharedPreferences.getString("task" + my_nic, null);
            if (json != null) {
                json = "[" + json + "]";
            }
            Type type = new TypeToken<ArrayList<MyContacts>>() {
            }.getType();
            contacts = gson.fromJson(json, type);
            if (contacts == null) {
                contacts = new ArrayList<>();
            }

            SharedPreferences sharedPreferencesstr = this.requireActivity().getSharedPreferences("sharedPreferencesstr" + my_nic, Context.MODE_PRIVATE);
            //this.requireActivity().getSharedPreferences("sharedPreferencesstr", 0).edit().clear().apply();
            Gson gsonstr = new Gson();
            String jsonstr = sharedPreferencesstr.getString("taskstr" + my_nic, null);
            Type typestr = new TypeToken<ArrayList<String>>() {
            }.getType();
            CONTACTS = gsonstr.fromJson(jsonstr, typestr);
            if (CONTACTS == null) {
                CONTACTS = new ArrayList<>();
            }

            SharedPreferences sharedPreferenceschatid = this.requireActivity().getSharedPreferences("sharedPreferenceschatid" + my_nic, Context.MODE_PRIVATE);
            //this.requireActivity().getSharedPreferences("sharedPreferenceschatid", 0).edit().clear().apply();
            Gson gsonchatid = new Gson();
            String jsonchatid = sharedPreferenceschatid.getString("taskchatid" + my_nic, null);
            Type typechatid = new TypeToken<ArrayList<String>>() {
            }.getType();
            CHAT_NUMBER = gsonchatid.fromJson(jsonchatid, typechatid);
            if (CHAT_NUMBER == null) {
                CHAT_NUMBER = new ArrayList<>();
            }

            Log.wtf(TAG, json);
            Log.wtf(TAG, jsonstr);
            Log.wtf(TAG, jsonchatid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {

            linlay_bar.setVisibility(View.GONE);
            linlay_list.setVisibility(View.VISIBLE);

            //При onStop фрагмента на сервер Firebase отправляется TimeStamp. По сути это метка времени,
            //которую потом можно преобразовать в дату (что успешно делается в начале InternetActivity).
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Метод, создающий key pair (шифрование - RSA Algorithm)
    private void generateKeys(String keyAlgorithm, int numBits, DatabaseReference
            CHAT_users, String chatId, String my_name, String my_nic) {
        try {
            //Создаем key pair
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
            keyGen.initialize(numBits);
            KeyPair keyPair = keyGen.genKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            //Сохраняем key pair
            saveKeyPair(chatId, privateKey, publicKey, my_name, CHAT_users, my_nic);
            getPrivateKey(chatId, my_nic);

            Log.wtf("pbk", String.valueOf(Arrays.equals(saveKeyPair(chatId, privateKey, publicKey, my_name, CHAT_users, my_nic), getPrivateKey(chatId, my_nic))));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Метод, сохраняющий key pair
    public byte[] saveKeyPair(String chatId, PrivateKey privateKey, PublicKey publicKey, String
            my_name, DatabaseReference CHAT_users, String my_nic) throws Exception {

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
        Map<String, String> map = (Map<String, String>) new TreeMap<String, String>();
        map.put(chatId, str_key_pvt);
        SharedPreferences sharedPreference_privateKey = this.requireActivity().getSharedPreferences("sharedPreference_privateKey" + my_nic, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor_privateKey = sharedPreference_privateKey.edit();
        Gson gson_privateKey = new Gson();
        String json_privateKey = gson_privateKey.toJson(map);
        if (json_privateKey != null) {
            json_privateKey = json_privateKey.substring(1, json_privateKey.length() - 1);
        }
        editor_privateKey.putString("task_privateKey" + my_nic, json_privateKey);
        editor_privateKey.apply();

        Log.wtf("pbl", "BYTE KEY " + Arrays.toString(publicKeyBytes));
        String str_key_pbl = Base64.getEncoder().encodeToString(publicKeyBytes);
        //String str_key_pbl = Base64.encodeToString(publicKeyBytes, Base64.NO_PADDING | Base64.NO_WRAP);
        Log.wtf("pbl", "STRING KEY " + str_key_pbl);

        //Отправляем публичный ключ на Firebase, чтобы собеседники могли писать нам сообщение
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        CHAT_users = database.getReference(secret_field + "/Internet_Messages/" + chatId);
        CHAT_users.child(my_nic + "_key_public").setValue(str_key_pbl);

        return privateKeyBytes;
    }

    //Метод, получающий секретный ключ через SharedPreferences из памяти устройства
    private byte[] getPrivateKey(String chatId, String my_nic) throws Exception {

        SharedPreferences sharedPreference_privateKey = this.requireActivity().getSharedPreferences("sharedPreference_privateKey" + my_nic, Context.MODE_PRIVATE);
        Gson gson_privateKey = new Gson();
        String json_privateKey = sharedPreference_privateKey.getString("task_privateKey" + my_nic, null);
        if (json_privateKey != null) {
            json_privateKey = "{" + json_privateKey + "}";
        }
        Type type_privateKey = new TypeToken<TreeMap<String, String>>() {
        }.getType();
        Map<String, String> map = gson_privateKey.fromJson(json_privateKey, type_privateKey);

        String str_key = "";
        List<String> help_array_messages = new ArrayList<>(map.values());
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).equals(chatId)) {
                str_key = help_array_messages.get(i);
                Log.wtf("str_key", str_key);
            }
        }

        byte[] privateKeyBytes = Base64.getDecoder().decode(str_key);
        //byte[] privateKeyBytes = Base64.decode(str_key, Base64.NO_PADDING | Base64.NO_WRAP);
        Log.wtf("pvt", "BYTE KEY" + privateKeyBytes);
        Log.wtf("pvt", "FINAL OUTPUT" + privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privatekey = keyFactory.generatePrivate(privateKeySpec);

        privateKeyBytes = privatekey.getEncoded();
        Log.wtf("publicKey (byte) e/d", Arrays.toString(privateKeyBytes));

        return privateKeyBytes;
    }

    private void removerequest(String secret_field, String my_nic) {
        //Получение доступа к БД Firebase. Удаляем запрос.
        DatabaseReference MessageRemove = FirebaseDatabase.getInstance().getReference(secret_field + "/Request/");
        ValueEventListener remove_request_listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                    Log.wtf("удаление запроса", String.valueOf(postsnapshot));
                    dataSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        MessageRemove.child(my_nic).orderByKey().addListenerForSingleValueEvent(remove_request_listener);
        MessageRemove.removeEventListener(remove_request_listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        listView.setClickable(true);
        ft.detach(this).attach(this).commit();
    }

    public void loadDataMessages(String my_nic) {

        SharedPreferences sharedPreferencesarrayall = requireActivity().getSharedPreferences("sharedPreferencesarrayall" + my_nic, Context.MODE_PRIVATE);
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
    }
}