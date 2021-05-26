package com.example.fludrex.ui.contacts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fludrex.CurrentTime;
import com.example.fludrex.InternetActivity;
import com.example.fludrex.MyContacts;
import com.example.fludrex.NewContactsAdapter;
import com.example.fludrex.R;
import com.example.fludrex.ReplaceRepeat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class ContactsFragment extends Fragment {

    EditText find_username1, find_useremail1;
    Button btn_find_user1;

    public String user_name;
    public String user_email;
    ArrayList<String> CONTACTS;
    ArrayList<String> CHAT_NUMBER;
    ArrayList<MyContacts> contacts;

    public String name;
    public String my_name;
    public String email;
    public int flag;
    public int FLAG;
    public String chatId;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference CHAT;
    DatabaseReference CHAT_users;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        find_username1 = (EditText) rootView.findViewById(R.id.find_username1);
        find_useremail1 = (EditText) rootView.findViewById(R.id.find_usernumber1);
        btn_find_user1 = (Button) rootView.findViewById(R.id.btn_find_user1);

        loadData();
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //contacts = new ArrayList<>();
        //CONTACTS = new ArrayList<>();

        ListView listView = (ListView) rootView.findViewById(R.id.contacts_listview1);
        NewContactsAdapter adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item, contacts);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        BufferedReader br_n = null;
        try {
            br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert br_n != null;
            my_name = br_n.readLine();
            user_name = my_name;
        } catch (IOException e) {
            e.printStackTrace();
        }

        btn_find_user1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_name = find_username1.getText().toString();
                user_email = find_useremail1.getText().toString();

                ReplaceRepeat replaceRepeat = new ReplaceRepeat();
                user_email = replaceRepeat.ReplaceRepeatStr(user_email);
                user_name = replaceRepeat.ReplaceRepeatStr(user_name);

                if (!user_email.equals("") &&
                        !user_email.equals(" ") &&
                        !user_email.equals("/n") &&
                        !user_name.equals("") &&
                        !user_name.equals(" ") &&
                        !user_name.equals("/n")) {

                    if (user_name.equals(my_name)) {
                        Toast.makeText(getActivity(), "Невозможно добавить себя в список контактов", Toast.LENGTH_SHORT).show();
                    } else {

                        //if (!contacts.contains(user_name) && !contacts.contains(user_email)) {}
                        //if (!CONTACTS.contains(user_name)) {}
                        //Toast.makeText(getActivity(), CONTACTS.toString(), Toast.LENGTH_SHORT).show();

                        if (!CONTACTS.contains(user_name)) {
                            flag = 0;
                            FLAG = 0;
                            DatabaseReference UserSearch = FirebaseDatabase.getInstance().getReference("Contacts");
                            UserSearch.orderByChild(user_name).equalTo(user_email).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        name = user_name;
                                        contacts.add(new MyContacts(name));
                                        CONTACTS.add(name);
                                        adapter.notifyDataSetChanged();
                                        flag = 1;
                                        find_useremail1.setText("");
                                        find_username1.setText("");

                                        chatId = "Chat_" + my_name + "_" + user_name;
                                        DatabaseReference ChatSearch1 = FirebaseDatabase.getInstance().getReference("Internet_Messages");
                                        DatabaseReference ChatSearchRef = ChatSearch1.child("Chat_" + user_name + "_" + my_name);
                                        ValueEventListener eventListener = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()) {
                                                    CHAT_users = database.getReference("Internet_Messages/" + chatId);
                                                    CHAT_users.child("chat_created").setValue("success");
                                                    //ContactsFragment.this.CHAT_users.push().setValue(user_name);
                                                    //ContactsFragment.this.CHAT_users.push().setValue(my_name);
                                                    chatId = "Chat_" + my_name + "_" + user_name;
                                                    Log.wtf(TAG, "!dataSnapshot.exists()");
                                                } else {
                                                    chatId = "Chat_" + user_name + "_" + my_name;
                                                    Log.wtf(TAG, "dataSnapshot.exists()");
                                                }
                                                CHAT_NUMBER.add(chatId);
                                                saveData();
                                                Log.wtf(TAG, String.valueOf(CHAT_NUMBER));
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        };
                                        ChatSearchRef.addListenerForSingleValueEvent(eventListener);
                                    }
                                    if (flag == 0) {
                                        Toast.makeText(getActivity(), "Пользователь " + user_name + " не найден", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Пользователь " + user_name + " успешно найден!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NotNull DatabaseError databaseError) {
                                    Toast.makeText(getActivity(), "Отсутствует подключение к интернету", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else
                            Toast.makeText(getActivity(), "Пользователь уже есть в вашем списке", Toast.LENGTH_SHORT).show();
                    }
                } else Toast.makeText(getActivity(), "Пустое поле ввода", Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeItemFromList(position);
                return true;
            }

            private void removeItemFromList(int position) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Удаление");
                alert.setMessage("Вы точно хотите удалить данного пользователя из списка своих контактов?");
                alert.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contacts.remove(adapter.getItem(position));
                        CONTACTS.remove(position);
                        CHAT_NUMBER.remove(position);
                        adapter.notifyDataSetChanged();
                        adapter.notifyDataSetInvalidated();
                        saveData();
                    }
                });
                alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                if (!hasConnection(requireActivity())) {
                    Toast.makeText(getActivity(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(ContactsFragment.this.getActivity(), InternetActivity.class);
                    String current_interlocutor = CONTACTS.get(position);
                    intent.putExtra("current_interlocutor", current_interlocutor);
                    String current_chat = CHAT_NUMBER.get(position);
                    intent.putExtra("current_chat", current_chat);
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }

    private void saveData() {
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contacts);
        if (json != null) {
            json = json.substring(1, json.length() - 1);
        }
        editor.putString("task", json);
        editor.apply();

        SharedPreferences sharedPreferencesstr = this.requireActivity().getSharedPreferences("sharedPreferencesstr", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesstr", 0).edit().clear().apply();
        SharedPreferences.Editor editorstr = sharedPreferencesstr.edit();
        Gson gsonstr = new Gson();
        String jsonstr = gsonstr.toJson(CONTACTS);
        editorstr.putString("taskstr", jsonstr);
        editorstr.apply();

        SharedPreferences sharedPreferenceschatid = this.requireActivity().getSharedPreferences("sharedPreferenceschatid", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferenceschatid", 0).edit().clear().apply();
        SharedPreferences.Editor editorchatid = sharedPreferenceschatid.edit();
        Gson gsonchatid = new Gson();
        String jsonchatid = gsonchatid.toJson(CHAT_NUMBER);
        editorchatid.putString("taskchatid", jsonchatid);
        editorchatid.apply();

        Log.wtf(TAG, json);
        Log.wtf(TAG, jsonstr);
        Log.wtf(TAG, jsonchatid);
    }

    private void loadData() {
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task", null);
        if (json != null) {
            json = "[" + json + "]";
        }
        Type type = new TypeToken<ArrayList<MyContacts>>() {
        }.getType();
        contacts = gson.fromJson(json, type);
        if (contacts == null) {
            contacts = new ArrayList<>();
        }

        SharedPreferences sharedPreferencesstr = this.requireActivity().getSharedPreferences("sharedPreferencesstr", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesstr", 0).edit().clear().apply();
        Gson gsonstr = new Gson();
        String jsonstr = sharedPreferencesstr.getString("taskstr", null);
        Type typestr = new TypeToken<ArrayList<String>>() {
        }.getType();
        CONTACTS = gsonstr.fromJson(jsonstr, typestr);
        if (CONTACTS == null) {
            CONTACTS = new ArrayList<>();
        }

        SharedPreferences sharedPreferenceschatid = this.requireActivity().getSharedPreferences("sharedPreferenceschatid", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferenceschatid", 0).edit().clear().apply();
        Gson gsonchatid = new Gson();
        String jsonchatid = sharedPreferenceschatid.getString("taskchatid", null);
        Type typechatid = new TypeToken<ArrayList<String>>() {
        }.getType();
        CHAT_NUMBER = gsonchatid.fromJson(jsonchatid, typechatid);
        if (CHAT_NUMBER == null) {
            CHAT_NUMBER = new ArrayList<>();
        }

        Log.wtf(TAG, json);
        Log.wtf(TAG, jsonstr);
        Log.wtf(TAG, jsonchatid);
    }
}