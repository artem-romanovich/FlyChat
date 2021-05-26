package com.example.fludrex.ui.bluetooth;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.fludrex.BluetoothActivity;
import com.example.fludrex.MyBluetoothDevices;
import com.example.fludrex.NewBluetoothAdapter;
import com.example.fludrex.R;
import java.util.ArrayList;
import java.util.Set;
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

    private BluetoothActivity.AcceptThread mAcceptThread;
    private BluetoothActivity.ConnectThread mConnectThread;
    private BluetoothActivity.ConnectedThread mConnectedThread;

    public EditText send_data;
    public TextView view_data;
    public Button button_Server, button_ConnectionREq, button_Button, button_podcl, button_changename;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        //send_data = root.findViewById(R.id.editText);
        //view_data = root.findViewById(R.id.textView);
        //button_Server = root.findViewById(R.id.button_Server);
        //button_ConnectionREq = root.findViewById(R.id.button_ConnectionREq);
        //button_Button = root.findViewById(R.id.button_Button);
        button_podcl = root.findViewById(R.id.button_podcl);
        button_changename = root.findViewById(R.id.button_changename);

        ArrayList<MyBluetoothDevices> devices = new ArrayList<MyBluetoothDevices>();
        ListView devicesList = (ListView) root.findViewById(R.id.available_devices1);
        NewBluetoothAdapter adapter = new NewBluetoothAdapter(getActivity(), R.layout.contact_item, devices);
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
        Toast.makeText(getActivity(), status, Toast.LENGTH_LONG).show();

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

        button_podcl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
                ActivityCompat.requestPermissions(BluetoothFragment.this.getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
                    switch (ContextCompat.checkSelfPermission(getActivity().getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        case PackageManager.PERMISSION_DENIED:
                            ((TextView) new AlertDialog.Builder(BluetoothFragment.this.getActivity())
                                    .setTitle("Требуется Разрешение")
                                    .setMessage(Html.fromHtml("<p>Чтобы найти ближайшие устройства Bluetooth, нажмите \"Разрешить\" во всплывающем окне </p>" +
                                            "<p> Больше дополнительной информации <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">здесь</a>.</p>"))
                                    .setNeutralButton("Разрешить", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (ContextCompat.checkSelfPermission(getActivity().getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                ActivityCompat.requestPermissions(BluetoothFragment.this.getActivity(),
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

                if (ContextCompat.checkSelfPermission(BluetoothFragment.this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    if (ActivityCompat.shouldShowRequestPermissionRationale(BluetoothFragment.this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        // Not to annoy user.
                        Toast.makeText(BluetoothFragment.this.getActivity(), "Необходим доступ к местоположению для работы приложения", Toast.LENGTH_SHORT).show();
                    } else {
                        // Request permission.
                        ActivityCompat.requestPermissions(BluetoothFragment.this.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_BLUETOOTH);
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
                getActivity().registerReceiver(mReceiver, filter); // Не забудьте снять регистрацию в onDestroy
            }
        });

        return root;
    }
}