package com.example.fludrex.ui.account;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.fludrex.R;
import com.example.fludrex.RegistrationActivity;
import com.google.android.gms.common.util.IOUtils;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.StandardCopyOption;

import static android.content.Context.MODE_PRIVATE;

/*
    Всего в приложении используются 3 фрагмента. Все они прикреплены к BottomNavigationActivity.
    AccountFragment - один из них. Отвечает за предоставление пользователю начальной информации.
    Первый фрагмент, видимый пользователем после открытия приложения.

    Имеет 3 кнопки: "О приложении", "Благодарности" и "Сменить аккаунт".
    (См. разметку)
*/

public class AccountFragment extends Fragment {

    public String my_name = "";
    public String my_email = "";
    public String my_nic = "";

    LinearLayout linlay_gratitude, linlay_info, linlay_start, linlay_share_app, linlink;
    TextView see_email, see_name, text_info, text_gratitude, txt_info_me, see_nic;
    Button link, sign_in_no_account, button_info, gratitude_button, btn_return1, btn_return2;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        //Возвращение объекта класса View по id из разметки
        see_email = root.findViewById(R.id.see_email);
        see_name = root.findViewById(R.id.see_name);
        link = root.findViewById(R.id.link);
        see_nic = root.findViewById(R.id.see_nic);
        sign_in_no_account = root.findViewById(R.id.sign_in_no_account);
        linlay_gratitude = root.findViewById(R.id.linlay_gratitude);
        linlay_info = root.findViewById(R.id.linlay_info);
        linlay_start = root.findViewById(R.id.linlay_start);
        text_info = root.findViewById(R.id.text_info);
        text_gratitude = root.findViewById(R.id.text_gratitude);
        button_info = root.findViewById(R.id.button_info);
        gratitude_button = root.findViewById(R.id.gratitude_button);
        btn_return1 = root.findViewById(R.id.btn_return1);
        btn_return2 = root.findViewById(R.id.btn_return2);
        txt_info_me = root.findViewById(R.id.txt_info_me);
        linlay_share_app = root.findViewById(R.id.linlay_share_app);
        linlink = root.findViewById(R.id.linlink);

        gratitude_button.setBackgroundResource(R.drawable.btn_selector);
        button_info.setBackgroundResource(R.drawable.btn_selector);

        //Всего используются 3 linlay. Изначально виден главный экран.
        linlay_info.setVisibility(View.GONE);
        linlay_gratitude.setVisibility(View.GONE);
        linlay_start.setVisibility(View.VISIBLE);

