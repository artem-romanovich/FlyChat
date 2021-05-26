package com.example.fludrex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/*
public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    EditText send_data;
    TextView view_data;
    
    String device_name = "_";

    ArrayList<String> DEVICES = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        send_data = (EditText) findViewById(R.id.editText);
        view_data = (TextView) findViewById(R.id.textView);

        ArrayList<MyBluetoothDevices> devices = new ArrayList<MyBluetoothDevices>();
        ListView devicesList = (ListView) findViewById(R.id.available_devices);
        NewBluetoothAdapter adapter = new NewBluetoothAdapter(this, R.layout.contact_item, devices);
        devicesList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            */
/*Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // Если список спаренных устройств не пуст
            if (pairedDevices.size() > 0) {
                // проходимся в цикле по этому списку
                for (BluetoothDevice device : pairedDevices) {
                    // Добавляем имена и адреса в mArrayAdapter, чтобы показать
                    // через ListView
                    devices.add(new MyBluetoothDevices(device.getName() + "\n" + device.getAddress()));
                }
            }*//*


            // Создаем BroadcastReceiver для ACTION_FOUND
            final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    // Когда найдено новое устройство
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Получаем объект BluetoothDevice из интента
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        device_name = device.getName();
                        //Добавляем имя и адрес в array adapter, чтобы показвать в ListView
                        devices.add(new MyBluetoothDevices(device.getName() + "\n" + device.getAddress()));
                    }
                }
            };
            // Регистрируем BroadcastReceiver
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter); // Не забудьте снять регистрацию в onDestroy
        }
    }









    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;

        public void AcceptThread(){
        // используем вспомогательную переменную, которую в дальнейшем свяжем с mmServerSocket,
            BluetoothServerSocket tmp = null;
            try{
               // MY_UUID это UUID нашего приложения, это же значение используется в клиентском приложении
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(device_name, MY_UUID);
            } catch(IOException e){}
            mmServerSocket = tmp;
        }

        public void run(){
            BluetoothSocket socket = null;
            // ждем пока не произойдет ошибка или не будет возвращен сокет
            while(true){
                try{
                    socket = mmServerSocket.accept();
                } catch(IOException e){
                    break;
                }
                // если соединение было подтверждено
                if(socket != null){
                    // управлчем соединением (в отдельном потоке)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void manageConnectedSocket(BluetoothSocket socket) {}

        */
/** отмена ожидания сокета *//*

        public void cancel(){
            try{
                mmServerSocket.close();
            } catch(IOException e){}
        }
    }










    public void Click(View view) {
        String status;
        if (bluetoothAdapter.isEnabled()) {
            String mydeviceaddress = bluetoothAdapter.getAddress();
            String mydevicename = bluetoothAdapter.getName();
            int state = bluetoothAdapter.getState();
            status = mydevicename + ":" + mydeviceaddress + " : " + state;
        } else {
            status = "Bluetooth выключен";
        }

        Toast.makeText(this, status, Toast.LENGTH_LONG).show();
    }

    public void Click2(View view) {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
        startActivity(discoverableIntent);
    }

    public void Click3(View view) {

    }
}*/


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import android.bluetooth.*;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

/*public class BluetoothActivity extends AppCompatActivity {
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ConnectedThread mConnectedThread;
    private Handler handler;
    String TAG = "MainActivity";
    public EditText send_data;
    public TextView view_data;
    StringBuilder messages;

    public void pairDevice1(View v) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            Object[] devices = pairedDevices.toArray();
            BluetoothDevice device = (BluetoothDevice) devices[0];
            ConnectThread connect = new ConnectThread(device, MY_UUID_INSECURE);
            connect.start();
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        public ConnectThread(BluetoothDevice device, UUID uuid) {
            mmDevice = device;
            deviceUUID = uuid;
        }
        public void run() {
            BluetoothSocket tmp = null;
            try {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
            }
            mmSocket = tmp;
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                }
            }
            connected(mmSocket);
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    final String incomingMessage = new String(buffer, 0, bytes);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view_data.setText(incomingMessage);
                        }
                    });

                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void SendMessage1(View v) {
        byte[] bytes = send_data.getText().toString().getBytes(Charset.defaultCharset());
        mConnectedThread.write(bytes);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        send_data = (EditText) findViewById(R.id.editText1);
        view_data = (TextView) findViewById(R.id.textView1);

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void Start_Server1(View view) {
        AcceptThread accept = new AcceptThread();
        accept.start();
    }

    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(send_data.toString(), MY_UUID_INSECURE);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }
        public void run() {
            BluetoothSocket socket = null;
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();

            } catch (IOException e) {
            }
            //talk about this is in the 3rd
            if (socket != null) {
                connected(socket);
            }
        }
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }
}*/

