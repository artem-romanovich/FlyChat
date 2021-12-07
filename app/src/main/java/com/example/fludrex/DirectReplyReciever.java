package com.example.fludrex;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Cipher;

public class DirectReplyReciever extends BroadcastReceiver {

    final DatabaseReference[] USER = new DatabaseReference[1];
    final DatabaseReference[] NOTIFICATION = new DatabaseReference[1];

    public static ArrayList<String> MESSAGES = new ArrayList<>();
    public static Map<String, MyMessage> map = new TreeMap<>();

    String get_time_To_look;
    String get_time;
    String interlocutor_nic;
    String my_nic;
    String my_name;
    String chatId;
    String secret_field;
    String get_message;
    String get_user_name;
    String get_key;

    Bundle remoteInput;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(context.openFileInput("file_nic")));
            my_nic = br_nn.readLine();
            BufferedReader br_n = new BufferedReader(new InputStreamReader(context.openFileInput("file_username" + my_nic)));
            my_name = br_n.readLine();

            SharedPreferences sharedPreferences_notification = context.getSharedPreferences("sharedPreferences_notification" + my_nic, Context.MODE_PRIVATE);
            Gson gson_notification = new Gson();
            String json_notification = sharedPreferences_notification.getString("task_notification" + my_nic, null);
            if (json_notification != null) { json_notification = "[" + json_notification + "]"; }
            Type type_notification = new TypeToken<String[]>() {}.getType();
            String[] notif = gson_notification.fromJson(json_notification, type_notification);
            if (notif == null) { notif = new String[]{}; }
            context.getSharedPreferences("sharedPreferences_notification", 0).edit().clear().apply();

            get_time_To_look = notif[0]; chatId = notif[1]; get_time = notif[2];
            secret_field = notif[3]; interlocutor_nic = notif[4]; get_message = notif[5];
            String s = notif[6]; get_user_name = notif[7]; get_key = notif[8];

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            USER[0] = database.getReference(secret_field + "/Internet_Messages/" + "/" + chatId + "/" + my_nic);
            NOTIFICATION[0] = database.getReference(secret_field + "/Notifications/" + interlocutor_nic);

            Log.wtf("<-get_time_To_look", get_time_To_look);
            Log.wtf("<-get_time", get_time);
            Log.wtf("<-interlocutor_nic", interlocutor_nic);
            Log.wtf("<-my_nic", my_nic);
            Log.wtf("<-chatId", chatId);
            Log.wtf("<-secret_field", secret_field);
            Log.wtf("<-get_message", get_message);
            Log.wtf("<-get_user_name", get_user_name);

            final long[] delay_time_long = new long[1];

            remoteInput = RemoteInput.getResultsFromIntent(intent);

            if (remoteInput != null) {

                CharSequence replyText = remoteInput.getCharSequence("key_text_reply");
                MessageListeningService.chatMessage = new MessagesForService(replyText, null);

                Toast.makeText(context, "Пользователь получит ваше сообщение", Toast.LENGTH_SHORT).show();
                Log.wtf("DirectReplyReciever", "Пользователь получит ваше сообщение");

                String sent_message = replyText.toString();
                Log.wtf("sent_message", sent_message);

                if (sent_message.equals("") ||
                        sent_message.equals(" ") ||
                        sent_message.equals("/n") ||
                        (sent_message.length() > InternetActivity.max_message_length)) {

                    if (sent_message.length() > InternetActivity.max_message_length) {
                        Toast.makeText(context, "Слишком большое (либо пустое) сообщение", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
                    String finalSecret_field = secret_field;
                    offsetRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //Считываем отстройку по времени
                            delay_time_long[0] = snapshot.getValue(Long.class);
                            Log.wtf("sent_message", String.valueOf(delay_time_long[0]));

                            CurrentTime currentTime = new CurrentTime();

                            loadDataMessages(my_nic, context);
                            MESSAGES.add(currentTime.getCurrentTimeFromBase(delay_time_long[0]) + "_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nic);
                            map.put(currentTime.getCurrentTimeFromBase(delay_time_long[0]) + "_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nic, new MyMessage(my_name, replyText.toString(), currentTime.getCurrentTimeFromBaseNextLine(delay_time_long[0])));
                            saveDataMessages(my_nic, context);

                            final DatabaseReference key_public_Ref = database.getReference(finalSecret_field + "/Internet_Messages/" + chatId + "/" + interlocutor_nic + "_key_public");

                            key_public_Ref.addValueEventListener(new ValueEventListener() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot11) {
                                    if (dataSnapshot11.exists()) {
                                        try {

                                            String key_public = dataSnapshot11.getValue(String.class);
                                            Log.wtf("key_public_Base64 (string)", key_public);

                                            byte[] publicKeyBytes = Base64.getDecoder().decode(key_public);
                                            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                                            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

                                            Cipher rsa = Cipher.getInstance("RSA");
                                            rsa.init(Cipher.ENCRYPT_MODE, publicKey);

                                            String encrypt_sent_message = Base64.getEncoder().encodeToString(rsa.doFinal(sent_message.getBytes()));
                                            Log.wtf("encrypt_sent_message", encrypt_sent_message);

                                            String nkey = USER[0].push().getKey();
                                            Log.wtf("nkey", nkey);
                                            assert nkey != null;
                                            //USER[0].child(nkey).setValue(nkey + "<" + currentTime.getCurrentTimeFromBase(delay_time_long[0]) + encrypt_sent_message);
                                            //NOTIFICATION[0].child(nkey).setValue(nkey + "<" + my_name + "|" + my_nic + "+" + chatId + "*" + currentTime.currenttime + encrypt_sent_message);

                                            USER[0].child(nkey).setValue(nkey + "<" + currentTime.getCurrentTimeFromBase(delay_time_long[0]) + encrypt_sent_message);
                                            //NOTIFICATION[0].child(nkey).setValue(nkey + "<" + my_name + "|" + my_nic + "+" + chatId + "*" + currentTime.currenttime + encrypt_sent_message);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

                MessageListeningService.sendNotification(context);
                MessageListeningService.removeAllNotifications(context);

                PackageManager pm  = context.getPackageManager();
                ComponentName componentName = new ComponentName(context, DirectReplyReciever.class);
                pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveDataMessages(String my_nic, Context context) {

        SharedPreferences sharedPreferencesarray = context.getSharedPreferences("sharedPreferencesarray" + my_nic, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorarray = sharedPreferencesarray.edit();
        Gson gsonarray = new Gson();
        String jsonarray = gsonarray.toJson(MESSAGES);
        if (jsonarray != null) {
            jsonarray = jsonarray.substring(1, jsonarray.length() - 1);
        }
        editorarray.putString("taskarray" + my_nic, jsonarray);
        editorarray.apply();

        SharedPreferences sharedPreferencesarrayall = context.getSharedPreferences("sharedPreferencesarrayall" + my_nic, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorarrayall = sharedPreferencesarrayall.edit();
        Gson gsonarrayall = new Gson();
        String jsonarrayall = gsonarrayall.toJson(map);
        if (jsonarrayall != null) {
            jsonarrayall = jsonarrayall.substring(1, jsonarrayall.length() - 1);
        }
        editorarrayall.putString("taskarrayall" + my_nic, jsonarrayall);
        editorarrayall.apply();
    }
    public static void loadDataMessages(String my_nic, Context context) {
        SharedPreferences sharedPreferencesarray = context.getSharedPreferences("sharedPreferencesarray" + my_nic, Context.MODE_PRIVATE);
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
        SharedPreferences sharedPreferencesarrayall = context.getSharedPreferences("sharedPreferencesarrayall" + my_nic, Context.MODE_PRIVATE);
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