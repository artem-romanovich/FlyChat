package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class MenuActivity extends AppCompatActivity {

    TextView text_name;
    EditText edit_name;

    public String name;
    public String email;
    public String password;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    //DatabaseReference test = database.getReference("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        edit_name = findViewById(R.id.name);
        text_name = findViewById(R.id.text_name);

        String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        DatabaseReference usernamebase = FirebaseDatabase.getInstance().getReference();
        usernamebase.child("Users").child(id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        UserParams userParams = dataSnapshot.getValue(UserParams.class);
                        name = userParams.name;
                        password = userParams.password;
                        email = userParams.email;
                        //String s = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                        text_name.setText("Приятного общения во FLUDREX,\n" + name + "!");
                        //text_name.setText("Приятного общения во FLUDREX!");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
        //Toast.makeText(this, data, Toast.LENGTH_LONG).show();
        //text_name.setText("Приятного общения во FLUDREX,\n" + "user" + "!");
    }

    public void CommunicationInternetClick(View view) {
        Intent intent = new Intent(MenuActivity.this, InternetActivity.class);
        intent.putExtra("eN", name);
        startActivity(intent);
    }

    public void CommunicationBluetoothClick(View view) {
        Intent intent = new Intent(MenuActivity.this, BluetoothActivity.class);
        startActivity(intent);
        //Toast.makeText(getApplicationContext(), "В разработке", Toast.LENGTH_SHORT).show();
    }

    public void AccountClick(View view) {
        Intent intent = new Intent(MenuActivity.this, SignIn.class);
        startActivity(intent);
    }

    public void ContactsClick(View view) {
        Intent intent = new Intent(MenuActivity.this, ContactsActivity.class);
        startActivity(intent);
    }

    public void SettingsClick(View view) {
        Toast.makeText(getApplicationContext(),
                "В разработке", Toast.LENGTH_SHORT).show();
    }
}