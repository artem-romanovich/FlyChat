package com.example.fludrex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.Html;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class NewContactsAdapter extends ArrayAdapter<MyContacts> {
    private LayoutInflater inflater;
    private int layout;
    private ArrayList<MyContacts> contactsList;
    private ArrayList<String> CONTACTS;

    String secret_field;
    String my_name;
    String my_nic;

    public NewContactsAdapter(Context context, int resource, ArrayList<MyContacts> contacts, ArrayList<String> CONTACTS) throws IOException {
        super(context, resource, contacts);
        this.contactsList = contacts;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        this.CONTACTS = CONTACTS;

        BufferedReader br_f = new BufferedReader(new InputStreamReader(context.openFileInput("file_secret_field")));
        secret_field = br_f.readLine();
        BufferedReader br_nn = new BufferedReader(new InputStreamReader(context.openFileInput("file_nic")));
        my_nic = br_nn.readLine();
        BufferedReader br_n = new BufferedReader(new InputStreamReader(context.openFileInput("file_username" + my_nic)));
        my_name = br_n.readLine();
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
        final String contact = CONTACTS.get(position);

        viewHolder.name_contact.setText(String.format(" %s ", contacts.getName()));
        viewHolder.status_contact.setText("offline");
        viewHolder.status_contact.setTextColor(Color.parseColor("#7D7D7D"));

        //Получение доступа к БД Firebase. Определение времени последнего посещения приложения собеседником.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference last_time_userRef = database.getReference(secret_field + "/Online_Users/" + contact + "/" + contact);
        last_time_userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (contact.equals(my_nic)) {
                        viewHolder.status_contact.setText("это вы");
                        viewHolder.status_contact.setTextColor(Color.parseColor("#31708E"));
                    } else {

                        //Если значение - "online"
                        try {
                            String date = dataSnapshot.getValue(String.class);
                            if (date.equals("online")) {
                                viewHolder.status_contact.setText("online");
                                viewHolder.status_contact.setTextColor(Color.parseColor("#4CAF50"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //Если значение - TimeStamp (См. onStop у ContactsFragment) и переводим в дату.
                        try {
                            Long date = dataSnapshot.getValue(Long.class);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    //Если значение - "online"
                                    try {
                                        String date = dataSnapshot.getValue(String.class);
                                        if (date.equals("online")) {
                                            viewHolder.status_contact.setText("online");
                                            viewHolder.status_contact.setTextColor(Color.parseColor("#4CAF50"));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        Long date = dataSnapshot.getValue(Long.class);
                                        viewHolder.status_contact.setText("offline");
                                        viewHolder.status_contact.setTextColor(Color.parseColor("#7D7D7D"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
