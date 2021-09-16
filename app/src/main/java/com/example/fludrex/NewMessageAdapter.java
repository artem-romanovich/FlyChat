package com.example.fludrex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class NewMessageAdapter extends ArrayAdapter<MyMessage> {
    private LayoutInflater inflater;
    private int layout;
    private ArrayList<MyMessage> messageList;

    public NewMessageAdapter(Context context, int IorU, ArrayList<MyMessage> messages) {
        super(context, IorU, messages);
        this.messageList = messages;
        this.layout = R.layout.message_item_user;
        this.inflater = LayoutInflater.from(context);
        if (IorU == 1) this.layout = R.layout.message_item_user;
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final MyMessage message = messageList.get(position);

        viewHolder.name_message.setText(message.getName());
        viewHolder.text_message.setText(message.getText());
        viewHolder.time_message.setText(message.getTime());

        return convertView;
    }
    private class ViewHolder {
        final TextView name_message, time_message, text_message;
        ViewHolder(View view){
            name_message = view.findViewById(R.id.user_message);
            time_message = view.findViewById(R.id.time_message);
            text_message = view.findViewById(R.id.text_message);
        }
    }
}