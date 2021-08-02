package com.example.fludrex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NotificationReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String get_user_name = intent.getStringExtra("nameinterlocutor");
        String get_user_nick = intent.getStringExtra("current_interlocutor");
        String get_user_chat = intent.getStringExtra("current_chat");

        Log.wtf("nameinterlocutor", get_user_name);
        Log.wtf("current_interlocutor", get_user_nick);
        Log.wtf("current_chat", get_user_chat);

        Intent toActivityIntent = new Intent(context, InternetActivity.class);
        toActivityIntent.putExtra("nameinterlocutor", get_user_name);
        toActivityIntent.putExtra("current_interlocutor", get_user_nick);
        toActivityIntent.putExtra("current_chat", get_user_chat);
        toActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(toActivityIntent);

        //MessageListeningService.removeNotification(context, 0);

        try {
            BufferedReader br_f = new BufferedReader(new InputStreamReader(context.openFileInput("file_secret_field")));
            String secret_field = br_f.readLine();
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(context.openFileInput("file_nic")));
            String my_nic = br_nn.readLine();

            /*DatabaseReference MessageRemove = FirebaseDatabase.getInstance().getReference(secret_field + "/Notifications/" + my_nic);
            ValueEventListener remove_message_listener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postsnapshot : dataSnapshot.getChildren()) {
                        dataSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            MessageRemove.orderByKey().addListenerForSingleValueEvent(remove_message_listener);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}