package wayfarer.bluetoothclienttest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 22;
    private static final String SERVER_MAC_ADDRESS = "00:02:72:C9:36:12";

    boolean btEnabled = false;
    BluetoothAdapter mBluetoothAdapter = null;
    List<String> mArrayAdapter = new ArrayList<>();
    private List<ConnectedThread> clientThreadList = new ArrayList<>();

    // create main UI handler
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        /*
         * handleMessage() defines the operations to perform when
         * the Handler receives a new Message to process.
         */
        @Override
        public void handleMessage(Message inputMessage) {
            // Gets the image task from the incoming Message object.
            if (inputMessage.what == ConnectedThread.MESSAGE_READ) {
                byte[] messData = (byte[]) inputMessage.obj;
                int size = inputMessage.arg1;
                Log.d("MainActivity", new String(messData,0,size));
            } else if (inputMessage.what == ConnectThread.CONNECTED_CLIENT) {
                ConnectedThread clientThread = (ConnectedThread) inputMessage.obj;
                clientThreadList.add(clientThread);
            }
        }
    };

    private ConnectThread mConnectThread;
    private BluetoothDevice mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity","+ enter onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.d("MainActivity","Bluetooth Adapter is not available");
            // Device does not support Bluetooth
            showError("No bluetooth support for you!");
        } else {
            Log.d("MainActivity","Bluetooth Adapter is available");
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d("MainActivity","Bluetooth Adapter is disabled. Request enabling");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Log.d("MainActivity","Bluetooth Adapter is enabled. Start discovery");
                btEnabled = true;
                findBtServerDevice();
            }
        }
        Log.d("MainActivity","- leave onCreate");
    }

    private void findBtServerDevice() {
        Log.d("MainActivity", "+ enter findBtServerDevice()");
        Log.d("MainActivity","find already paired devices");
        // query paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                String txt = device.getName() + "\n" + device.getAddress();
                Log.d("MainActivity",txt);
                mArrayAdapter.add(txt);

                if (SERVER_MAC_ADDRESS.equals(device.getAddress())) {
                    Log.d("MainActivity","Found server!");
                    connectToBtServer(device);
                    return;
                }
            }
        }

        Log.d("MainActivity","Discover surroundings");
        int hasPermission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            continueDoDiscovery();
            Log.d("MainActivity","- leave findBtServerDevice()");
            return;
        }

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_COARSE_LOCATION_PERMISSIONS);

        Log.d("MainActivity","- leave findBtServerDevice()");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_PERMISSIONS: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    continueDoDiscovery();
                } else {
                    Toast.makeText(this,
                            getResources().getString(R.string.permission_failure),
                            Toast.LENGTH_LONG).show();
                    //cancelOperation();
                }
                return;
            }
        }
    }

    private void continueDoDiscovery() {
        // discover devices
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        mBluetoothAdapter.startDiscovery();
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity.mReceiver","+ enter onReceive()");
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("MainActivity.mReceiver","BT device found:");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                String txt = device.getName() + "\n" + device.getAddress();
                Log.d("MainActivity",txt);
                mArrayAdapter.add(txt);

                if (SERVER_MAC_ADDRESS.equals(device.getAddress())) {
                    Log.d("MainActivity","Found server!");
                    connectToBtServer(device);
                }
            }
            Log.d("MainActivity.mReceiver","- leave onReceive");
        }
    };

    private void connectToBtServer(BluetoothDevice device) {
        mDevice = device;
        mBluetoothAdapter.cancelDiscovery();
        mConnectThread = new ConnectThread(mHandler, mBluetoothAdapter, device);
        mConnectThread.start();
    }

    public void onReconnect(View view) {
        Log.d("MainActivity","+ enter onReconnect");

        if (btEnabled && mDevice != null) {
            mConnectThread = new ConnectThread(mHandler, mBluetoothAdapter, mDevice);
            mConnectThread.start();
        }

        Log.d("MainActivity","- leave onReconnect");
    }

    @Override
    protected  void onDestroy() {
        Log.d("MainActivity","+ enter onDestroy");
        unregisterReceiver(mReceiver);

        if (btEnabled) {
            mBluetoothAdapter.cancelDiscovery();

            if (mConnectThread != null && mConnectThread.isAlive()) {
                mConnectThread.cancel();
            }
        }

        super.onDestroy();
        Log.d("MainActivity","- leave onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {
        Log.d("MainActivity","+ enter onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                btEnabled = true;
                findBtServerDevice();
            } else if (resultCode == RESULT_CANCELED) {
                showError("Program will do nothing without BT.");
            }
        }
        Log.d("MainActivity","- leave onActivityResult");
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void onSend(View view) {
        Log.d("MainActivity","+ enter onSend");
        // TODO: get text-message from view
        String message = "Hello world!";

        for (ConnectedThread t : clientThreadList) {
            if (t.isAlive()) {
                t.write(message.getBytes());
            }
        }

        Log.d("MainActivity","- leave onSend");
    }

    public void onStopDiscovering(View view) {
        Log.d("MainActivity","+ enter onStopDiscovering()");
        if (btEnabled)
            mBluetoothAdapter.cancelDiscovery();
        Log.d("MainActivity","- leave onStopDiscovering()");
    }
}
