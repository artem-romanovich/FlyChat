package com.example.fludrex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    EditText find_username, find_useremail;
    Button btn_find_user, btn_save_finduser;

    public String user_name;
    public String user_email;
    ArrayList<String> CONTACTS = new ArrayList<>();

    public String name;
    public String email;
    public int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        find_username = findViewById(R.id.find_username);
        find_useremail = findViewById(R.id.find_usernumber);
        btn_find_user = findViewById(R.id.btn_find_user);

        ArrayList<MyContacts> contacts = new ArrayList<MyContacts>();
        ListView contactsList = (ListView) findViewById(R.id.contacts_listview);
        NewContactsAdapter adapter = new NewContactsAdapter(this, R.layout.contact_item, contacts);
        contactsList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        loadData();

        btn_save_finduser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        btn_find_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_name = find_username.getText().toString();
                user_email = find_useremail.getText().toString();

                if (!CONTACTS.contains(user_name)) {

                    flag = 0;

                    DatabaseReference UserSearch = FirebaseDatabase.getInstance().getReference("Contacts");
                    UserSearch.orderByChild(user_name).equalTo(user_email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                //name = userSnapshot.child(user_name).getValue(String.class);
                                name = user_name;
                                contacts.add(new MyContacts(name, ""));
                                adapter.notifyDataSetChanged();
                                contactsList.smoothScrollToPosition(CONTACTS.size());
                                CONTACTS.add(name);
                                flag = 1;
                            }
                            if (flag == 0) {
                                Toast.makeText(getApplicationContext(), "Пользователь " + user_name + " не найден", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Пользователь " + user_name + " успешно найден!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(CONTACTS);
        editor.putString("task list", json);
        editor.apply();
        Toast.makeText(getApplicationContext(),
                "Изменения сохранены", Toast.LENGTH_SHORT).show();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        CONTACTS = gson.fromJson(json, type);

        if (CONTACTS == null) {
            CONTACTS = new ArrayList<>();
        }
    }

    /*public void FindUserClick(View view) throws FirebaseAuthException {
        user_name = find_username.getText().toString();
        user_email = find_useremail.getText().toString();

        DatabaseReference UserSearch = FirebaseDatabase.getInstance().getReference("Contacts");
        UserSearch.orderByChild(user_name).equalTo(user_email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Toast.makeText(getApplicationContext(),"privet!", Toast.LENGTH_SHORT).show();

                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    name = userSnapshot.child(user_name).getValue(String.class);
                    Toast.makeText(getApplicationContext(),
                        name, Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

        //contacts.add(new MyContacts(name));

        *//*UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(user_email);
        //System.out.println("Successfully fetched user data: " + userRecord.getEmail());
        String uid = userRecord.getUid();
        DatabaseReference finduserbase = FirebaseDatabase.getInstance().getReference();
        finduserbase.child("Users").child(uid).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        UserParams userParams = dataSnapshot.getValue(UserParams.class);
                        name = userParams.name;
                        email = userParams.email;
                        ArrayList<MyContacts> contacts = new ArrayList<MyContacts>();
                        contacts.add(new MyContacts(name + " " + email));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }*/
}