package com.kokme.cheapventilator;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.core.content.ContextCompat;

import androidx.core.app.ActivityCompat;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int CONNECTED = 1;
    private static final int DISCONNECTED = 2;
    private static final int MESSAGE_READ = 3;
    private static final int MESSAGE_WRITE = 4;

    private static final int REQUEST_CONNECT_DEVICE = 1;

    private ArrayAdapter<String> messageAdapter;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothChat chat;
    private Button buttonConnect;

    private Button buttonInPlus;
    private Button buttonInMinus;
    private Button buttonAVPlus;
    private Button buttonAVMinus;
    private Button buttonExPlus;
    private Button buttonExMinus;
    private Button buttonOxyInc;
    private Button buttonOxyDec;

    private TextView inCurrentTextView;
    private TextView exCurrentTextView;
    private TextView ibyeTextView;
    private TextView airvolumeTextView;
    private TextView breathpmTextView;
    private TextView airvpmTextView;
    private TextView patwetTextView;
    private TextView oxyConTextView;
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView oxyVolTextView;


    private int inPlus = 1;
    private int inMinus = 2;

    private int exPlus = 3;
    private int exMinus = 4;
    private int avPlus = 5;
    private int avMinus = 6;
    private int oxyUp = 7;
    private int oxyDown = 8;

    private int currentIn =1000;
    private int currentEx =1000;
    private int currentAirv =232;
    private static DecimalFormat df = new DecimalFormat("0.00");

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Toast.makeText(MainActivity.this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
        }
        // ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission is required for Bluetooth from Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isEnabled() == false) {
                    // Request to enable bluetooth
                    startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                    return;
                }
                if (chat == null) {
                    // Launch DeviceListActivity to search bluetooth device
                    startActivityForResult(new Intent(MainActivity.this, DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
                } else {
                    chat.close();
                }
            }
        });


        inCurrentTextView = (TextView) findViewById(R.id.in_current);
        exCurrentTextView = (TextView) findViewById(R.id.ex_current);

        ibyeTextView = (TextView) findViewById(R.id.ibye);
        breathpmTextView = (TextView) findViewById(R.id.breathpm);
        airvolumeTextView = (TextView) findViewById(R.id.airvolume);
        airvpmTextView = (TextView) findViewById(R.id.airvpm);

        patwetTextView = (TextView) findViewById(R.id.patwet);

        oxyConTextView = (TextView) findViewById(R.id.oxyCon);
        oxyVolTextView = (TextView) findViewById(R.id.oxyVol);

        humidityTextView = (TextView) findViewById(R.id.humi);
        temperatureTextView = (TextView) findViewById(R.id.temp);

        buttonInPlus = (Button) findViewById(R.id.button_in_plus);
        buttonInMinus = (Button) findViewById(R.id.button_in_minus);
        buttonAVPlus = (Button) findViewById(R.id.button_av_plus);
        buttonAVMinus = (Button) findViewById(R.id.button_av_minus);
        buttonExPlus = (Button) findViewById(R.id.button_ex_plus);
        buttonExMinus = (Button) findViewById(R.id.button_ex_minus);
        buttonOxyInc = (Button) findViewById(R.id.oxyInc);
        buttonOxyDec = (Button) findViewById(R.id.oxyDec);



        airvolumeTextView.setText(currentAirv+"");
        airvpmTextView.setText(Math.round(currentAirv*Math.round((60*1000)/(currentIn + currentEx)))+"");
        setInfoBox();

        buttonInPlus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chat.send(String.valueOf(inPlus).getBytes());
            }
        });

        buttonInMinus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chat.send(String.valueOf(inMinus).getBytes());
            }
        });
        buttonAVPlus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chat.send(String.valueOf(avPlus).getBytes());
            }
        });

        buttonAVMinus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chat.send(String.valueOf(avMinus).getBytes());
            }
        });
        buttonExPlus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                chat.send(String.valueOf(exPlus).getBytes());
            }
        });

        buttonExMinus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                chat.send(String.valueOf(exMinus).getBytes());
            }
        });
        buttonOxyInc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                chat.send(String.valueOf(oxyUp).getBytes());
            }
        });
        buttonOxyDec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                chat.send(String.valueOf(oxyDown).getBytes());
            }
        });
        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        ListView messageView = (ListView) findViewById(R.id.message_view);
        messageView.setAdapter(messageAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (chat != null) {
            chat.close();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // When DeviceListActivity returns with a device to connect
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    BluetoothSocket socket;

                    try {
                        socket = device.createRfcommSocketToServiceRecord(uuid);
                    } catch (IOException e) {
                        break;
                    }
                    chat = new BluetoothChat(socket, handler);
                    chat.start();
                }
                break;
        }

    }

    // The Handler that gets information back from the BluetoothChat
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECTED:
                    buttonConnect.setText("Disconnect");
                    buttonInPlus.setEnabled(true);
                    buttonInMinus.setEnabled(true);
                    buttonAVPlus.setEnabled(true);
                    buttonAVMinus.setEnabled(true);
                    buttonExPlus.setEnabled(true);
                    buttonExMinus.setEnabled(true);
                    buttonOxyInc.setEnabled(true);
                    buttonOxyDec.setEnabled(true);
                    break;
                case DISCONNECTED:
                    buttonConnect.setText("Connect to bluetooth device");
                    buttonInPlus.setEnabled(false);
                    buttonInMinus.setEnabled(false);
                    buttonAVPlus.setEnabled(false);
                    buttonAVMinus.setEnabled(false);
                    buttonExPlus.setEnabled(false);
                    buttonExMinus.setEnabled(false);
                    buttonOxyInc.setEnabled(false);
                    buttonOxyDec.setEnabled(false);
                    chat = null;
                    break;
                case MESSAGE_READ:
                    try {
                        //inCurrentTextView.setText("IN: " + new String((byte[]) msg.obj, 0, msg.arg1, "UTF-8"));
                        // Encoding with "EUC-KR" to read 한글inCurrentTextView.setText("IN: " + new String((byte[]) msg.obj, 0, msg.arg1, "UTF-8"));
                        //


                       final String receivedtextRaw = new String((byte[]) msg.obj, msg.arg1, msg.arg2, "UTF-8");
                       final String[] receivedtext= receivedtextRaw.split("-");


                        String mssg="";
                        for(int i = 0; i<receivedtext.length;i++){
                            switch (receivedtext[i]) {
                                case "F":
                                    airvolumeTextView.setText(receivedtext[i+1]+"");
                                    try{
                                        currentAirv =Integer.valueOf( receivedtext[i+1]);
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                    setInfoBox();
                                    mssg = receivedtext.length == 10?"CONNECTED Air Volume: ":"Current Air Volume: ";
                                    messageAdapter.add(mssg + receivedtext[i+1]+" ml/breath");
                                    break;
                                case "I":
                                    inCurrentTextView.setText("IN: " + receivedtext[i+1]);
                                    currentIn = Integer.valueOf(receivedtext[i+1]);
                                    setInfoBox();
                                    mssg = receivedtext.length == 10?"CONNECTED Current Inhale Time: ":"Current Inhale Time: ";
                                    messageAdapter.add(mssg + receivedtext[i+1]);
                                    break;
                                case "E":
                                    exCurrentTextView.setText("EX: "+ receivedtext[i+1]);
                                    currentEx = Integer.valueOf(receivedtext[i+1]);
                                    setInfoBox();
                                    mssg = receivedtext.length == 10?"CONNECTED Current Exhale Time: ":"Current Exhale Time: ";
                                    messageAdapter.add(mssg + receivedtext[i+1]);
                                    break;
                                case "O":
                                    oxyConTextView.setText(receivedtext[i+1]+"");
                                    setInfoBox();
                                    mssg = receivedtext.length == 10?"CONNECTED Current Oxygen Concentration: ":"Current Oxygen Concentration: ";
                                    messageAdapter.add(mssg + receivedtext[i+1] +"%");
                                    break;
                                case "V":
                                    oxyVolTextView.setText(receivedtext[i+1].replace("#","")+"");
                                    setInfoBox();
                                    mssg = receivedtext.length == 10?"CONNECTED Current Oxygen Volume ml/min: ":"Current Oxygen Volume ml/min: ";
                                    messageAdapter.add(mssg + receivedtext[i+1] +"%");
                                    break;
                                case "H":
                                    humidityTextView.setText(receivedtext[i+1]+"");
                                    break;
                                case "T":
                                    temperatureTextView.setText(receivedtext[i+1]+"");
                                    break;
                            }
                            i++;
                        }

                        buttonInPlus.setEnabled(true);
                        buttonInMinus.setEnabled(true);
                        buttonAVPlus.setEnabled(true);
                        buttonAVMinus.setEnabled(true);
                        buttonExPlus.setEnabled(true);
                        buttonExMinus.setEnabled(true);
                        buttonOxyInc.setEnabled(true);
                        buttonOxyDec.setEnabled(true);
                    } catch (UnsupportedEncodingException e) {
                    }
                    break;
                case MESSAGE_WRITE:
                    buttonInPlus.setEnabled(false);
                    buttonInMinus.setEnabled(false);
                    buttonAVPlus.setEnabled(false);
                    buttonAVMinus.setEnabled(false);
                    buttonExPlus.setEnabled(false);
                    buttonExMinus.setEnabled(false);
                    buttonOxyInc.setEnabled(false);
                    buttonOxyDec.setEnabled(false);
                    try {

                    switch (new String((byte[]) msg.obj, 0, msg.arg1, "UTF-8")) {
                        case "1":
                            messageAdapter.add("Send: Increase Inhale Time");

                            break;

                        case "2":
                            messageAdapter.add("Send: Decrease Inhale Time");
                            break;
                        case "3":
                            messageAdapter.add("Send: Increase Exhale Time");
                            break;
                        case "4":
                            messageAdapter.add("Send: Decrease Exhale Time");
                            break;
                        case "5":
                            messageAdapter.add("Send: Increase Air Volume");
                            break;
                        case "6":
                            messageAdapter.add("Send: Decrease Air Volume");
                            break;
                        case "7":
                            messageAdapter.add("Send: Increase Oxygen Volume");
                            break;
                        case "8":
                            messageAdapter.add("Send: Decrease Oxygen Volume");
                            break;
                    }
                    } catch (UnsupportedEncodingException e) {
                    }
                    break;
            }
        }
    };
    private int gcd(int a, int b){
        if(b == 0)return a;
        else return gcd(b, a%b);
    }
    private String ratio(int a, int b){
        final int gcd=( gcd(a,b));
        return a/gcd +":"+b/gcd;
    }
