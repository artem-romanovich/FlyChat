package com.example.fludrex;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import androidx.core.app.RemoteInput;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Cipher;

import static com.example.fludrex.App.CHANNEL_ID;

public class MessageListeningService extends Service {

    public static int counter = 1;
    public static int d = 1;

    public static MessagesForService chatMessage;
    private static int UNIQUE_INT_PER_CALL = 0;

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference NOTIFICATION;
    public static DatabaseReference SEND_CURRENT_APP;
    public static DatabaseReference connectedRef;
    public static ChildEventListener childEventListener;
    public static ValueEventListener connectedEventListener;

    public static String tmp_nck;

    public static ArrayList<String> MESSAGES = new ArrayList<>();
    public static Map<String, MyMessage> map = new TreeMap<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String contentText = "???????????????? ?????????? ??????????????????...";
            Intent intent1 = new Intent(this, GotoReciever.class);
            PendingIntent actionIntent = PendingIntent.getBroadcast(this,
                    0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "JH")
                    .setSound(null)
                    .setSilent(true);
            Notification notification = builder
                    .setContentTitle("FlyChat ??????????????")
                    .setContentText(contentText)
                    .setContentIntent(actionIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                    .build();

            createNotificationChannel(this, "FlyChat goto", "???????????????????????????? ?? ???????????? ???????????????? ??????????????????. ?????????????? ?????? ????????????????????.", "JH");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(-1, notification);

            startForeground(-1, notification);

            if (tmp_nck != null) {
                if (hasConnection(this)) {
                    sendNotification(this);
                }
            }

        } catch (Exception e) {
            Log.wtf("oncreate", "onStartCommand");
            e.printStackTrace();
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        if (hasConnection(MessageListeningService.this)) {
                            if (NOTIFICATION != null && childEventListener != null) {
                                NOTIFICATION.removeEventListener(childEventListener);
                            }
                            if (connectedRef != null && connectedEventListener != null) {
                                connectedRef.removeEventListener(connectedEventListener);
                            }
                            if (InternetActivity.INTERLOCUTOR != null && InternetActivity.childEventListener1 != null) {
                                InternetActivity.INTERLOCUTOR.removeEventListener(InternetActivity.childEventListener1);
                            }
                            oncreate(MessageListeningService.this);
                            break;
                        }
                    }
                }
            }).start();
        }
        return START_STICKY;
    }

    public static void sendNotification(Context context) throws Exception {
        if (hasConnection(context)) {

            BufferedReader br_nn = new BufferedReader(new InputStreamReader(context.openFileInput("file_nic")));
            String my_nic = br_nn.readLine();

            if (!tmp_nck.equals(my_nic)) {
                NOTIFICATION.removeEventListener(childEventListener);
                if (InternetActivity.INTERLOCUTOR != null && InternetActivity.childEventListener1 != null) {
                    InternetActivity.INTERLOCUTOR.removeEventListener(InternetActivity.childEventListener1);
                }
                oncreate(context);
            }
        } else {
            NOTIFICATION.removeEventListener(childEventListener);
            if (InternetActivity.INTERLOCUTOR != null && InternetActivity.childEventListener1 != null) {
                InternetActivity.INTERLOCUTOR.removeEventListener(InternetActivity.childEventListener1);
            }
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        if (hasConnection(context)) {
                            if (NOTIFICATION != null && childEventListener != null) {
                                NOTIFICATION.removeEventListener(childEventListener);
                            }
                            if (connectedRef != null && connectedEventListener != null) {
                                connectedRef.removeEventListener(connectedEventListener);
                            }
                            if (InternetActivity.INTERLOCUTOR != null && InternetActivity.childEventListener1 != null) {
                                InternetActivity.INTERLOCUTOR.removeEventListener(InternetActivity.childEventListener1);
                            }
                            oncreate(context);
                            break;
                        }
                    }
                }
            }).start();
        }
    }

    public static String removeNotification(Context context, int notificationId) {
        NotificationManager nMgr = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(notificationId);
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (InternetActivity.INTERLOCUTOR != null && InternetActivity.childEventListener1 != null) {
            InternetActivity.INTERLOCUTOR.removeEventListener(InternetActivity.childEventListener1);
        }
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (hasConnection(MessageListeningService.this)) {
                        if (NOTIFICATION != null && childEventListener != null) {
                            NOTIFICATION.removeEventListener(childEventListener);
                        }
                        if (connectedRef != null && connectedEventListener != null) {
                            connectedRef.removeEventListener(connectedEventListener);
                        }
                        if (InternetActivity.INTERLOCUTOR != null && InternetActivity.childEventListener1 != null) {
                            InternetActivity.INTERLOCUTOR.removeEventListener(InternetActivity.childEventListener1);
                        }
                        oncreate(MessageListeningService.this);
                        break;
                    }
                }
            }
        }).start();
    }

    public static void oncreate(Context context) {
        Log.wtf("create service", "create service");
        try {
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(context.openFileInput("file_nic")));
            String my_nic = br_nn.readLine();
            tmp_nck = my_nic;
            Log.wtf("tmp", tmp_nck);
            Log.wtf("my_nic", my_nic);

            BufferedReader br_f = new BufferedReader(new InputStreamReader(context.openFileInput("file_secret_field")));
            String secret_field = br_f.readLine();

            connectedRef = database.getReference(".info/connected");
            connectedEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        boolean connected = snapshot.getValue(Boolean.class);
                        DatabaseReference ONLINE_USERS = database.getReference(secret_field + "/Online_Users/" + my_nic);
                        DatabaseReference OU = ONLINE_USERS.child(my_nic);

                        Handler handler = new Handler();
                        handler.postDelayed(() -> {

                            try {
                                if (connected) {
                                    OU.onDisconnect().setValue(ServerValue.TIMESTAMP);
                                    OU.setValue("online");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }, 1500);
                    } catch (
                            Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            };
            connectedRef.addValueEventListener(connectedEventListener);

            NOTIFICATION = database.getReference(secret_field + "/Notifications/" + my_nic);
            InternetActivity.listener_status = 0;
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NotNull DataSnapshot datasnapshot, String previousChildName) {
                    try {
                        String get_user_info = datasnapshot.getValue(String.class);

                        if (get_user_info != null) {

                            int index_left = get_user_info.indexOf("<");
                            int index_bvb = get_user_info.indexOf("|");
                            int index_plus = get_user_info.indexOf("+");
                            int index_z = get_user_info.indexOf("*");

                            if (index_bvb != -1 && index_plus != -1) {

                                String get_key = get_user_info.substring(0, index_left);
                                String get_user_name = get_user_info.substring(index_left + 1, index_bvb);
                                String get_user_nick = get_user_info.substring(index_bvb + 1, index_plus);
                                String get_user_chat = get_user_info.substring(index_plus + 1, index_z);
                                String get_message = get_user_info.substring(index_z + 1);
                                String get_time = get_message.substring(0, 14);
                                get_message = get_message.substring(14);

                                String get_time_To_look = get_time.substring(8, 10) + "." + get_time.substring(10, 12) + "\n" +
                                        get_time.substring(6, 8) + "." + get_time.substring(4, 6) + "." + get_time.substring(0, 4);

                                DatabaseReference NotifRemove = FirebaseDatabase.getInstance().getReference(secret_field + "/Notifications/" + my_nic);
                                ValueEventListener remove_Notif_listener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                                            dataSnapshot.getRef().child(get_key).removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                };
                                NotifRemove.orderByKey().addListenerForSingleValueEvent(remove_Notif_listener);
                                //?????????????????? ?????????????? ?? ???? Firebase. ???????????????? ?????????????????????? ?????????????????? ?? ????.
                                DatabaseReference MessageRemove = FirebaseDatabase.getInstance().getReference(secret_field + "/Internet_Messages/");
                                ValueEventListener remove_message_listener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                                            dataSnapshot.getRef().child(get_key).removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                };
                                MessageRemove.child(get_user_chat).child(get_user_nick).orderByKey().addListenerForSingleValueEvent(remove_message_listener);

                                if (!my_nic.equals(get_user_nick)) {

                                    PrivateKey privateKey = getPrivateKey(get_user_chat, my_nic, context);
                                    Cipher rsa = Cipher.getInstance("RSA");
                                    rsa.init(Cipher.DECRYPT_MODE, privateKey);
                                    byte[] encrypt_sent_message = Base64.getDecoder().decode(get_message);
                                    byte[] utf8 = rsa.doFinal(encrypt_sent_message);
                                    get_message = new String(utf8, "UTF8");

                                    loadDataMessages(my_nic, context);
                                    MESSAGES.add(get_time + "_(" + my_nic + ")_" + get_user_nick + "_" + my_nic);
                                    map.put(get_time + "_(" + my_nic + ")_" + get_user_nick + "_" + my_nic, new MyMessage(get_user_name, get_message, get_time_To_look));
                                    saveDataMessages(my_nic, context); //!

                                    //----------------------------------------------------------------------
                                    Intent broadcastIntent = new Intent(context, NotificationReciever.class);
                                    broadcastIntent.putExtra("nameinterlocutor", get_user_name);
                                    broadcastIntent.putExtra("current_interlocutor", get_user_nick);
                                    broadcastIntent.putExtra("current_chat", get_user_chat);
                                    PendingIntent actionIntent = PendingIntent.getBroadcast(context,
                                            0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    //----------------------------------------------------------------------
                                    RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply")
                                            .setLabel("Your answer...")
                                            .build();

                                    PackageManager pm  = context.getPackageManager();
                                    ComponentName componentName = new ComponentName(context, DirectReplyReciever.class);
                                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                            PackageManager.DONT_KILL_APP);

                                    Intent replyIntent = new Intent(context, DirectReplyReciever.class);

                                    Log.wtf("->get_key", get_key);
                                    Log.wtf("->get_time_To_look", get_time_To_look);
                                    Log.wtf("->get_time", get_time);
                                    Log.wtf("->interlocutor_nic", get_user_nick);
                                    Log.wtf("->my_nic", my_nic);
                                    Log.wtf("->chatId", get_user_chat);
                                    Log.wtf("->secret_field", secret_field);
                                    Log.wtf("->get_message", get_message);
                                    Log.wtf("->get_user_name", get_user_name);
                                    String[] notif = new String[]
                                            {get_time_To_look, get_user_chat, get_time, secret_field, get_user_nick, get_message, my_nic, get_user_name, get_key};
                                    replyIntent.putExtra(android.content.Intent.EXTRA_TEXT, notif);
                                    SharedPreferences sharedPreferences_notification = context.getSharedPreferences("sharedPreferences_notification" + my_nic, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor_notification = sharedPreferences_notification.edit();
                                    Gson gson_notification = new Gson();
                                    String json_notification = gson_notification.toJson(notif);
                                    if (json_notification != null) {
                                        json_notification = json_notification.substring(1, json_notification.length() - 1);
                                    }
                                    editor_notification.putString("task_notification" + my_nic, json_notification);
                                    editor_notification.apply();

                                    PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context,
                                            UNIQUE_INT_PER_CALL, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    UNIQUE_INT_PER_CALL++;

                                    NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                                            R.drawable.ic_singing_bird,
                                            "????????????????",
                                            replyPendingIntent).addRemoteInput(remoteInput).build();
                                    NotificationCompat.MessagingStyle messagingStyle =
                                            new NotificationCompat.MessagingStyle("??");
                                    messagingStyle.setConversationTitle("?????????? ??????????????????");

                                    chatMessage = new MessagesForService(get_message, get_user_name);
                                    NotificationCompat.MessagingStyle.Message notificationMessage =
                                            new NotificationCompat.MessagingStyle.Message(
                                                    chatMessage.getText(),
                                                    chatMessage.getTimestamp(),
                                                    chatMessage.getSender()
                                            );
                                    messagingStyle.addMessage(notificationMessage);
                                    //----------------------------------------------------------------------

                                    Intent intent = new Intent(context, InternetActivity.class);
                                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            //.setContentTitle(get_user_name)
                                            //.setContentText("?????????? ??????????????????!")
                                            .setStyle(messagingStyle)
                                            .addAction(replyAction)
                                            .addAction(android.R.drawable.ic_delete, "??????????????", actionIntent)
                                            //.addAction(android.R.drawable.ic_delete, "??????????????????", pendRemoveIntent)
                                            .setColor(Color.parseColor("#31708E"))
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            //.setContentIntent(pendingIntent)
                                            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                            .setAutoCancel(true);
                                    Notification notification = builder.build();

                                    //?????????????? ?????????? ??????????????????????
                                    createNotificationChannel(context, "Message Listener Service",
                                            "???????????? ???????????????????? ?????????????????? ???????????????????? ??????????????????????", CHANNEL_ID);
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                                    notificationManager.notify(counter, notification);
                                    counter += d;

                                    if (!tmp_nck.equals(my_nic)) {
                                        NOTIFICATION.removeEventListener(childEventListener);
                                        if (InternetActivity.INTERLOCUTOR != null && InternetActivity.childEventListener1 != null) {
                                            InternetActivity.INTERLOCUTOR.removeEventListener(InternetActivity.childEventListener1);
                                        }
                                        oncreate(context);
                                    }
                                } else {
                                    NOTIFICATION.removeEventListener(childEventListener);
                                    oncreate(context);
                                }
                            }
                        }
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
            };
            NOTIFICATION.addChildEventListener(childEventListener);
        } catch (Exception e) {
            Log.wtf("oncreate", "Exception");
            e.printStackTrace();
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        if (hasConnection(context)) {
                            if (NOTIFICATION != null && childEventListener != null) {
                                NOTIFICATION.removeEventListener(childEventListener);
                            }
                            if (connectedRef != null && connectedEventListener != null) {
                                connectedRef.removeEventListener(connectedEventListener);
                            }
                            if (InternetActivity.INTERLOCUTOR != null && InternetActivity.childEventListener1 != null) {
                                InternetActivity.INTERLOCUTOR.removeEventListener(InternetActivity.childEventListener1);
                            }
                            oncreate(context);
                            break;
                        }
                    }
                }
            }).start();
        }
    }

    public static void removeAllNotifications(Context context) {
        NotificationManager nMgr = (NotificationManager) context.getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancelAll();
    }

    private static void createNotificationChannel(Context context, String notif_name, String description, String CHANNEL_ID) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, notif_name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    //??????????, ?????????????????????? ???????????????????? ?? ????????????????????
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

    //??????????, ???????????????????? ?????????????????? ???????? ???? ???????????? ????????????????????
    private static PrivateKey getPrivateKey(String chatId, String my_nic, Context context) throws NoSuchAlgorithmException, InvalidKeySpecException /*throws Exception*/ {

        //???????????????????? ???????? ?????????? SharedPreferences
        SharedPreferences sharedPreference_privateKey = context.getSharedPreferences("sharedPreference_privateKey" + my_nic, Context.MODE_PRIVATE);
        Gson gson_privateKey = new Gson();
        String json_privateKey = sharedPreference_privateKey.getString("task_privateKey" + my_nic, null);

        //?????? ???????????????????? ?? ?????????????? Json ???????????????????? ???????????????? ???????????????????????? ????????????????????.
        //?? ???????????? ?? ???????? ???????????? ?????????????????????? ???????????????? ????????????????
        if (json_privateKey != null) {
            json_privateKey = "{" + json_privateKey + "}";
        }

        Type type_privateKey = new TypeToken<TreeMap<String, String>>() {
        }.getType();
        Map<String, String> map = gson_privateKey.fromJson(json_privateKey, type_privateKey);

        //?? ???????????? ???????????????? ?????????? ?????? ?????????????? ??????????????.
        //???????????????? ?????????????????? ???????? ???? map, ?????????????????????? ?????? ?????????????? ?? ???????????????????? ????????????????????????.
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
                    Log.wtf("str_key", str_key);
                }
            }
        }

        //?????????????????????? ???????? ?? ???????????? ????????
        byte[] privateKeyBytes = Base64.getDecoder().decode(str_key);
        Log.wtf("pvt", "BYTE KEY" + Arrays.toString(privateKeyBytes));
        Log.wtf("pvt", "FINAL OUTPUT" + Arrays.toString(privateKeyBytes));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privatekey = keyFactory.generatePrivate(privateKeySpec);

        privateKeyBytes = privatekey.getEncoded();
        Log.wtf("privatekey (byte) e/d", Arrays.toString(privateKeyBytes));

        //???????????????????? ???????? ?????????? ?? ?????????????? "PrivateKey"
        return privatekey;
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

    //??????????, ?????????????????????? ??????????????????
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getCurrentApp() {
        try {
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(openFileInput("file_nic")));
            String my_nic = br_nn.readLine();
            BufferedReader br_f = new BufferedReader(new InputStreamReader(openFileInput("file_secret_field")));
            String secret_field = br_f.readLine();
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            SEND_CURRENT_APP = database.getReference(secret_field + "/Status/Current_activity/");
            SEND_CURRENT_APP.child(my_nic).setValue(taskInfo.get(0).topActivity.getClassName() + " -- " + componentInfo.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}