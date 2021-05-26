package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class SignIn extends AppCompatActivity {

    EditText sign_set_password, sign_set_email, sign_name;

    private final String file_name = "file_username";
    private final String file_password = "file_password";
    private final String file_email = "file_email";

    private FirebaseAuth mAuth;

    public String name;
    public String password;
    public String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        sign_set_password = findViewById(R.id.sign_password);
        sign_set_email = findViewById(R.id.sign_email);
        sign_name = findViewById(R.id.sign_name);

        /*try {
            String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            Log.wtf(TAG, id);
            DatabaseReference usernamebase = FirebaseDatabase.getInstance().getReference();
            usernamebase.child("Users").child(id).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                            UserParams userParams = dataSnapshot.getValue(UserParams.class);
                            //name = userParams.name;
                            SignIn.this.name = userParams.name;
                            SignIn.this.password = userParams.password;
                            SignIn.this.email = userParams.email;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(SignIn.this,
                    "Авторизация неуспешна", Toast.LENGTH_SHORT).show();
        }*/
    }

    public void signIn(String email, String password, String name) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference UserSearch = FirebaseDatabase.getInstance().getReference("Contacts");
                            UserSearch.orderByChild(name).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        try {

                                            Toast.makeText(SignIn.this,
                                                    "Авторизация успешна", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(SignIn.this, BottomNavigationActivity.class);
                                            startActivity(intent);

                                            try {
                                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                                        openFileOutput(file_name, MODE_PRIVATE)));
                                                bw.flush();
                                                bw.write(name);
                                                bw.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            try {
                                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                                        openFileOutput(file_password, MODE_PRIVATE)));
                                                bw.flush();
                                                bw.write(password);
                                                bw.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            try {
                                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                                        openFileOutput(file_email, MODE_PRIVATE)));
                                                bw.flush();
                                                bw.write(email);
                                                bw.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });

                        } else {
                            Toast.makeText(SignIn.this, "Авторизация не произошла", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signClick(View view) {
        mAuth = FirebaseAuth.getInstance();
        password = sign_set_password.getText().toString();
        email = sign_set_email.getText().toString();
        name = sign_name.getText().toString();

        ReplaceRepeat replaceRepeat = new ReplaceRepeat();
        password = replaceRepeat.ReplaceRepeatStr(password);
        email = replaceRepeat.ReplaceRepeatStr(email);
        name = replaceRepeat.ReplaceRepeatStr(name);

        signIn(email, password, name);
    }

}