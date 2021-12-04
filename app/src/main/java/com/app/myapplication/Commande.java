package com.app.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.DeviceCallback;

public class Commande extends AppCompatActivity {

    String vehicleAllumageBlue, vehicleVerroBlue, vehicleVitresBlue;
    SharedPreferences sharedPreferences;
    private final int REQUEST_CODE = 8000;
    Bluetooth bluetooth;
    String msg = "";
    String currentAdr = "", previousAdr = "";
    boolean j = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commande);

        sharedPreferences = getSharedPreferences("MyAutoApp", Context.MODE_PRIVATE);

        vehicleVitresBlue = sharedPreferences.getString("VitresBlue","");
        vehicleVerroBlue = sharedPreferences.getString("VerrouBlue","");
        vehicleAllumageBlue = sharedPreferences.getString("AllumageBlue","");

        bluetooth = new Bluetooth(this);

        bluetooth.setDeviceCallback(new DeviceCallback() {
            @Override public void onDeviceConnected(BluetoothDevice device) {
                if(! msg.isEmpty()) {
                    bluetooth.send(msg.getBytes());
                }
            }
            @Override public void onDeviceDisconnected(BluetoothDevice device, String message) {
                if(j == true){
                    bluetooth.connectToAddress(currentAdr);
                }
            }
            @Override public void onMessage(byte[] message) {}
            @Override public void onError(int message) {}
            @Override public void onConnectError(BluetoothDevice device, String message) {}
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        if(! bluetooth.isEnabled()){
            bluetooth.enable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        j = false;
        bluetooth.onStop();
        bluetooth.disconnect();
    }

    public void speak(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dit quelque choses");
        try{
            startActivityForResult(intent, REQUEST_CODE);
        }catch(Exception e){
            // Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void rightWindowDown(View view) {
        connectAndSend("6", vehicleVitresBlue);
    }

    public void rightWindowUP(View view) {
        connectAndSend("5", vehicleVitresBlue);
    }

    public void leftWindowDown(View view) {
        connectAndSend("8", vehicleVitresBlue);
    }

    public void leftWindowUp(View view) {
        connectAndSend("7", vehicleVitresBlue);
    }

    public void lockUnlock2(View view) {
        connectAndSend("2", vehicleVerroBlue);
    }

    public void lockUnlock(View view) {
        connectAndSend("4", vehicleVerroBlue);
    }

    public void trunk(View view) {
        connectAndSend("3", vehicleVerroBlue);
    }

    public void on(View view) {
        connectAndSend("2", vehicleAllumageBlue);
    }

    public void off(View view) {
        connectAndSend("3", vehicleAllumageBlue);
    }

    /*public void sendGSMMsg(String msg) {
        if(checkPermission(this, Manifest.permission.SEND_SMS)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(vehicleGSM, null,
                    msg, null, null);
            Toast.makeText(getApplicationContext(),
                    "Demmande Envoyée!", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),
                    "Demmande Réfusée", Toast.LENGTH_LONG).show();
        }
    }*/

    public static boolean checkPermission(Context context, String permission){
        int check = ContextCompat.checkSelfPermission(context, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    switch(result.get(0)){
                        case "coffre":
                            connectAndSend("2", vehicleVerroBlue);
                            break;
                        case "on":
                            connectAndSend("2", vehicleAllumageBlue);
                            break;
                        case "off":
                            connectAndSend("3", vehicleAllumageBlue);
                            break;
                        case "ouvrir":
                            connectAndSend("3", vehicleVerroBlue);
                            break;
                        case "fermer":
                            connectAndSend("4", vehicleVerroBlue);
                            break;
                    }
                }
                break;
        }
    }

    public void connectAndSend(String content, String address){
        msg = content;
        previousAdr = currentAdr;
        currentAdr = address;
        if(bluetooth.isConnected()){
            if(currentAdr.equals(previousAdr)){
                bluetooth.send(msg.getBytes());
            } else {
                j = true;
                bluetooth.disconnect();
            }
        } else {
            bluetooth.connectToAddress(address);
        }
    }

}