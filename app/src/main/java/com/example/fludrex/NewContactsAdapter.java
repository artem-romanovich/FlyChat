package com.example.fludrex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class NewContactsAdapter extends ArrayAdapter<MyContacts> {
    private LayoutInflater inflater;
    private int layout;
    private ArrayList<MyContacts> contactsList;

    public NewContactsAdapter(Context context, int resource, ArrayList<MyContacts> contacts) {
        super(context, resource, contacts);
        this.contactsList = contacts;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
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
        final MyContacts contacts = contactsList.get(position);
        viewHolder.name_contact.setText(contacts.getName());
        return convertView;
    }

    class ViewHolder {
        final TextView name_contact;
        ViewHolder(View view){
            name_contact = view.findViewById(R.id.name_contact);
        }
    }
}
