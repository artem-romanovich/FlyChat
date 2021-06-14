package com.example.fludrex.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.fludrex.R;
import com.example.fludrex.RegistrationActivity;
import com.example.fludrex.SignIn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class AccountFragment extends Fragment {

    public String my_name = "";
    public String my_email = "";

    TextView see_email, see_name;
    Button sign_in_no_account, change_theme;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        see_email = root.findViewById(R.id.see_email);
        see_name = root.findViewById(R.id.see_name);
        //change_theme = root.findViewById(R.id.change_theme);
        sign_in_no_account = root.findViewById(R.id.sign_in_no_account);

        try {
            BufferedReader br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username")));
            my_name = br_n.readLine();
            BufferedReader br_e = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_email")));
            my_email = br_e.readLine();

            String s = "Ваше имя:" + "<br /> <b>" + my_name + "</b>";
            see_name.setText(Html.fromHtml(s));
            String ss = "Адрес почты:" + "<br /> <b>" + my_email + "</b>";
            see_email.setText(Html.fromHtml(ss));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //see_name.setText(String.format("Ваше имя: %s", my_name));
        //see_email.setText(String.format("Адрес почты: %s", my_email));

        sign_in_no_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), SignIn.class);
                startActivity(intent);
            }
        });

        return root;
    }
}