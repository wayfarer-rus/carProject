package wayfarer.carremotecontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Project BluetoothClientTest
 * Created by wayfarer on 8/21/16.
 */
public class ConnectThread extends Thread {
    private static final UUID MY_UUID = UUID.fromString("00001105-0000-1000-8000-00805f9b34fb");
    public static final int CONNECTED_CLIENT = 3;
    private final Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private List<ConnectedThread> connectedThreadList = new ArrayList<>();
    private BluetoothSocket mSocket;

    public ConnectThread(Handler handler, BluetoothAdapter adapter, BluetoothDevice device) {
        Log.d("ConectThread", "+ enter ConnectThread");
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mBluetoothAdapter = adapter;
        mHandler = handler;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            Log.d("ConectThread", "create socket");
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e("ConnectThread", e.getMessage(), e);
        }
        mSocket = tmp;
        Log.d("ConectThread", "- leave ConectThread");
    }

    public void run() {
        Log.d("ConectThread", "+ enter run()");
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            Log.d("ConectThread", "connecting to socket");
            mSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            Log.e("ConectThread", "Unable to connect", connectException);
            Log.d("ConnectThread", "Trying fallback connect");
            // fallback connect
            try {
                Class<?> clazz = mSocket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};

                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};

                mSocket = (BluetoothSocket) m.invoke(mSocket.getRemoteDevice(), params);
                mSocket.connect();
            } catch (Exception e) {
                Log.e("ConnectThread", e.getMessage(), e);
                try {
                    mSocket.close();
                } catch (IOException closeException) { }
                return;
            }
        }

        Log.d("ConectThread", "connected");
        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mSocket);
        Log.d("ConectThread", "- leave run()");
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        ConnectedThread t = new ConnectedThread(mHandler, socket);
        connectedThreadList.add(t);
        t.start();
        mHandler.obtainMessage(CONNECTED_CLIENT, t).sendToTarget();
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            for (ConnectedThread t : connectedThreadList) {
                t.cancel();
            }
            mSocket.close();
        } catch (IOException e) { }
    }
}