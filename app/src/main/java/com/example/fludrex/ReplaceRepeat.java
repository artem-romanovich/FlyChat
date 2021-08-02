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

    //Необходим для очищения String-переменных от лишнего мусора - пробелов в начале и конце

    public String ReplaceRepeatStr(String string) {
        string = string.trim().replaceAll("\\s{2,}", " ");
        string = string.trim().replaceAll("\n{2,}", " ");
        return string;
    }
}
