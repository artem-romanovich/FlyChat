package com.example.fludrex.ui.bluetooth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.fludrex.BottomNavigationActivity;
import com.example.fludrex.CurrentTime;
import com.example.fludrex.InternetActivity;
import com.example.fludrex.MyBluetoothDevices;
import com.example.fludrex.MyMessage;
import com.example.fludrex.NewBluetoothAdapter;
import com.example.fludrex.NewMessageAdapterMoreLays;
import com.example.fludrex.R;
import com.example.fludrex.ReplaceRepeat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/*
    Всего в приложении используются 3 фрагмента. Все они прикреплены к BottomNavigationActivity.
    BluetoothFragment - один из них. Отвечает за Bluetooth-взаимодействие.
    Доступен по нажатию на соответствующую иконку в BottomNavigation.

    Имеет кнопку "Поиск устройств", отображает список найденных при помощи ListView.
    (См. разметку)

    Разработчик в виду критически малого количества информации по данной теме в сети обращался ко следующей статье:
    http://www.mobilab.ru/androiddev/bluetoothinandroid.html
        и open-source проекту BluetoothChat от Google.
*/

public class BluetoothFragment extends Fragment {

    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    String TAG = "BluetoothFragment";

    int max_message_length = 2500;
    String interlocutor_name;
    String interlocutor_nick;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECIEVED = 5;

    public SendRecieve sendRecieve;

    public ProgressBar progress_horizontal;
    public Button button_podcl, btn_agree_finding;
    public LinearLayout linlay_after_getting_agreement, linlay_sending_msg, linlay_finding_connecting, linlay_status_permission;
    public TextView txt_next_is_finding, txt_no_devices_available, current_blt_interlocutor, text_permission;
    public EditText get_blt_message;
    ImageButton btn_edit_blt_message;

    public ArrayList<MyBluetoothDevices> devices;
    public ListView devicesList;
    public NewBluetoothAdapter adapter;
    public ArrayList<String> DEVICES;
    public ArrayList<String> DEVICES_NAME_ONLY;

    public ArrayList<MyBluetoothDevices> connected_devices;
    public ListView connected_devicesList, messages_blt_listview;
    public NewBluetoothAdapter connected_adapter;
    public ArrayList<String> connected_DEVICES;

    public ArrayList<BluetoothDevice> BluetoothDevices;

    ListView messagesList;
    public ArrayList<MyMessage> messages = new ArrayList<>();
    public ArrayList<String> MESSAGES;
    public Map<String, MyMessage> map;
    NewMessageAdapterMoreLays madapter;
    BluetoothAdapter mBtAdapter;

    String my_nic;

    @SuppressLint("CutPasteId")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        //Возвращение объекта класса View по id из разметки
        button_podcl = root.findViewById(R.id.button_podcl);
        progress_horizontal = root.findViewById(R.id.progress_horizontal);
        linlay_after_getting_agreement = root.findViewById(R.id.linlay_after_getting_agreement);
        btn_agree_finding = root.findViewById(R.id.btn_agree_finding);
        txt_next_is_finding = root.findViewById(R.id.txt_next_is_finding);
        txt_no_devices_available = root.findViewById(R.id.txt_no_devices_available);

        button_podcl.setBackgroundResource(R.drawable.btn_selector);

        linlay_sending_msg = root.findViewById(R.id.linlay_sending_msg);
        current_blt_interlocutor = root.findViewById(R.id.current_blt_interlocutor);
        messages_blt_listview = root.findViewById(R.id.messages_blt_listview);
        get_blt_message = root.findViewById(R.id.get_blt_message);
        btn_edit_blt_message = root.findViewById(R.id.btn_edit_blt_message);
        linlay_finding_connecting = root.findViewById(R.id.linlay_finding_connecting);

        text_permission = root.findViewById(R.id.text_permission);
        linlay_status_permission = root.findViewById(R.id.linlay_status_permission);

        messagesList = root.findViewById(R.id.messages_blt_listview);

