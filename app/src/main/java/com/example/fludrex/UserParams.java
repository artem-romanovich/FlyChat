package com.example.fludrex;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserParams {
    public String name;
    public String email;
    public String password;

    public UserParams() {}
}