public class BluetoothActivity extends AppCompatActivity {
    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_BLUETOOTH = 2;

    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //private SingBroadcastReceiver mReceiver;

    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    private Handler handler;
    String TAG = "MainActivity";
    StringBuilder messages;
    int MESSAGE_READ = 1;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public EditText send_data;
    public TextView view_data;
    public Button button_Server, button_ConnectionREq, button_Button, button_podcl, button_changename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        send_data = findViewById(R.id.editText1);
        //button_Server = findViewById(R.id.button_Server);
        //button_ConnectionREq = findViewById(R.id.button_ConnectionREq);
        //button_Button = findViewById(R.id.button_Button);
        button_podcl = findViewById(R.id.button_podcl);
        button_changename = findViewById(R.id.button_changename);

        ArrayList<MyBluetoothDevices> devices = new ArrayList<MyBluetoothDevices>();
        ListView devicesList = (ListView) findViewById(R.id.available_devices1);
        NewBluetoothAdapter adapter = new NewBluetoothAdapter(this, R.layout.contact_item, devices);
        devicesList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        ArrayList<String> DEVICES = new ArrayList<>();

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        /*LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!LocationManagerCompat.isLocationEnabled(lm)) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }*/

        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

        String status;
        if (bluetoothAdapter.isEnabled()) {
            String mydeviceaddress = bluetoothAdapter.getAddress();
            String mydevicename = bluetoothAdapter.getName();
            int state = bluetoothAdapter.getState();
            status = mydevicename + ":" + mydeviceaddress + " : " + state;
        } else {
            status = "Bluetooth выключен";
        }
        Toast.makeText(this, status, Toast.LENGTH_LONG).show();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // Если список спаренных устройств не пуст
        if (pairedDevices.size() > 0) {
            // проходимся в цикле по этому списку
            for (BluetoothDevice device : pairedDevices) {
                // Добавляем имена и адреса в mArrayAdapter, чтобы показать
                // через ListView
                adapter.add(new MyBluetoothDevices(device.getName() + " (" + device.getAddress() + ")"));
                DEVICES.add(device.getName() + " (" + device.getAddress() + ")");
            }
        }

        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        /*mReceiver = new SingBroadcastReceiver();
        IntentFilter ifilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, ifilter);*/

