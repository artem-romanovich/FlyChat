package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class InternetActivity extends AppCompatActivity {

    public static int max_message_length = 5000;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference USER;
    DatabaseReference USER_STORAGE;
    DatabaseReference INTERLOCUTOR;
    DatabaseReference INTERLOCUTOR_STORAGE;
    DatabaseReference NOTIFICATION;
    DatabaseReference CHATID;

    EditText get_message;
    TextView current_interlocutor;
    Button btn_edit_message, btn_set_name;
    ArrayList<String> MESSAGES;
    Map<String, MyMessage> map;
    ArrayList<MyMessage> messages = new ArrayList<>();

    public String sent_message;
    public String my_name; //!
    public String interlocutor_name;
    public String path;
    public String chatId;

    private static final String CHANNEL_ID = "New channel";
    private static final int NOTIFY_ID = 101;
    private int counter = 101;

    int user = 1;
    int interlocutor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet);
        Objects.requireNonNull(getSupportActionBar()).hide();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btn_edit_message = findViewById(R.id.btn_edit_message);
        btn_set_name = findViewById(R.id.btn_set_name);
        get_message = findViewById(R.id.get_message);
        current_interlocutor = findViewById(R.id.current_interlocutor);

        String s = "Диалог с " + "<b>" + getIntent().getStringExtra("current_interlocutor") + "</b>";
        current_interlocutor.setText(Html.fromHtml(s));
        interlocutor_name = getIntent().getStringExtra("current_interlocutor");

        chatId = getIntent().getStringExtra("current_chat");

        BufferedReader br_n = null;
        try {
            br_n = new BufferedReader(new InputStreamReader(openFileInput("file_username")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert br_n != null;
            my_name = br_n.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        USER = database.getReference("Internet_Messages/" + "/" + chatId + "/" + my_name);
        INTERLOCUTOR = database.getReference("Internet_Messages/" + "/" + chatId + "/" + interlocutor_name);
        USER_STORAGE = database.getReference("Internet_Messages/" + "/" + chatId + "/" + "storage" + "/" + my_name);
        INTERLOCUTOR_STORAGE = database.getReference("Internet_Messages/" + "/" + chatId + "/" + "storage" + "/" + interlocutor_name);
        CHATID = database.getReference("Internet_Messages/" + "/" + chatId);

        //messages = new ArrayList<MyMessage>();
        //MESSAGES = new ArrayList<>();

        loadData();

        List<MyMessage> help_array_messages = new ArrayList<MyMessage>(map.values());
        List<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        for (Map.Entry entry : map.entrySet()) {
            Log.wtf("Вывод полностью map", "Key: " + entry.getKey() + " Value: " + entry.getValue());
        }
        for (int i = 0; i < keys.size(); i++) {
            help_array_messages.add(map.get(keys.get(i)));
            //Log.wtf("Вывод map", keys.get(i) + " - " + MESSAGES.get(i));
            Log.wtf("Вывод map", keys.get(i));
        }

        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).contains((my_name + "_" + interlocutor_name))) {
                //Log.wtf("Вывод help_array_message", String.valueOf(help_array_messages.get(i))+" (mi)");
                messages.add(help_array_messages.get(i));
            } else if (keys.get(i).contains((interlocutor_name + "_" + my_name))) {
                //Log.wtf("Вывод help_array_message", String.valueOf(help_array_messages.get(i))+" (im)");
                messages.add(help_array_messages.get(i));
            }
        }
        //Log.wtf("Вывод всего массива Messages", MESSAGES.toString());
        //Log.wtf("Вывод всего массива help_array_messages", help_array_messages.toString());

        /*ListView messagesList = (ListView) findViewById(R.id.messages_listview);
        NewMessageAdapter adapter = new NewMessageAdapter(this, R.layout.message_item_user, messages);
        messagesList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        messagesList.smoothScrollToPosition(MESSAGES.size());*/

        int IorU = user;
        ListView messagesList = (ListView) findViewById(R.id.messages_listview);
        NewMessageAdapter adapter = new NewMessageAdapter(this, IorU, messages);
        messagesList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        messagesList.smoothScrollToPosition(MESSAGES.size());

        btn_edit_message.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (!hasConnection(InternetActivity.this)) {
                    Toast.makeText(InternetActivity.this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    sent_message = get_message.getText().toString();

                    Log.wtf("my_name", my_name);
                    Log.wtf("interlocutor_name", interlocutor_name);
                    Log.wtf("chatId", chatId);

                    ReplaceRepeat replaceRepeat = new ReplaceRepeat();
                    sent_message = replaceRepeat.ReplaceRepeatStr(sent_message);

                    if (sent_message.equals("") ||
                            sent_message.equals(" ") ||
                            sent_message.equals("/n") ||
                            (sent_message.length() > max_message_length)) {

                        Toast.makeText(getApplicationContext(),
                                "Слишком большое (либо пустое) сообщение", Toast.LENGTH_SHORT).show();
                    } else {

                        CurrentTime currentTime = new CurrentTime();
                        CurrentTime currentTimeComma = new CurrentTime();
                        currentTime.getCurrentTime();
                        currentTimeComma.getCurrentTimeComma();
                        messages.add(new MyMessage(my_name, sent_message, currentTime.currenttime));
                        MESSAGES.add(currentTimeComma.currenttime + "_" + my_name + "_" + interlocutor_name);
                        map.put(currentTimeComma.currenttime + "_" + my_name + "_" + interlocutor_name, new MyMessage(my_name, sent_message, currentTime.currenttime));
                        saveData();
                        adapter.notifyDataSetChanged();
                        messagesList.smoothScrollToPosition(MESSAGES.size());

                        //NOTIFICATION = database.getReference("Notifications/");
                        //InternetActivity.this.NOTIFICATION.push().child(interlocutor_name).setValue(sent_message);
                        InternetActivity.this.USER.push().setValue(sent_message);
                        InternetActivity.this.USER_STORAGE.push().setValue(sent_message);
                        //USER.child(currentTimeComma.currenttime).setValue(sent_message);

                        get_message.setText("");
                    }
                }
            }
        });

        /*messagesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeItemFromList(position);
                return true;
            }

            private void removeItemFromList(int position) {
                AlertDialog.Builder alert = new AlertDialog.Builder(InternetActivity.this);
                alert.setTitle("Удаление");
                alert.setMessage("Вы точно хотите удалить данное сообщение без возможности восстановления? У пользователя данное сообщение останется");
                alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyMessage delete_message = adapter.getItem(position);
                        messages.remove(delete_message);
                        MESSAGES.remove(position);

                        for (Map.Entry entry : map.entrySet()) {

                            Log.wtf("remove", String.valueOf(entry.getKey()));
                            Log.wtf("remove", String.valueOf(delete_message));

                            if (entry.getKey() == delete_message) {
                                map.remove(entry.getKey());
                                Log.wtf("remove", "SUCCESS");
                            }
                        }

                        adapter.notifyDataSetChanged();
                        adapter.notifyDataSetInvalidated();
                        saveData();
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
        });*/

        final ChildEventListener childEventListener1 = INTERLOCUTOR.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                String data_snapshotValue = datasnapshot.getValue(String.class);
                //Log.i(LOCATION_SERVICE, data_snapshotValue);

                CurrentTime currentTime = new CurrentTime();
                CurrentTime currentTimeComma = new CurrentTime();
                currentTime.getCurrentTime();
                currentTimeComma.getCurrentTimeComma();

                Intent intent = new Intent(InternetActivity.this, BottomNavigationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(InternetActivity.this, 0, intent, 0);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(InternetActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_method_draw_image)
                        .setContentTitle(interlocutor_name)
                        .setContentText(data_snapshotValue)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setAutoCancel(true);
                Notification notification = builder.build();

                createNotificationChannel(interlocutor_name, data_snapshotValue);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(InternetActivity.this);
                notificationManager.notify(counter, notification);

                messages.add(new MyMessage(interlocutor_name, data_snapshotValue, currentTime.currenttime));
                MESSAGES.add(currentTimeComma.currenttime + "_" + interlocutor_name + "_" + my_name);
                map.put(currentTimeComma.currenttime + "_" + interlocutor_name + "_" + my_name, new MyMessage(interlocutor_name, data_snapshotValue, currentTime.currenttime));

                adapter.notifyDataSetChanged();
                messagesList.smoothScrollToPosition(MESSAGES.size());
                DatabaseReference MessageRemove = FirebaseDatabase.getInstance().getReference("Internet_Messages/");
                MessageRemove.child(chatId).child(interlocutor_name).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                            Log.wtf(LOCATION_SERVICE, String.valueOf(postsnapshot));
                            dataSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                saveData();
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                Log.wtf("onChildChanged", "happen");
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

    private void saveData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(messages);
        if (json != null) {
            json = json.substring(1, json.length() - 1);
        }
        editor.putString("taskm", json);
        editor.apply();

        SharedPreferences sharedPreferencesarray = this.getSharedPreferences("sharedPreferencesarray", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarray", 0).edit().clear().apply();
        SharedPreferences.Editor editorarray = sharedPreferencesarray.edit();
        Gson gsonarray = new Gson();
        String jsonarray = gsonarray.toJson(MESSAGES);
        if (jsonarray != null) {
            jsonarray = jsonarray.substring(1, jsonarray.length() - 1);
        }
        editorarray.putString("taskarray", jsonarray);
        editorarray.apply();

        SharedPreferences sharedPreferencesarrayall = this.getSharedPreferences("sharedPreferencesarrayall", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarrayall", 0).edit().clear().apply();
        SharedPreferences.Editor editorarrayall = sharedPreferencesarrayall.edit();
        Gson gsonarrayall = new Gson();
        String jsonarrayall = gsonarrayall.toJson(map);
        if (jsonarrayall != null) {
            jsonarrayall = jsonarrayall.substring(1, jsonarrayall.length() - 1);
        }
        editorarrayall.putString("taskarrayall", jsonarrayall);
        editorarrayall.apply();

        //Log.wtf("messages json", json);
        //Log.wtf("MESSAGES json", jsonarray);
        //Log.wtf("map json", jsonarrayall);
    }

    private void loadData() {
        /*SharedPreferences sharedPreferences = this.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
        Gson gson = new Gson();
        String json = sharedPreferences.getString("taskm", null);
        if (json != null) {
            json = "[" + json + "]";
        }
        Type type = new TypeToken<ArrayList<MyMessage>>() {
        }.getType();
        messages = gson.fromJson(json, type);
        if (messages == null) {
            messages = new ArrayList<>();
        }*/

        SharedPreferences sharedPreferencesarray = this.getSharedPreferences("sharedPreferencesarray", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarray", 0).edit().clear().apply();
        Gson gsonarray = new Gson();
        String jsonarray = sharedPreferencesarray.getString("taskarray", null);
        if (jsonarray != null) {
            jsonarray = "[" + jsonarray + "]";
        }
        Type typearray = new TypeToken<ArrayList<String>>() {
        }.getType();
        MESSAGES = gsonarray.fromJson(jsonarray, typearray);
        if (MESSAGES == null) {
            MESSAGES = new ArrayList<>();
        }

        SharedPreferences sharedPreferencesarrayall = this.getSharedPreferences("sharedPreferencesarrayall", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarrayall", 0).edit().clear().apply();
        Gson gsonarrayall = new Gson();
        String jsonarrayall = sharedPreferencesarrayall.getString("taskarrayall", null);
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

    private void createNotificationChannel(String interlocutor_name, String data_snapshotValue) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, interlocutor_name, importance);
            channel.setDescription(data_snapshotValue);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}