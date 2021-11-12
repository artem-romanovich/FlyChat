package com.example.fludrex;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NewContactsAdapter extends ArrayAdapter<MyContacts> {
    private LayoutInflater inflater;
    private int layout;
    private ArrayList<MyContacts> contactsList;
    private ArrayList<String> CONTACTS;

    String secret_field;
    String my_name;
    String my_nic;

    Map<String, MyMessage> map;

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
        viewHolder.last_msg.setText(getLastMsg(position));
        //viewHolder.status_contact.setText("offline");
        //viewHolder.status_contact.setTextColor(Color.parseColor("#7D7D7D"));

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
                            viewHolder.status_contact.setText("offline");
                            viewHolder.status_contact.setTextColor(Color.parseColor("#7D7D7D"));
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

    public ListAdapter sortData() {
        Collections.sort(CONTACTS);
        return this;
    }

    private class ViewHolder {
        final TextView name_contact, status_contact, last_msg;

        ViewHolder(View view) {
            name_contact = view.findViewById(R.id.name_contact);
            status_contact = view.findViewById(R.id.status_contact);
            last_msg = view.findViewById(R.id.last_msg);
        }
    }

    public String getLastMsg(int position) {

        ArrayList<MyMessage> messages = new ArrayList<MyMessage>();
        loadDataMessages(my_nic);

        String interlocutor_nic = CONTACTS.get(position);

        List<MyMessage> help_array_messages = new ArrayList<MyMessage>(map.values());
        ArrayList<String> keys = new ArrayList<String>(map.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            help_array_messages.add(map.get(keys.get(i)));
        }
        ArrayList<String> keys_after = new ArrayList<String>();
        for (int i = 0; i < keys.size(); i++) {
            if (keys.get(i).contains(("_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nic))) {
                keys_after.add(keys.get(i));
                messages.add(help_array_messages.get(i));
            } else if (keys.get(i).contains(("_(" + my_nic + ")_" + interlocutor_nic + "_" + my_nic))) {
                keys_after.add(keys.get(i));
                messages.add(help_array_messages.get(i));
            }
        }



        if (keys_after.size() == 0) {
            return "";
        } else {

            String s = "";
            if (keys_after.get(keys_after.size() - 1)
                    .contains(("_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nic))) {
                s = "Вы: ";
            } else if (keys_after.get(keys_after.size() - 1)
                    .contains(("_(" + my_nic + ")_" + interlocutor_nic + "_" + my_nic))) {
                s = contactsList.get(position).getName()+": ";
            }

            MyMessage a = map.get(keys_after.get(keys_after.size() - 1));
            String b = null;
            if (a != null) {
                b = a.getText();
                if (b.length() >= 25){
                    b = b.substring(0, 25) + "...";
                }
            }
            return s+b;
        }
    }

    public void loadDataMessages(String my_nic) {

        SharedPreferences sharedPreferencesarrayall = getContext().getSharedPreferences("sharedPreferencesarrayall" + my_nic, Context.MODE_PRIVATE);
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
}
