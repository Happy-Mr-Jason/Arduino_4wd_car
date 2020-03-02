package com.example.arduino_4wd_car;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static com.example.arduino_4wd_car.R.drawable.shape_mode_button;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class MainActivity extends AppCompatActivity implements ContextListernerInterface {

    public static final int REQUEST_ENABLE_BT = 10;
    public static final int CAR_MODE_JOYSTICK = 1;
    public static final int CAR_MODE_BUTTON = 2;
    public static final int CAR_MODE_SENSOR = 3;
    public static final int TILTING_MODE_BUTTON = 1;
    public static final int TILTING_MODE_TRACKING = 2;
    private BluetoothAdapter bluetoothAdapter;
    private int mPairedDeviceCount = 0;
    private Set<BluetoothDevice> mDevices;
    private BluetoothDevice mRemoteDevice;
    private BluetoothSocket mSocket = null;
    private OutputStream mOutputStream = null;
    private InputStream mInputStream = null;
    private Thread mWorkerThread = null;
    private char sendHeader = (char) 0x05;
    private char mEndCharDelimiter1 = (char) 0x0D;
    private char mEndCharDelimiter2 = (char) 0x0A;
    private String mStrDelimiter = "\r\n";
    private char mCharDelimiter = '\n';
    private byte[] readBuffer;
    private int readBufferPosition;
    private MenuItem bluetooth_menuitem;
    private RelativeLayout layoutJoystickZone;
    private LinearLayout layoutButtonsCar;
    private LinearLayout layoutSensorButtonsCar;
    private ImageView ivJoystick;
    private TextView tvPosition;
    private TextView tvSpeedCmd;
    private Button[] mCarModeButtons = new Button[3];
    private Button[] mCarButtons = new Button[7];
    private Button[] mTiltingModeButtons = new Button[2];
    private Button[] mTiltingButtons = new Button[5];
    private int[] mCarButtonIDs = {R.id.btnCarTurnLeft, R.id.btnCarTurnRight, R.id.btnCarForward, R.id.btnCarBackward, R.id.btnCarStop, R.id.btnSensorCarStart, R.id.btnSensorCarStop};
    private int[] mCarModeButtonIDs = {R.id.btnCarJoystickMode, R.id.btnCarButtonMode, R.id.btnCarSensorMode};
    private int[] mTiltingButtonIDs = {R.id.btnTiltingNeutral, R.id.btnTiltingUp, R.id.btnTiltingDown, R.id.btnTiltingLeft, R.id.btnTiltingRight};
    private int[] mTiltingModeButtonIDs = {R.id.btnTiltingTrackingMode, R.id.btnTiltingButtonMode};
    private LinearLayout layoutButtonsTilting;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.icon_24);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(" ARDUINO 4WD CAR");

        layoutJoystickZone = findViewById(R.id.layoutLeft);
        layoutButtonsCar = findViewById(R.id.layoutButtonsCar);
        layoutButtonsTilting = findViewById(R.id.layoutButtonsTilting);
        layoutSensorButtonsCar = findViewById(R.id.layoutSensorButtonsCar);
        tvPosition = findViewById(R.id.tvPosition);
        tvSpeedCmd = findViewById(R.id.tvSpeedCmd);

        MyTouchListener myTouchListener = new MyTouchListener(this);
        ivJoystick = findViewById(R.id.ivJoystick);
        ivJoystick.setOnTouchListener(myTouchListener);

        MyButtonClickListener myButtonClickListener = new MyButtonClickListener(this);
        for (int i = 0; i < mCarButtons.length; i++) {
            mCarButtons[i] = findViewById(mCarButtonIDs[i]);
            mCarButtons[i].setOnClickListener(myButtonClickListener);
        }
        for (int i = 0; i < mCarModeButtons.length; i++) {
            mCarModeButtons[i] = findViewById(mCarModeButtonIDs[i]);
            mCarModeButtons[i].setBackground(getResources().getDrawable(shape_mode_button));
            mCarModeButtons[i].setOnClickListener(myButtonClickListener);
        }
        for (int i = 0; i < mTiltingButtons.length; i++) {
            mTiltingButtons[i] = findViewById(mTiltingButtonIDs[i]);
            mTiltingButtons[i].setOnClickListener(myButtonClickListener);
        }
        for (int i = 0; i < mTiltingModeButtons.length; i++) {
            mTiltingModeButtons[i] = findViewById(mTiltingModeButtonIDs[i]);
            mTiltingModeButtons[i].setBackground(getResources().getDrawable(shape_mode_button));
            mTiltingModeButtons[i].setOnClickListener(myButtonClickListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                bluetooth_menuitem = item;
                checkBluetooth();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    selectDevice();
                } else if (requestCode == RESULT_CANCELED) {
                    showToast("블루투스 연결을 취소하였습니다.");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mWorkerThread.interrupt(); //Finish Thread
            mInputStream.close();
            mOutputStream.close();
            mSocket.close(); // Finish Socket
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMovePosition(float positionX, float positionY) {
        tvPosition.setText("Joystick Move X " + positionX + " Move Y : " + positionY);
        int mSpeed_L = 0;
        int mSpeed_R = 0;
        int mJoysticSpeed = (int) Math.sqrt(Math.pow(positionX, 2) + Math.pow(positionY, 2));
        if (mJoysticSpeed > 100)
            mJoysticSpeed = 100;

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        float abspositionX = Math.abs(positionX);
        float abspositionY = Math.abs(positionY);

        if (positionX >= 0) {
            mSpeed_L = mJoysticSpeed;
            mSpeed_R = (int) (mJoysticSpeed * (0.1 + 0.9 * (1 - (abspositionX / (abspositionX + abspositionY)))));
        } else {
            mSpeed_R = mJoysticSpeed;
            mSpeed_L = (int) (mJoysticSpeed * (0.1 + 0.9 * (1 - (abspositionX / (abspositionX + abspositionY)))));
        }
        String strcmd = "";
        if (positionY >= 0) {
            strcmd = "CF";
        } else {
            strcmd = "CB";
        }
        sendData(strcmd, mSpeed_L, mSpeed_R);
        tvSpeedCmd.setText(("Speed Command : " + strcmd + " / Speed_L : " + mSpeed_L + "%" + " / Speed_R : " + mSpeed_R + "%"));
    }

    @Override
    public void sendTiltingCommand(String cmd, int data1, int data2) {
        sendData(cmd, data1, data2);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void carModeChanged(View selectedButton, int selectedMode) {
        for (int i = 0; i < mCarModeButtons.length; i++) {
            mCarModeButtons[i].setSelected(false);
        }
        selectedButton.setSelected(true);
        switch (selectedMode) {
            case CAR_MODE_JOYSTICK:
                ivJoystick.setVisibility(View.VISIBLE);
                layoutButtonsCar.setVisibility(View.INVISIBLE);
                layoutSensorButtonsCar.setVisibility(View.INVISIBLE);
                break;
            case CAR_MODE_BUTTON:
                ivJoystick.setVisibility(View.INVISIBLE);
                layoutButtonsCar.setVisibility(View.VISIBLE);
                layoutSensorButtonsCar.setVisibility(View.INVISIBLE);
                break;
            case CAR_MODE_SENSOR:
                ivJoystick.setVisibility(View.INVISIBLE);
                layoutButtonsCar.setVisibility(View.INVISIBLE);
                layoutSensorButtonsCar.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void tiltingModeChanged(View selectedButton, int selectedMode) {
        for (int i = 0; i < mTiltingModeButtons.length; i++) {
            mTiltingModeButtons[i].setSelected(false);
        }
        selectedButton.setSelected(true);
        switch (selectedMode) {
            case TILTING_MODE_BUTTON:
                layoutButtonsTilting.setVisibility(View.VISIBLE);
                break;
            case TILTING_MODE_TRACKING:
                layoutButtonsTilting.setVisibility(View.INVISIBLE);
                break;
        }
    }

    //Check the Bluetooth Connection
    void checkBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // bluetoothAdapter Device
        if (bluetoothAdapter == null) {
            showToast("블루투스를 지원하지 않습니다.");
        } else {
            //if
            // Bluetooth is not Enabled
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            } else {
                //if Bluetooth is Enabled
                selectDevice();
            }
        }
    }

    //Select a Bluetooth Device
    void selectDevice() {
        mDevices = bluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = mDevices.size();
        if (mPairedDeviceCount == 0) {
            showToast("연결할 블루투스 장치가 하나도 없습니다.");
        } else {
            AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
            dlgBuilder.setTitle("블루투스 장치 선택");
            final ArrayList<String> listItems = new ArrayList<>();
            for (BluetoothDevice device : mDevices) {
                listItems.add(device.getName());
            }
            listItems.add("취소");
            final CharSequence items[] = listItems.toArray(new CharSequence[listItems.size()]);
            dlgBuilder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == mPairedDeviceCount) {
                        showToast("취소를 선택했습니다.");
                    } else {
//                        connectToSelectedDevice(items[which].toString());
                        connectToSelectedDevice(listItems.get(which).toString());
                    }
                }
            });
            dlgBuilder.setCancelable(false);
            dlgBuilder.show();
        }
    }

    //Connect Selected Device
    void connectToSelectedDevice(String selectedDeviceName) {
        mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        String uuids = "";
        for (int i = 0; i < mRemoteDevice.getUuids().length; i++) {
            uuids += mRemoteDevice.getUuids()[i].getUuid() + "\n";
        }
        Log.i("Selected_Device_UUIDs", uuids);
        try {
            mSocket = mRemoteDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            mSocket.connect(); // Try Connect Device
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();
            beginListenForData();
            bluetooth_menuitem.setIcon(R.drawable.ic_bluetooth_connected);
        } catch (Exception e) {
            showToast("블루투스 연결 중 오류가 발생하였습니다.");
            bluetooth_menuitem.setIcon(R.drawable.ic_bluetooth_black);
        }
    }

    //Get Paired Device
    BluetoothDevice getDeviceFromBondedList(String name) {
        BluetoothDevice selectedDevice = null;
        for (BluetoothDevice device : mDevices) {
            if (name.equals(device.getName())) {
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }

    //Ready to Receive Data from Device
    void beginListenForData() {
        final Handler handler = new Handler();
        readBuffer = new byte[1024];
        readBufferPosition = 0;
        mWorkerThread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        int bytesAvailable = mInputStream.available();
                        if (bytesAvailable > 0) {
                            byte packetBytes[] = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte pByte = packetBytes[i];
                                if (pByte == mCharDelimiter) {
                                    // if find mCharDelimiter, process handler and init buffer.
                                    byte encodeBytes[] = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodeBytes, 0, encodeBytes.length);
                                    // data is a line that ending with "\n" received from Bluetooth.
                                    final String data = new String(encodeBytes, US_ASCII);  //"US_ASCII"
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Received data processing ///////////////////////////////////////////////////////
                                            Log.d("Received_Data", data);

                                        }
                                    });
                                } else {
                                    // if don't find mCharDelimiter, read a line By byte
                                    readBuffer[readBufferPosition++] = pByte;
                                }
                            }
                        }
                    } catch (Exception e) {
                        showToast("데이터 수신 중 오류가 발생하였습니다.");
                    }
                }
            }
        });
        mWorkerThread.start();
    }

    // Ready to Transfer Data to Device

    void sendData(String cmd, int data1, int data2) {

        byte sendBytes[] = new byte[8];
        char cmdArray[] = cmd.toCharArray();
        int length = 4;
        if (data1 > 127) {
            data1 -= 256;
        }
        if (data2 > 127) {
            data2 -= 256;
        }
        sendBytes[0] = (byte) sendHeader;
        sendBytes[1] = (byte) length;
        sendBytes[2] = (byte) cmdArray[0];
        sendBytes[3] = (byte) cmdArray[1];
        sendBytes[4] = (byte) data1;
        sendBytes[5] = (byte) data2;
        sendBytes[6] = (byte) mEndCharDelimiter1;
        sendBytes[7] = (byte) mEndCharDelimiter2;
        String sendData = "";
        for (int i = 0; i < sendBytes.length; i++) {
            sendData += " " +  sendBytes[i];
        }
        Log.d("Send BT Data", sendData);
        try {
            mOutputStream.write(sendBytes);
        } catch (Exception e) {
            showToast("데이터 전송 중 오류가 발생하였습니다.");
        }
    }

    void showToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

}