        try {

            BufferedReader br_nn = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_nic")));
            my_nic = br_nn.readLine();

            /*messages = new ArrayList<MyMessage>();
                //MESSAGES = new ArrayList<>();
                //loadDataMessages();
                loadDataMessages(my_nic);

                List<MyMessage> help_array_messages = new ArrayList<MyMessage>(map.values());
                ArrayList<String> keys = new ArrayList<String>(map.keySet());
                Collections.sort(keys);
                for (int i = 0; i < keys.size(); i++) {
                    help_array_messages.add(map.get(keys.get(i)));
                    //Log.wtf("Соответствие map и MESSAGES", keys.get(i) + " - " + MESSAGES.get(i));
                }
                ArrayList<String> keys_after = new ArrayList<String>();
                for (int i = 0; i < keys.size(); i++) {
                    if (keys.get(i).contains(("_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nick))) {
                        Log.wtf("Вывод help_array_message", (help_array_messages.get(i)) + " (me_to_interlocutor) " + keys.get(i) + " " + i);
                        keys_after.add(keys.get(i));
                        messages.add(help_array_messages.get(i));
                    } else if (keys.get(i).contains(("_(" + my_nic + ")_" + interlocutor_nick + "_" + my_nic))) {
                        Log.wtf("Вывод help_array_message", (help_array_messages.get(i)) + " (interlocutor_to_me) " + keys.get(i) + " " + i);
                        keys_after.add(keys.get(i));
                        messages.add(help_array_messages.get(i));
                    }
                }
                MESSAGES.clear();
                for (int i = 0; i < keys_after.size(); i++) {
                    MESSAGES.add(keys_after.get(i));
                    Log.wtf("Вывод keys_after", keys_after.get(i));
                }

                messagesList = root.findViewById(R.id.messages_blt_listview);
                madapter = new NewMessageAdapterMoreLays(requireContext(), MESSAGES, messages, my_nic, 1);                   //!!!!!!!!!!!
                messagesList.setAdapter(madapter);
                madapter.notifyDataSetChanged();*/
            //messagesList.setSelection(MESSAGES.size());

            //Установление адаптера для вывода информации о найденных устройствах
            devices = new ArrayList<MyBluetoothDevices>();

            devicesList = root.findViewById(R.id.available_devices);
            adapter = new NewBluetoothAdapter(requireActivity(), R.layout.blt_device_item, devices);
            devicesList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            DEVICES = new ArrayList<>();
            DEVICES_NAME_ONLY = new ArrayList<>();

            connected_devices = new ArrayList<MyBluetoothDevices>();
            connected_devicesList = root.findViewById(R.id.connected_devices);
            connected_adapter = new NewBluetoothAdapter(requireActivity(), R.layout.blt_device_item, connected_devices);
            connected_devicesList.setAdapter(connected_adapter);
            connected_adapter.notifyDataSetChanged();
            connected_DEVICES = new ArrayList<>();

            BluetoothDevices = new ArrayList<>();

            progress_horizontal.setVisibility(View.GONE);
            linlay_after_getting_agreement.setVisibility(View.GONE);
            linlay_sending_msg.setVisibility(View.GONE);
            linlay_status_permission.setVisibility(View.GONE);

            btn_agree_finding.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Запрос разрешения на перевод телефона в режим видимости для других телефонов (на 300 СЕКУНД)
                    boolean hasDiscoverablePermission = bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
                    if (!hasDiscoverablePermission) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoverableIntent);

