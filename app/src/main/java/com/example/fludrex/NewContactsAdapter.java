package com.example.fludrex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

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
        if (convertView == null) {
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final MyContacts contacts = contactsList.get(position);

        viewHolder.name_contact.setText(String.format(" %s ", contacts.getName()));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference last_time_userRef = database.getReference("Online_Users/" + contacts.getName() + "/" + contacts.getName());
        last_time_userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    try {
                        String date = dataSnapshot.getValue(String.class);
                        assert date != null;
                        if (date.equals("online")) {
                            viewHolder.status_contact.setText("online");
                            viewHolder.status_contact.setTextColor(Color.parseColor("#4CAF50"));
                        } else {
                            //viewHolder.status_contact.setText(date);
                            viewHolder.status_contact.setText("offline");
                            viewHolder.status_contact.setTextColor(Color.parseColor("#7D7D7D"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        return convertView;
    }

    private class ViewHolder {
        final TextView name_contact, status_contact;

        ViewHolder(View view) {
            name_contact = view.findViewById(R.id.name_contact);
            status_contact = view.findViewById(R.id.status_contact);
        }
    }
}