        button_podcl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
                ActivityCompat.requestPermissions(BluetoothActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
                    switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        case PackageManager.PERMISSION_DENIED:
                            ((TextView) new AlertDialog.Builder(BluetoothActivity.this)
                                    .setTitle("Требуется Разрешение")
                                    .setMessage(Html.fromHtml("<p>Чтобы найти ближайшие устройства Bluetooth, нажмите \"Разрешить\" во всплывающем окне </p>" +
                                            "<p> Больше дополнительной информации <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">здесь</a>.</p>"))
                                    .setNeutralButton("Разрешить", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                ActivityCompat.requestPermissions(BluetoothActivity.this,
                                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                                            }
                                        }
                                    })
                                    .show()
                                    .findViewById(android.R.id.message))
                                    .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                            break;
                        case PackageManager.PERMISSION_GRANTED:
                            break;
                    }
                }

                if (ContextCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    if (ActivityCompat.shouldShowRequestPermissionRationale(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        // Not to annoy user.
                        Toast.makeText(BluetoothActivity.this, "Необходим доступ к местоположению для работы приложения", Toast.LENGTH_SHORT).show();
                    } else {
                        // Request permission.
                        ActivityCompat.requestPermissions(BluetoothActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_BLUETOOTH);
                    }
                }

                // Создаем BroadcastReceiver для ACTION_FOUND
                if (mBtAdapter.isDiscovering()) {
                    mBtAdapter.cancelDiscovery();
                    Log.wtf(TAG, "Устройство имеет соединение");
                }

                mBtAdapter.startDiscovery();
                final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        // Когда найдено новое устройство
                        Log.wtf(TAG, "startDiscovery");
                        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                            // Получаем объект BluetoothDevice из интента
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            //Добавляем имя и адрес в array adapter, чтобы показвать в ListView
                            if (!DEVICES.contains(device.getName() + " (" + device.getAddress() + ")")) {
                                adapter.add(new MyBluetoothDevices(device.getName() + " (" + device.getAddress() + ")"));
                                DEVICES.add(device.getName() + " (" + device.getAddress() + ")");
                                Log.wtf(TAG, "Устройство найдено");
                                adapter.notifyDataSetChanged();
                                mBtAdapter.cancelDiscovery();
                            }
                        }
                    }
                };
                // Регистрируем BroadcastReceiver
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Не забудьте снять регистрацию в onDestroy
            }
        });

    }

    /*private class SingBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Log.wtf(TAG, "Поиск начался");
            String action = intent.getAction(); //may need to chain this to a recognizing function
            if (BluetoothDevice.ACTION_FOUND.equals(action)){

                // Получаем объект BluetoothDevice из интента
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Добавляем имя и адрес в array adapter, чтобы показвать в ListView
                //adapter.add(new MyBluetoothDevices(device.getName() + " (" + device.getAddress() + ")"));
                Log.wtf(TAG, "Устройство найдено");
                //adapter.notifyDataSetChanged();
            }
        }
    }*/

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_BLUETOOTH: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();

                    // Permission granted.
                } else {
                    Toast.makeText(this, "Permission must be granted to use the application.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }*/

    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // используем вспомогательную переменную, которую в дальнейшем
            // свяжем с mmServerSocket,
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID это UUID нашего приложения, это же значение
                // используется в клиентском приложении
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(send_data.getText().toString(), MY_UUID);
            } catch (IOException e) {
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // ждем пока не произойдет ошибка или не
            // будет возвращен сокет
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // если соединение было подтверждено
                if (socket != null) {
                    // управлчем соединением (в отдельном потоке)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void manageConnectedSocket(BluetoothSocket mmSocket) {
            handler.obtainMessage(MESSAGE_READ, 3, 4).sendToTarget();
        }

        //отмена ожидания сокета
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // используем вспомогательную переменную, которую в дальнейшем
            // свяжем с mmSocket,
            BluetoothSocket tmp = null;
            mmDevice = device;

            // получаем BluetoothSocket чтобы соединиться с BluetoothDevice
            try {
                // MY_UUID это UUID, который используется и в сервере
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Отменяем сканирование, поскольку оно тормозит соединение
            bluetoothAdapter.cancelDiscovery();

            try {
                // Соединяемся с устройством через сокет.
                // Метод блокирует выполнение программы до
                // установки соединения или возникновения ошибки
                mmSocket.connect();
            } catch (IOException connectException) {
                // Невозможно соединиться. Закрываем сокет и выходим.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }
            // управлчем соединением (в отдельном потоке)
            manageConnectedSocket(mmSocket);
        }

        //отмена ожидания сокета
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

        private void manageConnectedSocket(BluetoothSocket mmSocket) {
            handler.obtainMessage(MESSAGE_READ, 3, 4).sendToTarget();
        }
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Получить входящий и исходящий потоки данных
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];// буферный массив
            int bytes;// bytes returned from read()

            // Прослушиваем InputStream пока не произойдет исключение
            while (true) {
                try {
                    // читаем из InputStream
                    bytes = mmInStream.read(buffer);
                    // посылаем прочитанные байты главной деятельности
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Вызываем этот метод из главной деятельности, чтобы отправить данные
    удаленному устройству */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Вызываем этот метод из главной деятельности,
    чтобы разорвать соединение */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void SendMessage1(View view) {
        bluetoothAdapter.setName(send_data.getText().toString());
        Log.wtf(TAG, "Name - " + send_data.getText().toString());
    }

    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /*private void manageConnectedSocket(BluetoothSocket socket) {
        //create thread responsible for sending messages.
        SendingThread w = new SendingThread(socket);
        MainActivity.addSendingThread(w);
        //Creates listener for messages to accept.
        MainActivity.addListener(socket);
    }*/
}