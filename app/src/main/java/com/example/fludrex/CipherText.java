package com.example.fludrex;

import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class CipherText {

    //Ранее мессенджер поддерживал симметричное шифроание, данный класс был нужен для кодировки/декодировки текста.
    //Затем в приложении появилось асимметричное шифрование по алгоритму RSA, и в данном классе необходимость отпала.

    public byte[] cipherText(byte[] input, String password) throws InvalidKeyException,
            NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, IOException {

        password = "hlZ84aaP2sgcec9CT8eSy4PPNDLDNS5B3p5ZblE29VM";

        Log.wtf("processCypher", "Coding: " + input);
        Log.wtf("processCypher", "password:" + password);
        byte[] secretText = codeText(input, password);
        byte[] text_to_return = new byte[0];

        if (!input.equals(decodeText(secretText, password))) {
            // Расшифровка текста приводит к другому результату.
            Log.wtf("processCypher", "Internal error while coding.");
        } else {
            text_to_return = secretText;
        }
        Log.wtf("processCypher", Arrays.toString(text_to_return));
        return text_to_return;
    }

    public byte[] codeText(byte[] input, String key) throws IOException,
            InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException {
        // пароль для DES шифрования должен быть как минимум 8 символов.
        // Обработайте ошибку так, чтобы пользователь мог использовать
        // и более короткие пароли.
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        SecretKey desKey = skf.generateSecret(dks);
        // DES/ECB/PKCS5Padding for SunJCE
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, desKey);
        CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(
                input), cipher);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[64];
        int numBytes;
        while ((numBytes = cis.read(buffer)) != -1) {
            output.write(buffer, 0, numBytes);
        }
        // Приводим все байты в кодировку, в которой используются только
        // латинские буквы, цифры и некоторые знаки препинания.
        return Base64.encode(output.toByteArray(), Base64.NO_PADDING | Base64.NO_WRAP);
    }

    public String decodeText(byte[] input, String key) throws IOException,
            InvalidKeyException, NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException {
        // Выделите создание секретного ключа в отдельную
        // функцию для уменьшения дублирования кода.
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        SecretKey desKey = skf.generateSecret(dks);
        // DES/ECB/PKCS5Padding for SunJCE
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, desKey);
        CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(
                Base64.decode(input, Base64.NO_PADDING | Base64.NO_WRAP)), cipher);
        // Выделите копирование содержание потока в
        // строку в отдельный метод.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[64];
        int numBytes;
        while ((numBytes = cis.read(buffer)) != -1) {
            output.write(buffer, 0, numBytes);
        }
        return output.toString();
    }
}
