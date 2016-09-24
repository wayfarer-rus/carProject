package wayfarer.carremotecontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.zerokol.views.JoystickView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CarRemoteControl extends AppCompatActivity  {
    private static final String LOG_TAG = "MainActivity";
    private static final String SERVER_MAC_ADDRESS = "00:02:72:C9:36:12";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 2;

    public static ConcurrentLinkedQueue videoNalQueue = new ConcurrentLinkedQueue();

    private List<ConnectedThread> clientThreadList = new ArrayList<>();
    private ConnectThread mConnectThread;
    private BluetoothDevice mBtDevice;
    private boolean btEnabled = false;
    private BluetoothAdapter mBluetoothAdapter = null;
    private JoystickView joystick;
    private TextView angleTextView;
    private TextView powerTextView;
    private TextView directionTextView;

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
                if (!mStarted) {
                    mSurface = videoView.getHolder().getSurface();
                    mMediaCodec.configure(mOutputFormat, mSurface, null, 0);
                    mOutputFormat = mMediaCodec.getOutputFormat();
                    mMediaCodec.start();
                    mStarted = true;
                }

                // print message from socket
                /*byte[] messData = (byte[]) inputMessage.obj;
                int size = inputMessage.arg1;

                bufferLock.lock();
                bufferStream = new ByteArrayOutputStream();
                bufferStream.write(messData, 0, size);

                if (size > 0 && !mStarted) {
                    mSurface = videoView.getHolder().getSurface();
                    mMediaCodec.configure(mOutputFormat, mSurface, null, 0);
                    mOutputFormat = mMediaCodec.getOutputFormat();
                    mMediaCodec.start();
                    mStarted = true;
                }

                bufferLock.unlock();*/
            } else if (inputMessage.what == ConnectThread.CONNECTED_CLIENT) {
                // remember socket client
                ConnectedThread clientThread = (ConnectedThread) inputMessage.obj;
                clientThreadList.add(clientThread);
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG,"+ enter onReceive()");
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(LOG_TAG,"BT device found:");
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                String txt = device.getName() + "\n" + device.getAddress();
                Log.d(LOG_TAG,txt);

                if (SERVER_MAC_ADDRESS.equals(device.getAddress())) {
                    Log.d(LOG_TAG,"Found server!");
                    mBtDevice = device;
                }
            }
            Log.d(LOG_TAG,"- leave onReceive");
        }
    };

    private boolean mStarted = false;
    private ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
    private VideoView videoView;
    private final ReentrantLock bufferLock = new ReentrantLock();
    private MediaCodec mMediaCodec;
    private MediaFormat mOutputFormat;
    private Surface mSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_remote_control);

        // setup right slider
        final TextView rightSliderText = (TextView)findViewById(R.id.verticalSeekbarTextRight);
        VerticalSeekBar verticalSeebarRight = (VerticalSeekBar)findViewById(R.id.verticalSeekbarRight);
        setupVerticalSpeedSlider("rightWheels", rightSliderText,verticalSeebarRight);

        // setup left slider
        final TextView leftSliderText = (TextView)findViewById(R.id.verticalSeekbarTextLeft);
        VerticalSeekBar verticalSeebarLeft = (VerticalSeekBar)findViewById(R.id.verticalSeekbarLeft);
        setupVerticalSpeedSlider("leftWheels", leftSliderText,verticalSeebarLeft);

        // setup joystick
        angleTextView = (TextView) findViewById(R.id.angleTextView);
        powerTextView = (TextView) findViewById(R.id.powerTextView);
        directionTextView = (TextView) findViewById(R.id.directionTextView);
        joystick = (JoystickView) findViewById(R.id.joystick);
        //Event listener that always returns the variation of the angle in degrees, motion power in percentage and direction of movement
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                angleTextView.setText("Angle: " + String.valueOf(angle) + "°");
                powerTextView.setText("Power: " + String.valueOf(power) + "%");
                sendFromJoystick("joystick",angle, power);
                String directionTxt;

                switch (direction) {
                    case JoystickView.FRONT:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.front_lab);
                        directionTextView.setText(directionTxt);
                        break;
                    case JoystickView.FRONT_RIGHT:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.front_right_lab);
                        directionTextView.setText(directionTxt);
                        break;
                    case JoystickView.RIGHT:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.right_lab);
                        directionTextView.setText(directionTxt);
                        break;
                    case JoystickView.RIGHT_BOTTOM:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.right_bottom_lab);
                        directionTextView.setText(directionTxt);
                        break;
                    case JoystickView.BOTTOM:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.bottom_lab);
                        directionTextView.setText(directionTxt);
                        break;
                    case JoystickView.BOTTOM_LEFT:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.bottom_left_lab);
                        directionTextView.setText(directionTxt);
                        break;
                    case JoystickView.LEFT:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.left_lab);
                        directionTextView.setText(directionTxt);
                        break;
                    case JoystickView.LEFT_FRONT:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.left_front_lab);
                        directionTextView.setText(directionTxt);
                        break;
                    default:
                        directionTxt = "Direct: " + CarRemoteControl.this.getString(R.string.center_lab);
                        directionTextView.setText(directionTxt);
                }
            }
        }, (long)(100.0/6.0)); // iterate 60 times in second

        // init bluetooth device
        initBtDevice();
        // init video view
        videoView = (VideoView) findViewById(R.id.videoView);

        try {
            mOutputFormat = MediaFormat.createVideoFormat("video/avc", 320,240);
            mOutputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
//            mMediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaCodecList mcl = new MediaCodecList(MediaCodecList.ALL_CODECS);
            mMediaCodec = MediaCodec.createByCodecName(mcl.findDecoderForFormat(mOutputFormat));
            mMediaCodec.setCallback(new MediaCodec.Callback() {

                @Override
                public void onInputBufferAvailable(MediaCodec mc, int i) {
                    ByteBuffer inputBuffer = mc.getInputBuffer(i);

                    if (!videoNalQueue.isEmpty()) {
                        byte[] data = (byte[]) videoNalQueue.poll();
                        int size = data.length;
                        inputBuffer.put(data);
                        Log.d("MediaCodec", "tic" + i);
                        mc.queueInputBuffer(i,0, size, System.currentTimeMillis(), 0);
                    } else {
                        mc.queueInputBuffer(i,0, 0, System.currentTimeMillis(), 0);
                    }
                }

                @Override
                public void onOutputBufferAvailable(MediaCodec mc, int i, MediaCodec.BufferInfo bufferInfo) {
                    Log.d("MediaCodec", "tak" + i);
                    mc.releaseOutputBuffer(i, true);
                }

                @Override
                public void onError(MediaCodec mc, MediaCodec.CodecException e) {
                    Log.e("MediaCodec", e.getMessage(), e);
                    mc.stop();
                    mc.release();
                }

                @Override
                public void onOutputFormatChanged(MediaCodec mc, MediaFormat mediaFormat) {
                    // Subsequent data will conform to new format.
                    // Can ignore if using getOutputFormat(outputBufferId)
                    mOutputFormat = mediaFormat; // option B
                }
            });


        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

    }

    @Override
    protected  void onDestroy() {
        Log.d(LOG_TAG,"+ enter onDestroy");

        try {
            unregisterReceiver(mReceiver);
        } catch (java.lang.IllegalArgumentException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        mMediaCodec.stop();
        mMediaCodec.release();

        if (btEnabled) {
            mBluetoothAdapter.cancelDiscovery();

            if (mConnectThread != null && mConnectThread.isAlive()) {
                mConnectThread.cancel();
            }
        }

        super.onDestroy();
        Log.d(LOG_TAG,"- leave onDestroy");
    }

    private void initBtDevice() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.d(LOG_TAG,"Bluetooth Adapter is not available");
            // Device does not support Bluetooth
            showError("No bluetooth support for you!");
        } else {
            Log.d(LOG_TAG,"Bluetooth Adapter is available");

            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(LOG_TAG,"Bluetooth Adapter is disabled. Request enabling");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Log.d(LOG_TAG,"Bluetooth Adapter is enabled. Start discovery");
                btEnabled = true;
                findBtServerDevice();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {
        Log.d(LOG_TAG,"+ enter onActivityResult");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                btEnabled = true;
                findBtServerDevice();
            } else if (resultCode == RESULT_CANCELED) {
                showError("Program will do nothing without BT.");
            }
        }
        Log.d(LOG_TAG,"- leave onActivityResult");
    }

    private void findBtServerDevice() {
        Log.d(LOG_TAG, "+ enter findBtServerDevice()");
        Log.d(LOG_TAG,"find already paired devices");
        // query paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                String txt = device.getName() + "\n" + device.getAddress();
                Log.d(LOG_TAG, txt);

                if (SERVER_MAC_ADDRESS.equals(device.getAddress())) {
                    Log.d(LOG_TAG,"Found server!");
                    mBtDevice = device;
                    return;
                }
            }
        }
        // device not found in paired
        // check permissions
        int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            continueDoDiscovery();
            Log.d(LOG_TAG,"- leave findBtServerDevice. Lookup process initiated.");
            return;
        }

        // permissions not granted. request from user
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_COARSE_LOCATION_PERMISSIONS);

        Log.d(LOG_TAG,"- leave findBtServerDevice()");
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
        Log.d(LOG_TAG,"Discover surroundings");
        // discover devices
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        mBluetoothAdapter.startDiscovery();
    }

    private void connectToBtServer(BluetoothDevice device) {
        Log.d(LOG_TAG,"+ enter connectToBtServer");
        mBluetoothAdapter.cancelDiscovery();
        mConnectThread = new ConnectThread(mHandler, mBluetoothAdapter, device);
        mConnectThread.start();
        Log.d(LOG_TAG,"- leave connectToBtServer");
    }

    private void setupVerticalSpeedSlider(final String id, final TextView textView, VerticalSeekBar seekBar) {
        textView.setTextSize(48);
        textView.setText("0");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(100);
                textView.setText("0");
                sendFromScrollBar(id, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                int value = progress-100;
                textView.setText(""+value);
                sendFromScrollBar(id, value);
            }
        });
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

    private void sendFromScrollBar(String id, int value) {
        if (btEnabled && mBtDevice != null) {
            for (ConnectedThread t : clientThreadList) {
                if (t.isAlive()) {
                    String message = "{\""+id +"\":"+value+"}#";
                    t.write(message.getBytes());
                }
            }
        }
    }

    private void sendFromJoystick(String id, int angle, int power) {
        if (btEnabled && mBtDevice != null) {
            for (ConnectedThread t : clientThreadList) {
                if (t.isAlive()) {
                    String message = "{\""+id +"\":{\"angle\":"+angle+",\"power\":" + power + "}}#";
                    t.write(message.getBytes());
                }
            }
        }
    }

    // Button handlers
    public void onConnect(View view) {
        if (btEnabled && mBtDevice != null && (mConnectThread == null || !mConnectThread.isAlive()))
            connectToBtServer(mBtDevice);
    }

    public void onDisconnect(View view) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
            if (mStarted) {
                mMediaCodec.stop();
                mMediaCodec.release();
                mStarted = false;
            }
        }
    }

    public void onStopServer(View view) {
        if (mConnectThread != null) {
            sendFromScrollBar("terminate", 1);
            if (mStarted) {
                mMediaCodec.stop();
                mMediaCodec.release();
                mStarted = false;
            }
        }
    }
}