                        //btn_agree_finding.setText("Нажмите, чтобы проверить разрешения\n\nПри необходимости выйдите и снова зайдите во вкладку \"Bluetooth\"");
                    } else {
                        //linlay_finding_connecting.setVisibility(View.GONE);
                        btn_agree_finding.setVisibility(View.GONE);
                        devicesList.setVisibility(View.GONE);
                        connected_devicesList.setVisibility(View.GONE);
                        //linlay_status_permission.setVisibility(View.VISIBLE);

                        ServerClass serverClass;
                        try {
                            serverClass = new ServerClass();
                            serverClass.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Thread t = new Thread(() -> {
                            while (true) {
                                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {

                                    Intent intent = new Intent(requireActivity(), BottomNavigationActivity.class);
                                    intent.putExtra("toast", "2");
                                    startActivity(intent);
                                    break;
                                }
                            }
                        });
                        t.start();

                        //Читаем файл, пробуем получить имя пользователя
                        String my_name_set;
                        try {

                            BufferedReader br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username" + my_nic)));
                            my_name_set = br_n.readLine();
                            bluetoothAdapter.setName(my_name_set + " (" + my_nic + ")");

                            //linlay_finding_connecting.setVisibility(View.VISIBLE);
                            //linlay_status_permission.setVisibility(View.GONE);
                            linlay_after_getting_agreement.setVisibility(View.VISIBLE);

                            //Для отладки: вывод имени пользователя с MAC-адресом. Либо сообщение об отключенном Bluetooth-е.
                            /*String status;
                            if (bluetoothAdapter.isEnabled()) {
                                @SuppressLint("HardwareIds")
                                String mydeviceaddress = bluetoothAdapter.getAddress();
                                String mydevicename = bluetoothAdapter.getName();
                                int state = bluetoothAdapter.getState();
                                status = mydevicename + ":" + mydeviceaddress + " : " + state;
                            } else {
                                status = "Bluetooth выключен";
                            }
                            Log.wtf("my_name", status);*/

                            //При необходимости можно вывести список уже спаренных (когда либо в прошлом) устройств.
                            //Не следует путать понятие СОЕДИНЕННЫХ и СПАРЕНЫХ устройств.
                            //Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                            /*if (pairedDevices.size() > 0) {
                                for (BluetoothDevice device : pairedDevices) {
                                    adapter.add(new MyBluetoothDevices(device.getName() + " (" + device.getAddress() + ")"));
                                    DEVICES.add(device.getName() + " (" + device.getAddress() + ")");
                                }
                            }*/

                            //Создание экземпляра класса BluetoothAdapter необходимо для любой работы с Bluetooth
                            mBtAdapter = BluetoothAdapter.getDefaultAdapter();

                            //Установление слушателя событий нажатия кнопки "Поиск устройств"
                            button_podcl.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //При выключенном GPS (доступ к местоположению) запрос на включение
                                    LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
                                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    } else {

                                        txt_next_is_finding.setVisibility(View.GONE);
                                        devicesList.setVisibility(View.VISIBLE);

                                        //Временно отключаем кнопку и делаем видимым горизонтальный progressbar
                                        progress_horizontal.setVisibility(View.VISIBLE);
                                        button_podcl.setClickable(false);

                                        //Сканирование радиодиапазона для поиска имеющихся в нем устройств.
                                        if (mBtAdapter.isDiscovering()) {
                                            mBtAdapter.cancelDiscovery();
                                            Log.wtf(TAG, "Устройство имеет соединение");
                                        }
                                        mBtAdapter.startDiscovery();

                                        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                                            @Override
                                            public void onReceive(Context context, Intent intent) {
                                                String action = intent.getAction();
                                                Log.wtf(TAG, "startDiscovery");

                                                //Когда найдено новое устройство
                                                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                                                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                                    String device_name = device.getName() + " (" + device.getAddress() + ")";

                                                    //Если такого устройства еще нет, добавляем в список
                                                    if (!DEVICES.contains(device_name) && device.getName() != null && !device.getName().equals("")) {
                                                        adapter.add(new MyBluetoothDevices(device.getName()));
                                                        DEVICES.add(device_name);
                                                        DEVICES_NAME_ONLY.add(device.getName());
                                                        BluetoothDevices.add(device);
                                                        Log.wtf(TAG, "Устройство найдено");
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        };

                                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                                        requireActivity().registerReceiver(broadcastReceiver, filter);

                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                if (mBtAdapter != null) {
                                                    mBtAdapter.cancelDiscovery();
                                                }
                                                LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(broadcastReceiver);

                                                progress_horizontal.setVisibility(View.GONE);
                                                button_podcl.setClickable(true);
                                            }
                                        }, 15 * 1000);
                                    }
                                }
                            });

                            //Условие простого нажатия на элемент списка (клика)
                            devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {

                                    try {
                                        ClientClass clientClass = new ClientClass(BluetoothDevices.get(position), position);
                                        clientClass.start();

                                        Message message = Message.obtain();
                                        message.what = STATE_CONNECTING;
                                        handler.sendMessage(message);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireActivity(), "Перед использованием приложения требуется регистрация", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(requireActivity(), BottomNavigationActivity.class);
            startActivity(intent);
            e.printStackTrace();
        }
        //Отрисовка пользовательского интерфейса для фрагмента
        return root;
    }

    /*BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.wtf(TAG, "startDiscovery");

            //Когда найдено новое устройство
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String device_name = device.getName() + " (" + device.getAddress() + ")";

                //Если такого устройства еще нет, добавляем в список
                if (!DEVICES.contains(device_name) && device.getName() != null && !device.getName().equals("")) {
                    adapter.add(new MyBluetoothDevices(device.getName()));
                    DEVICES.add(device_name);
                    DEVICES_NAME_ONLY.add(device.getName());
                    BluetoothDevices.add(device);
                    Log.wtf(TAG, "Устройство найдено");
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };*/

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case STATE_LISTENING:
                    break;

                case STATE_CONNECTING:
                    break;

                case STATE_CONNECTED:
                    try {
                        Toast.makeText(requireActivity(), "Соединено", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATE_CONNECTION_FAILED:
                    try {
                        Toast.makeText(requireActivity(), "Соединение не удалось", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case STATE_MESSAGE_RECIEVED:

                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    //Toast.makeText(requireActivity(), tempMsg, Toast.LENGTH_SHORT).show();

                    CurrentTime currentTime = new CurrentTime();
                    String get_time = currentTime.getCurrentTimeFromBase(0);

                    String get_time_To_look =
                            get_time.substring(8, 10) + "." +
                                    get_time.substring(10, 12) + "\n" +
                                    get_time.substring(6, 8) + "." +
                                    get_time.substring(4, 6) + "." +
                                    get_time.substring(0, 4);

                    messages.add(new MyMessage(interlocutor_name, tempMsg, get_time_To_look));
                    MESSAGES.add(get_time + "_(" + my_nic + ")_" + interlocutor_nick + "_" + my_nic);
                    //Обновляем адаптер
                    map.put(get_time + "_(" + my_nic + ")_" + interlocutor_nick + "_" + my_nic, new MyMessage(interlocutor_name, tempMsg, get_time_To_look));
                    saveDataMessages(my_nic);
                    madapter.notifyDataSetChanged();
                    messagesList.smoothScrollToPosition(MESSAGES.size());

                    //Читаем файл, пробуем получить имя пользователя
                    String my_name = "";
                    BufferedReader br_n = null;
                    try {
                        br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username" + my_nic)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        assert br_n != null;
                        my_name = br_n.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /*String checkingtxtmsg = tempMsg.substring(tempMsg.length()-9);
                    Toast.makeText(requireActivity(), checkingtxtmsg, Toast.LENGTH_SHORT).show();

                    if (checkingtxtmsg.equals("-cnctrqst")) {

                    } else {
                        Toast.makeText(requireActivity(), tempMsg, Toast.LENGTH_SHORT).show();
                    }
                    break;*/
            }
            return true;
        }
    });

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() throws IOException {

            //Читаем файл, пробуем получить имя пользователя
            String my_device_name = "";
            BufferedReader br_n = null;
            BufferedReader br_nn = null;
            try {
                br_nn = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_nic")));
                String my_nic = br_nn.readLine();

                br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username" + my_nic)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                my_device_name = br_n.readLine();
            } catch (Exception e) {
                Toast.makeText(requireActivity(), "Для использования Bluetooth сперва зарегистрируйтесь в мессенджере", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(requireActivity(), BottomNavigationActivity.class);
                startActivity(intent);
                requireActivity().finish();
                e.printStackTrace();
            }

            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(my_device_name, MY_UUID);
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();

                } catch (IOException e) {

                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {

                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    try {
                        sendRecieve = new SendRecieve(socket);
                        sendRecieve.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothSocket socket;
        private int pos;
        private String device_name;

        public ClientClass(BluetoothDevice device, int position) throws IOException {

            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            pos = position;
            device_name = device.getName();
        }

        public void run() {
            try {
                socket.connect();

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!connected_DEVICES.contains(DEVICES_NAME_ONLY.get(pos))) {
                            connected_adapter.add(new MyBluetoothDevices(DEVICES_NAME_ONLY.get(pos)));
                            connected_DEVICES.add(DEVICES_NAME_ONLY.get(pos));
                            Log.wtf(TAG, "Соединение с устройством найдено");
                            connected_adapter.notifyDataSetChanged();

                            adapter.remove(adapter.getItem(pos));
                            DEVICES.remove(pos);
                            DEVICES_NAME_ONLY.remove(pos);
                            BluetoothDevices.remove(pos);

                            txt_no_devices_available.setVisibility(View.GONE);
                            connected_devicesList.setVisibility(View.VISIBLE);

                            connected_devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {

                                    /*String chosen_device = connected_DEVICES.get(position);
                                    intent.putExtra("chosen_device", chosen_device);
                                    intent.putExtra("pos_device", String.valueOf(pos));
                                    startActivity(intent);*/

                                    linlay_finding_connecting.setVisibility(View.GONE);
                                    linlay_sending_msg.setVisibility(View.VISIBLE);

                                    int index_left_bracket = device_name.indexOf("(");
                                    interlocutor_name = device_name.substring(0, index_left_bracket - 1);
                                    interlocutor_nick = device_name.substring(index_left_bracket + 1, device_name.length() - 1);

                                    String s = "Выбранное устройство:<br>" + "<b>" + device_name + "</b>";
                                    current_blt_interlocutor.setText(Html.fromHtml(s));

                                    messages = new ArrayList<MyMessage>();
                                    //MESSAGES = new ArrayList<>();
                                    //loadDataMessages();
                                    loadDataMessages(my_nic);

                                    List<MyMessage> help_array_messages = new ArrayList<MyMessage>(map.values());
                                    ArrayList<String> keys = new ArrayList<String>(map.keySet());
                                    Collections.sort(keys);
                                    for (int i = 0; i < keys.size(); i++) {
                                        help_array_messages.add(map.get(keys.get(i)));
                                        //Log.wtf("Соответствие map и MESSAGES", keys.get(i) + " - " + MESSAGES.get(i));
                                    }
                                    ArrayList<String> keys_after = new ArrayList<String>();
                                    for (int i = 0; i < keys.size(); i++) {
                                        if (keys.get(i).contains(("_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nick))) {
                                            Log.wtf("Вывод help_array_message", (help_array_messages.get(i)) + " (me_to_interlocutor) " + keys.get(i) + " " + i);
                                            keys_after.add(keys.get(i));
                                            messages.add(help_array_messages.get(i));
                                        } else if (keys.get(i).contains(("_(" + my_nic + ")_" + interlocutor_nick + "_" + my_nic))) {
                                            Log.wtf("Вывод help_array_message", (help_array_messages.get(i)) + " (interlocutor_to_me) " + keys.get(i) + " " + i);
                                            keys_after.add(keys.get(i));
                                            messages.add(help_array_messages.get(i));
                                        }
                                    }
                                    MESSAGES.clear();
                                    for (int i = 0; i < keys_after.size(); i++) {
                                        MESSAGES.add(keys_after.get(i));
                                        Log.wtf("Вывод keys_after", keys_after.get(i));
                                    }

                                    madapter = new NewMessageAdapterMoreLays(requireContext(), MESSAGES, messages, my_nic);                   //!!!!!!!!!!!
                                    messagesList.setAdapter(madapter);
                                    madapter.notifyDataSetChanged();

                                    //Читаем файл, пробуем получить имя пользователя
                                    String my_name = "";
                                    try {
                                        BufferedReader br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username" + my_nic)));
                                        my_name = br_n.readLine();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    /*for (int i = 0; i < keys.size(); i++) {
                                        if (keys.get(i).contains((my_name + "_" + interlocutor_name))) {
                                            Log.wtf("Вывод help_array_message", String.valueOf(help_array_messages.get(i)) + " (mi)");
                                            madapter.add(help_array_messages.get(i));
                                        } else if (keys.get(i).contains((interlocutor_name + "_" + my_name))) {
                                            Log.wtf("Вывод help_array_message", String.valueOf(help_array_messages.get(i)) + " (im)");
                                            madapter.add(help_array_messages.get(i));
                                        }
                                    }*/

                                    btn_edit_blt_message.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            String sent_message = get_blt_message.getText().toString();

                                            ReplaceRepeat replaceRepeat = new ReplaceRepeat();
                                            sent_message = replaceRepeat.ReplaceRepeatStr(sent_message);

                                            if (sent_message.equals("") ||
                                                    sent_message.equals(" ") ||
                                                    sent_message.equals("/n") ||
                                                    (sent_message.length() > max_message_length)) {

                                                //Если сообщение слишком большое, пользователь об этом до недавнего времени просто уведомлялся.
                                                //Однако теперь в приложении невозможно написать текст длиною более, чем max_message_length = 2500 символов
                                                if (sent_message.length() > max_message_length) {
                                                    Toast.makeText(requireActivity(),
                                                            "Слишком большое (либо пустое) сообщение", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {

                                                CurrentTime currentTime = new CurrentTime();
                                                String currenttime = currentTime.getCurrentTimeFromBase(0);

                                                String get_time_To_look =
                                                        currenttime.substring(8, 10) + "." +
                                                                currenttime.substring(10, 12) + "\n" +
                                                                currenttime.substring(6, 8) + "." +
                                                                currenttime.substring(4, 6) + "." +
                                                                currenttime.substring(0, 4);

                                                //Читаем файл, пробуем получить имя пользователя
                                                String my_name = "";
                                                BufferedReader br_n = null;
                                                try {
                                                    br_n = new BufferedReader(new InputStreamReader(requireActivity().openFileInput("file_username" + my_nic)));
                                                } catch (FileNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                                try {
                                                    assert br_n != null;
                                                    my_name = br_n.readLine();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                                madapter.add(new MyMessage(my_name, sent_message, get_time_To_look));
                                                MESSAGES.add(currenttime + "_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nick);
                                                map.put(currenttime + "_(" + my_nic + ")_" + my_nic + "_" + interlocutor_nick, new MyMessage(my_name, sent_message, get_time_To_look));
                                                saveDataMessages(my_nic);
                                                madapter.notifyDataSetChanged();
                                                messagesList.smoothScrollToPosition(MESSAGES.size());
                                                get_blt_message.setText("");

                                                /*madapter.add(new MyMessage(my_name, sent_message, get_time_To_look));
                                                MESSAGES.add(currenttime + "_" + my_name + "_" + interlocutor_name);
                                                map.put(currenttime + "_" + my_name + "_" + interlocutor_name, new MyMessage(my_name, sent_message, get_time_To_look));
                                                //saveDataMessages();
                                                saveDataMessages(my_nic);
                                                madapter.notifyDataSetChanged();
                                                messagesList.smoothScrollToPosition(MESSAGES.size());
                                                get_blt_message.setText("");*/

                                                try {
                                                    sendRecieve.write(sent_message.getBytes());
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();

            } catch (IOException e) {
                e.printStackTrace();

                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    public class SendRecieve extends Thread {
        public final BluetoothSocket bluetoothSocket;
        public final InputStream inputStream;
        public final OutputStream outputStream;

        public SendRecieve(BluetoothSocket socket) throws IOException {
            bluetoothSocket = socket;
            InputStream tempIn;
            OutputStream tempOut;

            tempIn = bluetoothSocket.getInputStream();
            tempOut = bluetoothSocket.getOutputStream();

            inputStream = tempIn;
            outputStream = tempOut;

        }

        public void run() {
            byte[] buffer = new byte[2048];
            int bytes;

            while (true) {
                try {

                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) throws IOException {

            outputStream.write(bytes);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mBtAdapter != null) {
                mBtAdapter.cancelDiscovery();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Метод, сохраняющий сообщения
    public void saveDataMessages() {

        SharedPreferences sharedPreferencesarrayblt = requireActivity().getSharedPreferences("sharedPreferencesarrayblt", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarrayblt", 0).edit().clear().apply();
        SharedPreferences.Editor editorarrayblt = sharedPreferencesarrayblt.edit();
        Gson gsonarrayblt = new Gson();
        String jsonarrayblt = gsonarrayblt.toJson(MESSAGES);
        if (jsonarrayblt != null) {
            jsonarrayblt = jsonarrayblt.substring(1, jsonarrayblt.length() - 1);
        }
        editorarrayblt.putString("taskarrayblt", jsonarrayblt);
        editorarrayblt.apply();

        SharedPreferences sharedPreferencesarrayallblt = requireActivity().getSharedPreferences("sharedPreferencesarrayallblt", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarrayallblt", 0).edit().clear().apply();
        SharedPreferences.Editor editorarrayallblt = sharedPreferencesarrayallblt.edit();
        Gson gsonarrayallblt = new Gson();
        String jsonarrayallblt = gsonarrayallblt.toJson(map);
        if (jsonarrayallblt != null) {
            jsonarrayallblt = jsonarrayallblt.substring(1, jsonarrayallblt.length() - 1);
        }
        editorarrayallblt.putString("taskarrayallblt", jsonarrayallblt);
        editorarrayallblt.apply();
    }

    //Метод, загружающий сообщения
    public void loadDataMessages() {

        SharedPreferences sharedPreferencesarrayblt = requireActivity().getSharedPreferences("sharedPreferencesarrayblt", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarrayblt", 0).edit().clear().apply();
        Gson gsonarrayblt = new Gson();
        String jsonarrayblt = sharedPreferencesarrayblt.getString("taskarrayblt", null);
        if (jsonarrayblt != null) {
            jsonarrayblt = "[" + jsonarrayblt + "]";
        }
        Type typearrayblt = new TypeToken<ArrayList<String>>() {
        }.getType();
        MESSAGES = gsonarrayblt.fromJson(jsonarrayblt, typearrayblt);
        if (MESSAGES == null) {
            MESSAGES = new ArrayList<>();
        }

        SharedPreferences sharedPreferencesarrayallblt = requireActivity().getSharedPreferences("sharedPreferencesarrayallblt", Context.MODE_PRIVATE);
        //this.requireActivity().getSharedPreferences("sharedPreferencesarrayall", 0).edit().clear().apply();
        Gson gsonarrayallblt = new Gson();
        String jsonarrayallblt = sharedPreferencesarrayallblt.getString("taskarrayallblt", null);
        if (jsonarrayallblt != null) {
            jsonarrayallblt = "{" + jsonarrayallblt + "}";
        }
        Type typearrayallblt = new TypeToken<TreeMap<String, MyMessage>>() {
        }.getType();
        map = gsonarrayallblt.fromJson(jsonarrayallblt, typearrayallblt);
        if (map == null) {
            map = (Map<String, MyMessage>) new TreeMap<String, MyMessage>();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        onDestroy();
    }

    //Метод, сохраняющий сообщения
    public void saveDataMessages(String my_nic) {

        SharedPreferences sharedPreferencesarray = requireActivity().getSharedPreferences("sharedPreferencesarray" + my_nic, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorarray = sharedPreferencesarray.edit();
        Gson gsonarray = new Gson();
        String jsonarray = gsonarray.toJson(MESSAGES);
        if (jsonarray != null) {
            jsonarray = jsonarray.substring(1, jsonarray.length() - 1);
        }
        editorarray.putString("taskarray" + my_nic, jsonarray);
        editorarray.apply();

        SharedPreferences sharedPreferencesarrayall = requireActivity().getSharedPreferences("sharedPreferencesarrayall" + my_nic, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorarrayall = sharedPreferencesarrayall.edit();
        Gson gsonarrayall = new Gson();
        String jsonarrayall = gsonarrayall.toJson(map);
        if (jsonarrayall != null) {
            jsonarrayall = jsonarrayall.substring(1, jsonarrayall.length() - 1);
        }
        editorarrayall.putString("taskarrayall" + my_nic, jsonarrayall);
        editorarrayall.apply();
    }

    //Метод, загружающий сообщения
    public void loadDataMessages(String my_nic) {

        SharedPreferences sharedPreferencesarray = requireActivity().getSharedPreferences("sharedPreferencesarray" + my_nic, Context.MODE_PRIVATE);
        Gson gsonarray = new Gson();
        String jsonarray = sharedPreferencesarray.getString("taskarray" + my_nic, null);
        if (jsonarray != null) {
            jsonarray = "[" + jsonarray + "]";
        }
        Type typearray = new TypeToken<ArrayList<String>>() {
        }.getType();
        MESSAGES = gsonarray.fromJson(jsonarray, typearray);
        if (MESSAGES == null) {
            MESSAGES = new ArrayList<>();
        }

        SharedPreferences sharedPreferencesarrayall = requireActivity().getSharedPreferences("sharedPreferencesarrayall" + my_nic, Context.MODE_PRIVATE);
        Gson gsonarrayall = new Gson();
        String jsonarrayall = sharedPreferencesarrayall.getString("taskarrayall" + my_nic, null);
        if (jsonarrayall != null) {
            jsonarrayall = "{" + jsonarrayall + "}";
        }
        Type typearrayall = new TypeToken<TreeMap<String, MyMessage>>() {
        }.getType();
        map = gsonarrayall.fromJson(jsonarrayall, typearrayall);
        if (map == null) {
            map = (Map<String, MyMessage>) new TreeMap<String, MyMessage>();
        }
    }
}















































