package com.example.fludrex.ui.bluetooth;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.fludrex.BluetoothActivity;
import com.example.fludrex.CurrentTime;
import com.example.fludrex.ImageActivity;
import com.example.fludrex.MyBluetoothDevices;
import com.example.fludrex.MyMessage;
import com.example.fludrex.NewBluetoothAdapter;
import com.example.fludrex.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Permission;
import java.security.Permissions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothFragment extends Fragment {

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

    //private BluetoothActivity.AcceptThread mAcceptThread;
    //private BluetoothActivity.ConnectThread mConnectThread;
    //private BluetoothActivity.ConnectedThread mConnectedThread;

    public EditText send_data;
    public TextView view_data;
    ProgressBar progress_horizontal;
    public Button button_Server, button_ConnectionREq, button_Button,
            button_podcl, button_changename;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        button_podcl = root.findViewById(R.id.button_podcl);
        button_changename = root.findViewById(R.id.button_changename);
        progress_horizontal = root.findViewById(R.id.progress_horizontal);
        progress_horizontal.setVisibility(View.GONE);

        //int REQUEST_CODE_BACKGROUND = 1;
        //String[] list_background = new String[0];
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //    list_background = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION};
        //}
        //ActivityCompat.requestPermissions(requireActivity(), list_background, REQUEST_CODE_BACKGROUND);

        //boolean hasBackgroundLocationPermission = false;
        //    hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(requireActivity(),
        //            Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        //if (!hasBackgroundLocationPermission) {
        //    Toast.makeText(getActivity(),
        //            "Необходим доступ к местоположению в любом режиме. Разрешите доступ вручную в настройках", Toast.LENGTH_LONG).show();
        //    requireActivity().finish();
        //} else {

        ArrayList<MyBluetoothDevices> devices = new ArrayList<MyBluetoothDevices>();
        ListView devicesList = (ListView) root.findViewById(R.id.available_devices1);
        NewBluetoothAdapter adapter = new NewBluetoothAdapter(getActivity(), R.layout.blt_device_item, devices);
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

        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }


        int MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
            switch (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    if (ContextCompat.checkSelfPermission(requireActivity().getBaseContext(),
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(requireActivity(),
                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
                                    }
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }

            boolean hasBackgroundLocationPermission = false;
            hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!hasBackgroundLocationPermission) {
                Toast.makeText(requireActivity(),
                        "Необходим доступ к местоположению в любом режиме. Разрешите доступ вручную в настройках", Toast.LENGTH_LONG).show();
                requireActivity().finish();
            }

        }


        String status;
        if (bluetoothAdapter.isEnabled()) {
            @SuppressLint("HardwareIds")
            String mydeviceaddress = bluetoothAdapter.getAddress();
            String mydevicename = bluetoothAdapter.getName();
            int state = bluetoothAdapter.getState();
            status = mydevicename + ":" + mydeviceaddress + " : " + state;
        } else {
            status = "Bluetooth выключен";
        }
        Log.wtf("my_name", status);

        //Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // Если список спаренных устройств не пуст
        /*if (pairedDevices.size() > 0) {
            // проходимся в цикле по этому списку
            for (BluetoothDevice device : pairedDevices) {
                // Добавляем имена и адреса в mArrayAdapter, чтобы показать
                // через ListView
                adapter.add(new MyBluetoothDevices(device.getName() + " (" + device.getAddress() + ")"));
                DEVICES.add(device.getName() + " (" + device.getAddress() + ")");
            }
        }*/

        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        button_podcl.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                progress_horizontal.setVisibility(View.VISIBLE);
                button_podcl.setClickable(false);

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
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        progress_horizontal.setVisibility(View.GONE);
                                        button_podcl.setClickable(true);
                                    }
                                }, 200);
                            }
                        }
                    };
                    // Регистрируем BroadcastReceiver
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    requireActivity().registerReceiver(mReceiver, filter); // Не забудьте снять регистрацию в onDestroy
            }
        });
        return root;
    }



}