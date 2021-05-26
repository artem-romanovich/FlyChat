package com.example.fludrex;

//  ___________
//< Hello World >
//  ===========
//          \
//          \
//          ^__^
//          (oo)\_______
//          (__)\       )\/\
//          ||----w |
//          ||     ||

public class ReplaceRepeat {

    public String ReplaceRepeatStr(String string) {
        string = string.trim().replaceAll("\\s{2,}", " ");
        string = string.trim().replaceAll("\n{2,}", " ");
        return string;
    }
}
