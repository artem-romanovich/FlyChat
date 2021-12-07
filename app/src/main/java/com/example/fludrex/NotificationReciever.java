package com.example.fludrex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

        MessageListeningService.removeAllNotifications(context);
    }
}