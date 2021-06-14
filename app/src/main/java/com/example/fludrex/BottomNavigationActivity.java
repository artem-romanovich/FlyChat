package com.example.fludrex;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.snapshot.ChildKey;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class BottomNavigationActivity extends AppCompatActivity {

    DatabaseReference INTERLOCUTOR;

    ArrayList<String> MESSAGES;
    Map<String, MyMessage> map;
    ArrayList<MyMessage> messages = new ArrayList<>();

    public String my_name; //!
    public String interlocutor_name;
    public String chatId;

    private static final String CHANNEL_ID = "New channel";
    private int counter = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();

        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bluetooth,
                R.id.navigation_contacts,
                R.id.navigation_internet,
                R.id.navigation_account).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        /*final ChildEventListener childEventListener1 = INTERLOCUTOR.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                String data_snapshotValue = datasnapshot.getValue(String.class);

                Intent intent = new Intent(BottomNavigationActivity.this, BottomNavigationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(BottomNavigationActivity.this, 0, intent, 0);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(BottomNavigationActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_method_draw_image)
                        .setContentTitle(interlocutor_name)
                        .setContentText(data_snapshotValue)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setAutoCancel(true);
                Notification notification = builder.build();

                createNotificationChannel(interlocutor_name, data_snapshotValue);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BottomNavigationActivity.this);
                notificationManager.notify(counter, notification);
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
        });*/
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