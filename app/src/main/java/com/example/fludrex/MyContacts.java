package com.example.fludrex;

public class MyContacts {
    private String name;

    public MyContacts(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "{\"name\":\"" + name + "\"},\n";
    }

    public String getName() {
        return name;
    }
}

