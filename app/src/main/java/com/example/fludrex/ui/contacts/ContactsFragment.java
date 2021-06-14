package com.example.fludrex.ui.contacts;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fludrex.InternetActivity;
import com.example.fludrex.MyContacts;
import com.example.fludrex.NewContactsAdapter;
import com.example.fludrex.R;
import com.example.fludrex.RegistrationActivity;
import com.example.fludrex.ReplaceRepeat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.ContentValues.TAG;
import static android.content.Context.ACTIVITY_SERVICE;

public class ContactsFragment extends Fragment {

    EditText find_username1, find_useremail1;
    Button btn_find_user1;
    LinearLayout linlay_bar, linlay_list, linlay_btn;

    public String user_name;
    public String user_email;
    public String user_password;
    ArrayList<String> CONTACTS;
    ArrayList<String> CHAT_NUMBER;
    ArrayList<MyContacts> contacts;

    public String name;
    public String my_name;
    public String my_password;
    public String my_email;
    public int flag;
    public int FLAG;
    public String chatId;

    private FirebaseAuth mAuth;

    private final String file_name = "file_username";
    private final String file_password = "file_password";
    private final String file_email = "file_email";

    public int my_version = 1;              //!!!
    public int last_version;
    public String status;

    SwipeRefreshLayout mySwipeRefreshLayout;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    public FirebaseUser currentUser;
    DatabaseReference ONLINE_USERS;
    DatabaseReference CAPABLE;
    DatabaseReference VERSION;
    DatabaseReference CHAT_users;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        find_username1 = (EditText) rootView.findViewById(R.id.find_username1);
        find_useremail1 = (EditText) rootView.findViewById(R.id.find_usernumber1);
        btn_find_user1 = (Button) rootView.findViewById(R.id.btn_find_user1);
        linlay_bar = (LinearLayout) rootView.findViewById(R.id.linlay_bar);
        linlay_btn = (LinearLayout) rootView.findViewById(R.id.linlay_btn);
        linlay_list = (LinearLayout) rootView.findViewById(R.id.linlay_list);
        mySwipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);

        linlay_bar.setVisibility(View.VISIBLE);
        linlay_list.setVisibility(View.GONE);
        linlay_btn.setVisibility(View.GONE);

        if (hasConnection(requireActivity())) {
            Toast.makeText(requireActivity(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
            requireActivity().finish();

        } else {
            try {
                BufferedReader br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput(file_name)));
                my_name = br_n.readLine();
                BufferedReader br_p = new BufferedReader(new InputStreamReader(requireActivity().openFileInput(file_password)));
                my_password = br_p.readLine();
                BufferedReader br_e = new BufferedReader(new InputStreamReader(requireActivity().openFileInput(file_email)));
                my_email = br_e.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (my_email == null || my_name == null || my_password == null) {
                Intent intent = new Intent(requireActivity(), RegistrationActivity.class);
                startActivity(intent);
            } else {
                mAuth = FirebaseAuth.getInstance();
                signIn(my_email, my_password);

                currentUser = mAuth.getCurrentUser();
                ONLINE_USERS = database.getReference("Online_Users/" + my_name);
                ONLINE_USERS.child(my_name).setValue("online");

                CAPABLE = database.getReference("Status/Capable");
                final ChildEventListener childEventListener1 = CAPABLE.addChildEventListener(new ChildEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                        if (datasnapshot.exists()) {
                            status = datasnapshot.getValue(String.class);
                            Log.wtf("Capable", status);
                            if (status.equals("NO")) {
                                Toast.makeText(requireActivity(),
                                        "Ведутся работы, приложение недоступно", Toast.LENGTH_LONG).show();
                                requireActivity().finish();
                            }
                            if (status.equals("YES")) {

                                VERSION = database.getReference("Status/Version");
                                final ChildEventListener childEventListener2 = VERSION.addChildEventListener(new ChildEventListener() {
                                    @SuppressLint("ResourceType")
                                    @RequiresApi(api = Build.VERSION_CODES.O)
                                    @Override
                                    public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                                        last_version = datasnapshot.getValue(Integer.class);
                                        Log.wtf("Version", String.valueOf(last_version));

                                        String[] versionnumber = {" ", "(первая) ", "(вторая) ", "(третья) ", "(четвертая) ", "(пятая) "};
                                        String[] versionnumber2 = {"", "первой", "второй", "третьей", "четвертой", "пятой"};

                                        if (last_version != my_version) {
                                            if (last_version < versionnumber2.length) {
                                                Toast.makeText(requireActivity(),
                                                        "Неактуальная " + versionnumber[my_version] + "версия приложения. " +
                                                                "Требуется обновление до " + versionnumber2[last_version], Toast.LENGTH_LONG).show();
                                                requireActivity().finish();
                                            } else {
                                                Toast.makeText(requireActivity(),
                                                        "Неактуальная версия приложения. Требуется обновление", Toast.LENGTH_LONG).show();
                                                requireActivity().finish();
                                            }
                                        } else {

                                            /*if (my_email == null || my_password == null) {
                                                Intent intent = new Intent(requireActivity(), SignIn.class);
                                                startActivity(intent);
                                            } else {
                                                mAuth = FirebaseAuth.getInstance();
                                                signIn(my_email, my_password);
                                            }*/

                                            loadData();
                                            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                            //contacts = new ArrayList<>();
                                            //CONTACTS = new ArrayList<>();

                                            ListView listView = (ListView) rootView.findViewById(R.id.contacts_listview1);
                                            NewContactsAdapter adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item, contacts);
                                            listView.setAdapter(adapter);
                                            adapter.notifyDataSetChanged();

                                            linlay_bar.setVisibility(View.GONE);
                                            linlay_list.setVisibility(View.VISIBLE);
                                            linlay_btn.setVisibility(View.VISIBLE);

                                            btn_find_user1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    if (hasConnection(requireActivity())) {
                                                        Toast.makeText(requireActivity(), "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show();
                                                    } else {

                                                        btn_find_user1.setClickable(false);

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
                                                                btn_find_user1.setClickable(true);
                                                            } else {

                                                                if (!CONTACTS.contains(user_name)) {
                                                                    flag = 0;
                                                                    FLAG = 0;
                                                                    DatabaseReference UserSearch = FirebaseDatabase.getInstance().getReference("Contacts");
                                                                    UserSearch.orderByChild(user_name).equalTo(user_email).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                                                                                String status = "offline";

                                                                                contacts.add(new MyContacts(user_name, status));
                                                                                CONTACTS.add(user_name);
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
                                                                                btn_find_user1.setClickable(true);
                                                                            } else {
                                                                                Toast.makeText(getActivity(), "Пользователь " + user_name + " успешно найден!", Toast.LENGTH_SHORT).show();
                                                                                btn_find_user1.setClickable(true);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(@NotNull DatabaseError databaseError) {
                                                                            btn_find_user1.setClickable(true);
                                                                        }
                                                                    });
                                                                } else {
                                                                    Toast.makeText(getActivity(), "Пользователь уже есть в вашем списке", Toast.LENGTH_SHORT).show();
                                                                    btn_find_user1.setClickable(true);
                                                                }
                                                            }
                                                        } else {
                                                            Toast.makeText(getActivity(), "Пустое поле ввода", Toast.LENGTH_SHORT).show();
                                                            btn_find_user1.setClickable(true);
                                                        }
                                                    }
                                                }
                                            });

                                            mySwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.parseColor("#FFFFFFFF"));
                                            mySwipeRefreshLayout.setColorSchemeColors(Color.parseColor("#FF5085A5"));
                                            mySwipeRefreshLayout.setOnRefreshListener(
                                                    new SwipeRefreshLayout.OnRefreshListener() {
                                                        @Override
                                                        public void onRefresh() {
                                                            ListView listView = (ListView) rootView.findViewById(R.id.contacts_listview1);
                                                            NewContactsAdapter adapter = new NewContactsAdapter(getActivity(), R.layout.contact_item, contacts);
                                                            listView.setAdapter(adapter);
                                                            adapter.notifyDataSetChanged();

                                                            Handler handler = new Handler();
                                                            handler.postDelayed(new Runnable() {
                                                                public void run() {
                                                                    mySwipeRefreshLayout.setRefreshing(false);
                                                                }
                                                            }, 700);
                                                        }
                                                    }
                                            );

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
                                                    if (hasConnection(requireActivity())) {
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
                                        }
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot snapshot) {
                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                    }
                                });
                            }
                        } else {
                            Log.wtf("Capable", "!incapable!");
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot snapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }
        }

        return rootView;
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return false;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return false;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo == null || !wifiInfo.isConnected();
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(requireActivity(),
                                    "Авторизация оборвалась. Попробуйте еще раз", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ContactsFragment.this.requireActivity(), RegistrationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    }
                });
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

    @Override
    public void onStop() {
        super.onStop();
        try {
            ActivityManager am = (ActivityManager) requireContext().getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            Log.d("CURRENT Activity", taskInfo.get(0).topActivity.getClassName());
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (!componentInfo.getClassName().equals("com.example.fludrex.InternetActivity")) {

                currentUser = mAuth.getCurrentUser();
                ONLINE_USERS = database.getReference("Online_Users/" + my_name);
                ONLINE_USERS.child(my_name).setValue(ServerValue.TIMESTAMP);

                final DatabaseReference connectedRef = database.getReference(".info/connected");
                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        if (connected) {
                            final DatabaseReference last_time_userRef = database.getReference("Online_Users/" + my_name + "/" + my_name);
                            last_time_userRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    try {

                                        long date = snapshot.getValue(Long.class);
                                        Log.wtf("currenttime", String.valueOf(date));
                                        Date myDate = new Date(date);
                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
                                        String currenttime = dateFormat.format(myDate);
                                        Log.wtf("currenttime", currenttime);
                                        ONLINE_USERS.child(my_name).setValue(currenttime);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w(TAG, "Listener was cancelled at .info/connected");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}