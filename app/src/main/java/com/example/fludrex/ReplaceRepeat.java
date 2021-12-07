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
        //string = string.trim().replaceAll("\\s{2,}", " ");
        //string = string.trim().replaceAll("\n{2,}", "\n");

        string = string.trim();
        String string2 = string;

        for (int i = 0; i < string.length(); i++) {
            string2 = string2.replaceAll("  ", " ");
            string2 = string2.replaceAll("\n\n", "\n");
        }

        return string2;
    }
}
