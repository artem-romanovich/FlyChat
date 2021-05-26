package com.example.fludrex.ui.internet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.fludrex.CurrentTime;
import com.example.fludrex.InternetActivity;
import com.example.fludrex.MyMessage;
import com.example.fludrex.NewMessageAdapter;
import com.example.fludrex.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class InternetFragment extends Fragment {

    public static int max_message_length = 2000;
    public static int max_name_length = 17;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference messageRef = database.getReference("Internet_Messages");
    DatabaseReference USER = database.getReference("Internet_Messages");
    DatabaseReference INTERLOCUTOR = database.getReference("Internet_Messages");
    DatabaseReference USER_name = database.getReference("Internet_Messages/user_name");
    DatabaseReference INTERLOCUTOR_name = database.getReference("Internet_Messages/interlocutor_name");

    EditText get_message;
    Button btn_edit_message, btn_set_name;
    ArrayList<String> MESSAGES = new ArrayList<>();

    public String sent_message;
    public String my_name = "USER1"; //!!!!!!!!!!!!!!
    public String interlocutor_name = "Птичка";
    public String user_name = "Артем";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_internet, container, false);

        ArrayList<MyMessage> messages = new ArrayList<MyMessage>();
        ListView messagesList = (ListView) rootView.findViewById(R.id.messages_listview);
        NewMessageAdapter adapter = new NewMessageAdapter(getActivity(), 1, messages);
        messagesList.setAdapter(adapter);

        btn_edit_message = rootView.findViewById(R.id.btn_edit_message);
        btn_set_name = rootView.findViewById(R.id.btn_set_name);
        get_message = rootView.findViewById(R.id.get_message);

        /*my_name = "Птичка";

        btn_edit_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sent_message = get_message.getText().toString();

                if (sent_message.equals("") || (sent_message.length() > max_message_length)) {
                    Toast.makeText(getActivity(),
                            "Слишком большое (либо пустое) сообщение", Toast.LENGTH_SHORT).show();
                } else {

                    CurrentTime currentTime = new CurrentTime();
                    currentTime.getCurrentTime();
                    messages.add(new MyMessage(my_name, sent_message, currentTime.currenttime));
                    adapter.notifyDataSetChanged();
                    messagesList.smoothScrollToPosition(MESSAGES.size());

                    currentTime.getCurrentTimeComma();
                    messageRef.child(my_name).push().setValue(
                            sent_message + " | " + currentTime.currenttime);

                    if (my_name.equals(user_name)) {
                        USER.child("user_messages").setValue(sent_message);
                        InternetFragment.this.USER.push().setValue(sent_message);
                    }
                    if (my_name.equals(interlocutor_name)) {
                        INTERLOCUTOR.child("interlocutors_messages").setValue(sent_message);
                        InternetFragment.this.INTERLOCUTOR.push().setValue(sent_message);
                    }

                    get_message.setText("");
                }
            }
        });

        final ChildEventListener childEventListener1 = INTERLOCUTOR.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                if (my_name.equals(user_name)) {
                    String data_snapshotValue = datasnapshot.getValue(String.class);
                    MESSAGES.add(data_snapshotValue);

                    CurrentTime currentTime = new CurrentTime();
                    currentTime.getCurrentTime();
                    messages.add(new MyMessage(interlocutor_name, data_snapshotValue, currentTime.currenttime));

                    adapter.notifyDataSetChanged();
                    messagesList.smoothScrollToPosition(MESSAGES.size());
                }
            }
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onCancelled(DatabaseError error) {}

        });

        final ChildEventListener childEventListener2 = USER.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot datasnapshot, String previousChildName) {
                if (my_name.equals(interlocutor_name)) {
                    String data_snapshotValue = datasnapshot.getValue(String.class);
                    MESSAGES.add(data_snapshotValue);

                    CurrentTime currentTime = new CurrentTime();
                    currentTime.getCurrentTime();
                    messages.add(new MyMessage(user_name, data_snapshotValue, currentTime.currenttime));

                    adapter.notifyDataSetChanged();
                    messagesList.smoothScrollToPosition(MESSAGES.size());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onChildRemoved(DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            @Override
            public void onCancelled(DatabaseError error) {}

        });
*/
        return rootView;
    }
}