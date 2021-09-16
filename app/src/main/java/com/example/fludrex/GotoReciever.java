package com.example.fludrex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GotoReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent toActivityIntent = new Intent(context, BottomNavigationActivity.class);
        toActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(toActivityIntent);
    }
}