        //Чтение из файлов имени и email-адреса пользователя
        try {
            BufferedReader br_nn = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_nic")));
            my_nic = br_nn.readLine();
            BufferedReader br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username" + my_nic)));
            my_name = br_n.readLine();
            BufferedReader br_e = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_email" + my_nic)));
            my_email = br_e.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Если в аккаунт пользователь в аккаунт не вошел, его об этом уведомят.
        //В противном (скорее, наоборот) случае нач. экран показывет имя и адрес почты попльзователя.
        if (my_name == null || my_email == null || my_name.equals("") || my_email.equals("") || my_nic == null || my_nic.equals("")) {
            String s = "Вы вышли из своего аккаунта. Небходимо зайти в уже существующий или создать новый аккаунт.";
            see_name.setText(s);
            see_email.setText("");
            see_nic.setText("");
            sign_in_no_account.setText("Создать аккаунт");
            see_nic.setVisibility(View.GONE);
        } else {
            sign_in_no_account.setText("Сменить аккаунт");
            String s = "Ваше имя:" + "<br /> <b>" + my_name + "</b>";
            see_name.setText(Html.fromHtml(s));
            String ss = "Адрес почты:" + "<br /> <b>" + my_email + "</b>";
            see_email.setText(Html.fromHtml(ss));
            String sss = "Никнейм:" + "<br /> <b>" + my_nic + "</b>";
            see_nic.setText(Html.fromHtml(sss));
        }

        linlay_share_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ApplicationInfo app = requireContext().getApplicationInfo();
                String filePath = app.sourceDir;

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.setPackage("com.android.bluetooth");

                Uri appUri = FileProvider.getUriForFile(
                        requireActivity(),
                        "com.example.fludrex.provider",
                        //targetFile1
                        new File(filePath));

                intent.putExtra(Intent.EXTRA_STREAM, appUri);
                startActivity(Intent.createChooser(intent, "Share app"));
            }
        });

        //Установление слушателя событий нажатия кнопки "Сменить аккаунт"
        sign_in_no_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Чтение из файлов имени и email-адреса пользователя
                try {
                    BufferedReader br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username" + my_nic)));
                    my_name = br_n.readLine();
                    BufferedReader br_e = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_email" + my_nic)));
                    my_email = br_e.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!(my_name == null || my_email == null || my_name.equals("") || my_email.equals(""))) { //При условии, что пользователь вошел в аккаунт

                    //Создание предупреждения, уточняем намерения пользователя
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Выход");
                    alert.setMessage("Вы точно хотите выйти из своего аккаунта?");
                    alert.setPositiveButton("Да", new DialogInterface.OnClickListener() { //Пользователь хочет выйти из аккаунта
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Выход из учетной записи Firebase
                            FirebaseAuth.getInstance().signOut();

                            //Очистка списка контактов и сообщений
                            //requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
                            //requireActivity().getSharedPreferences("sharedPreferencesstr", 0).edit().clear().apply();
                            //requireActivity().getSharedPreferences("sharedPreferenceschatid", 0).edit().clear().apply();

                            //requireActivity().getSharedPreferences("sharedPreferencesarray", 0).edit().clear().apply();
                            //requireActivity().getSharedPreferences("sharedPreferencesarrayall", 0).edit().clear().apply();

                            //Очистка файлов с реквизитами пользователя
                            try {
                                //BufferedWriter bn = new BufferedWriter(new OutputStreamWriter(
                                //        requireActivity().openFileOutput("file_username"+my_nic, MODE_PRIVATE)));
                                //BufferedWriter bp = new BufferedWriter(new OutputStreamWriter(
                                //        requireActivity().openFileOutput("file_password"+my_nic, MODE_PRIVATE)));
                                //BufferedWriter be = new BufferedWriter(new OutputStreamWriter(
                                //        requireActivity().openFileOutput("file_email"+my_nic, MODE_PRIVATE)));
                                BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                        requireActivity().openFileOutput("file_nic", MODE_PRIVATE)));
                                //bp.flush();
                                //bn.flush();
                                //be.flush();
                                bnn.flush();
                                bnn.close();
                                //bn.close();
                                //bp.close();
                                //be.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //Открытие RegistrationActivity и закрытие BottomNavigationActivity
                            Intent intent = new Intent(requireActivity(), RegistrationActivity.class);
                            startActivity(intent);

                            requireActivity().finish();
                        }
                    });
                    alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() { //Пользователь не хочет выйти из аккаунта
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Закрытие предупреждения
                            dialog.dismiss();
                        }
                    });
                    alert.show();

                } else { //При условии, что данные некорректно загружены или не загружены вообще (пользователь в аккаунт не вошел)

                    //Без создания предупреждения, ровно то же самое:

                    //Выход из учетной записи Firebase
                    FirebaseAuth.getInstance().signOut();

                    //Очистка списка контактов и сообщений
                    //requireActivity().getSharedPreferences("sharedPreferences", 0).edit().clear().apply();
                    //requireActivity().getSharedPreferences("sharedPreferencesstr", 0).edit().clear().apply();
                    //requireActivity().getSharedPreferences("sharedPreferenceschatid", 0).edit().clear().apply();

                    //requireActivity().getSharedPreferences("sharedPreferencesarray", 0).edit().clear().apply();
                    //requireActivity().getSharedPreferences("sharedPreferencesarrayall", 0).edit().clear().apply();

                    //Очистка файлов с реквизитами пользователя
                    try {
                        //BufferedWriter bn = new BufferedWriter(new OutputStreamWriter(
                        //        requireActivity().openFileOutput("file_username"+my_nic, MODE_PRIVATE)));
                        //BufferedWriter bp = new BufferedWriter(new OutputStreamWriter(
                        //        requireActivity().openFileOutput("file_password"+my_nic, MODE_PRIVATE)));
                        //BufferedWriter be = new BufferedWriter(new OutputStreamWriter(
                        //        requireActivity().openFileOutput("file_email"+my_nic, MODE_PRIVATE)));
                        BufferedWriter bnn = new BufferedWriter(new OutputStreamWriter(
                                requireActivity().openFileOutput("file_nic", MODE_PRIVATE)));
                        //bp.flush();
                        //bn.flush();
                        //be.flush();
                        bnn.flush();
                        bnn.close();
                        //bn.close();
                        //bp.close();
                        //be.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //Открытие RegistrationActivity и закрытие BottomNavigationActivity
                    Intent intent = new Intent(requireActivity(), RegistrationActivity.class);
                    startActivity(intent);

                    requireActivity().finish();
                }
            }
        });

        //Установление слушателя событий нажатия кнопки "О приложении"
        button_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Закрытие начального экрана, демонстрация разметки с информацией
                linlay_start.setVisibility(View.GONE);
                linlay_info.setVisibility(View.VISIBLE);
                String ss = "Здесь приведены ответы на самые распространенные вопросы.<br>" +
                        "<br>" +
                        "<b>В: Как корректно пройти регистрацию?<br></b>" +
                        "О: Необходимо нажать на красную кнопку \"создать аккаунт\" в разделе \"аккаунт\". Заполните все поля и нажмите \"получить ссылку\". На указанный почтовый адрес придет ссылка для авторизации. После нужно вернуться обратно в приложение и \"подтвердить ссылку\". Так аккаунт будет создан и подтвержден.<br>" +
                        "<br>" +
                        "<b>В: Не находятся Bluetooth-устройства поблизости.<br></b>" +
                        "О: Перед тем, как первый раз начинать искать искать устройства, обязательно вручную зайдите в настройки приложения и обновите тип доступа, как это показано в разделе \"Видео\" на сайте (Bluetooth общение).<br>" +
                        "К сожалению, это глобальная проблема версий Android >10. Также, устройства могут находяться слишком далеко друг от друга. Радиус действия зависит от версии Bluetooth (уточните для своего устройства).<br>" +
                        "Пропускная способность значительно снижается в помещении.<br>" +
                        "<br>" +
                        "<b>В: Как скрыть службу из панели уведомлений?<br></b>" +
                        "О: Данное уведомление является обязательным информированием пользователя о службе поиска новых сообщений. Его можно отключить в настройках приложения (Уведомления/FlyChat goto/отключить).";
                text_info.setText(Html.fromHtml(ss));

                //Вывод информации о приложении
                // *txt_info_me - лишь один из TextView. Остальные прописаны в разметке (нет необходимости
                //  выделять данные жирным шрифтом). См. "R.layout.fragment_account"
                String s = "Создатель и единственный владелец проекта: <br><b>Артем Романович</b><br><br>" +
                        "Почта создателя: <br><b>artrom170@gmail.com</b><br><br>" +
                        "Лицензия: <br><b>Apache License, Version 2.0</b><br><br>"+
                        "Сайт: <br><b>https://artem-romanovich.github.io/flychat_share/</b>";
                txt_info_me.setText(Html.fromHtml(s));

                link.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://artem-romanovich.github.io/flychat_share/"));
                        startActivity(browserIntent);
                    }
                });

                //при нажатии на кнопку "Назад" пользователь выходит на начальный экран
                btn_return1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        linlay_info.setVisibility(View.GONE);
                        linlay_start.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        //Установление слушателя событий нажатия кнопки "Благодарности"
        gratitude_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Закрытие начального экрана, демонстрация разметки с проявлением благодарностей
                linlay_start.setVisibility(View.GONE);
                linlay_gratitude.setVisibility(View.VISIBLE);

                //Проявление благодарностей руководителю, тестировщику и курсу!
                String s = "Хочется выразить благодарность " + "<b>" + "Мулю Павлу Фридриховичу" + "</b>" +
                        " за всестороннюю помощь и поддержку при создании приложения, а также поблагодарить " +
                        "<b>" + "Никиту Козореза" + "</b>" + " за оказание помощи при тестировании.<br><br>" +
                        "Спасибо <b>" + "Софье Докучаевой" + "</b>" + " за очаровательную иконку мессенджера.<br><br>" +
                        "Проект создан в процессе прохождения курса " + "<b>" + "SAMSUNG IT School" + "<b>" + ".<br><br>";
                text_gratitude.setText(Html.fromHtml(s));

                //при нажатии на кнопку "Назад" пользователь выходит на начальный экран
                btn_return2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        linlay_gratitude.setVisibility(View.GONE);
                        linlay_start.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        //Отрисовка пользовательского интерфейса для фрагмента
        return root;
    }
}