package com.example.fludrex;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class NewMessageAdapterMoreLays extends ArrayAdapter<MyMessage> {
    private LayoutInflater inflater;
    private ArrayList<MyMessage> messageList;
    private ArrayList<String> MESSAGES;
    private String my_nic;

    int USER_VARIANT = 0;
    int INTERLOCUTOR_VARIANT = 1;
    int MULTI_VARIANT = 2;

    public NewMessageAdapterMoreLays(Context context, ArrayList<String> MESSAGES, ArrayList<MyMessage> messages, String my_nic) {
        super(context, R.layout.message_item_user, messages);
        this.messageList = messages;
        this.inflater = LayoutInflater.from(context);
        this.MESSAGES = MESSAGES;
        this.my_nic = my_nic;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        int t;
        try {
            if (MESSAGES.get(position).contains("_(" + my_nic + ")_" + my_nic + "_" + my_nic)) {
                t = MULTI_VARIANT;
            } else {

                if (MESSAGES.get(position).contains("_(" + my_nic + ")_" + my_nic)) {
                    t = USER_VARIANT;
                } else {
                    t = INTERLOCUTOR_VARIANT;
                }

            }
        } catch (Exception e) {
            t = MULTI_VARIANT;
            e.printStackTrace();
        }
        return t;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        int type = getItemViewType(position);
        if (convertView == null) {

            if (type == USER_VARIANT) {
                convertView = inflater.inflate(R.layout.message_item_user, parent, false);
            }
            if (type == INTERLOCUTOR_VARIANT) {
                convertView = inflater.inflate(R.layout.message_item_interlocutor, parent, false);
            }
            if (type == MULTI_VARIANT) {
                convertView = inflater.inflate(R.layout.message_item_universal, parent, false);
            }
            //
            //convertView = inflater.inflate(R.layout.message_item_universal, parent, false);
            //
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final MyMessage message = messageList.get(position);

        viewHolder.name_message.setText(message.getName());
        viewHolder.text_message.setText(message.getText());

        String s = message.getText();
        changeFontTruth(s, viewHolder);
        viewHolder.time_message.setText(message.getTime());

        return convertView;
    }

    private void changeFontTruth(String s, ViewHolder viewHolder) {
        if (s.charAt(0) == '|' && s.charAt(1) == 'r' && s.charAt(2) == '|' &&
                s.charAt(s.length() - 3) == '|' && s.charAt(s.length() - 2) == 'r' && s.charAt(s.length() - 1) == '|'
        ) {
            viewHolder.text_message.setTextColor(Color.parseColor("#EF5350"));
            viewHolder.text_message.setText(s.substring(3, s.length() - 3));
            viewHolder.text_message.setTextSize(25);
        } else if (s.charAt(0) == '|' && s.charAt(1) == 'b' && s.charAt(2) == '|' &&
                s.charAt(s.length() - 3) == '|' && s.charAt(s.length() - 2) == 'b' && s.charAt(s.length() - 1) == '|'
        ) {
            viewHolder.text_message.setTextColor(Color.parseColor("#FF03DAC5"));
            viewHolder.text_message.setText(s.substring(3, s.length() - 3));
            viewHolder.text_message.setTextSize(25);
        } else if (s.charAt(0) == '|' && s.charAt(1) == 'g' && s.charAt(2) == '|' &&
                s.charAt(s.length() - 3) == '|' && s.charAt(s.length() - 2) == 'g' && s.charAt(s.length() - 1) == '|'
        ) {
            viewHolder.text_message.setTextColor(Color.parseColor("#FBC02D"));
            viewHolder.text_message.setText(s.substring(3, s.length() - 3));
            viewHolder.text_message.setTextSize(25);
        } else if (s.charAt(0) == '|' && s.charAt(1) == 'p' && s.charAt(2) == '|' &&
                s.charAt(s.length() - 3) == '|' && s.charAt(s.length() - 2) == 'p' && s.charAt(s.length() - 1) == '|'
        ) {
            viewHolder.text_message.setTextColor(Color.parseColor("#D271E3"));
            viewHolder.text_message.setText(s.substring(3, s.length() - 3));
            viewHolder.text_message.setTextSize(25);
        } else {
            viewHolder.text_message.setTextColor(Color.parseColor("#FFFFFFFF"));
            viewHolder.text_message.setTextSize(15);
        }
    }

    private class ViewHolder {
        final TextView name_message, time_message, text_message;

        ViewHolder(View view) {
            name_message = view.findViewById(R.id.user_message);
            time_message = view.findViewById(R.id.time_message);
            text_message = view.findViewById(R.id.text_message);
        }
    }
}