private void setInfoBox(){
        patwetTextView.setText(currentAirv/7+"");
    airvpmTextView.setText(Math.round(currentAirv*Math.round((60*1000)/(currentIn + currentEx)))+"");
    breathpmTextView.setText(Math.round((60*1000)/(currentIn + currentEx))+"" );
    ibyeTextView.setText(ratio(currentIn,currentEx)+"" );
}
    // This class connect with a bluetooth device and perform data transmissions when connected.
    private class BluetoothChat extends Thread {
        private BluetoothSocket socket;
        private Handler handler;
        private InputStream inputStream;
        private OutputStream outputStream;

        public BluetoothChat(BluetoothSocket socket, Handler handler) {
            this.socket = socket;
            this.handler = handler;
        }

        public void run() {
            try {
                socket.connect();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (Exception e) {
                close();
                return;
            }
            handler.obtainMessage(CONNECTED, -1, -1).sendToTarget();
            int bytes = 0;
            int begin = 0;
            byte buffer[] = new byte[1024];
            while (true) {
                try {
                    bytes += inputStream.read(buffer, bytes, buffer.length - bytes);
                    for(int i = begin; i < bytes; i++) {
                        if(buffer[i] == "#".getBytes()[0]) {
                            handler.obtainMessage(MESSAGE_READ, begin, i, buffer).sendToTarget();

                            begin = i + 1;
                            if(i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    close();
                    break;
                }
            }

           /* while (true) {
                try {
                    byte buffer[] = new byte[1024];
                    int bytes = inputStream.read(buffer);

                    // Read single byte until '\0' is found
                    //for (; (buffer[bytes] = (byte) inputStream.read()) != '\0'; bytes++) ;
                    //inCurrentTextView.setText("HHH");
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    close();
                    break;
                }
            }*/
        }

        public void close() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                handler.obtainMessage(DISCONNECTED, -1, -1).sendToTarget();
            }
        }

        public void send(byte[] buffer) {
            try {
                outputStream.write(buffer);
                outputStream.write('\n');
                handler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
                close();
            }
        }
    }
}