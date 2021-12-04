package com.app.myapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DiscoveryCallback;

public class Parametres extends AppCompatActivity {

    Bluetooth bluetooth;
    ArrayList<BlueDevice> bluetoothDevices;
    ListView blueVerrouList, blueVitresList, blueAllumageList;
    EditText blueVitresAdr, blueVerrouAdr, blueAllumageAdr;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        blueVerrouList = findViewById(R.id.listBlueVerrou);
        blueVitresList = findViewById(R.id.listBlueVitres);
        blueAllumageList = findViewById(R.id.listBlueAllumage);

        blueVitresAdr = findViewById(R.id.blueVitresField);
        blueVerrouAdr = findViewById(R.id.blueVerrouField);
        blueAllumageAdr = findViewById(R.id.blueAllumageField);

        bluetoothDevices = new ArrayList<>();

        sharedPreferences = getSharedPreferences("MyAutoApp", Context.MODE_PRIVATE);

        blueVitresAdr.setText(sharedPreferences.getString("VitresBlue",""));
        blueVerrouAdr.setText(sharedPreferences.getString("VerrouBlue",""));
        blueAllumageAdr.setText(sharedPreferences.getString("AllumageBlue",""));

        bluetooth = new Bluetooth(this);
        bluetooth.setBluetoothCallback(bluetoothCallback);

        bluetooth.setDiscoveryCallback(new DiscoveryCallback() {
            @Override public void onDiscoveryStarted() {}
            @Override public void onDiscoveryFinished() {
                ArrayAdapter<BlueDevice> adapter = new ArrayAdapter<BlueDevice>(Parametres.this, android.R.layout.simple_list_item_1, bluetoothDevices);
                blueVerrouList.setAdapter(adapter);
                blueVitresList.setAdapter(adapter);
                blueAllumageList.setAdapter(adapter);
            }
            @Override public void onDeviceFound(BluetoothDevice device) {
                BlueDevice discovredDevice = new BlueDevice(device.getName(), device.getAddress());
                if(! contains(discovredDevice)) {
                    bluetoothDevices.add(discovredDevice);
                }
            }
            @Override public void onDevicePaired(BluetoothDevice device) {}
            @Override public void onDeviceUnpaired(BluetoothDevice device) {}
            @Override public void onError(int message) {}
        });

        blueVerrouList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                blueVerrouAdr.setText(bluetoothDevices.get(i).adrress);
            }
        });

        blueVitresList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                blueVitresAdr.setText(bluetoothDevices.get(i).adrress);
            }
        });

        blueAllumageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                blueAllumageAdr.setText(bluetoothDevices.get(i).adrress);
            }
        });

    }

    private boolean contains(BlueDevice descovredDevice) {
        for(int i=0; i<bluetoothDevices.size(); i++){
            if(bluetoothDevices.get(i).adrress.contains(descovredDevice.adrress)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        if(bluetooth.isEnabled()){
            scan();
        } else {
            bluetooth.enable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.onStop();
    }

    private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
        @Override public void onBluetoothTurningOn() {}
        @Override public void onBluetoothTurningOff() {}
        @Override public void onBluetoothOff() {}
        @Override public void onUserDeniedActivation() {}
        @Override
        public void onBluetoothOn() {
            scan();
        }
    };

    public void scan(){
        bluetoothDevices.clear();
        bluetooth.startScanning();
    }

    public void sauvegarder(View view) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("VitresBlue", blueVitresAdr.getText().toString());
        edit.putString("VerrouBlue", blueVerrouAdr.getText().toString());
        edit.putString("AllumageBlue", blueAllumageAdr.getText().toString());
        edit.commit();
        Toast.makeText(this, "Data Saved Successfully !", Toast.LENGTH_SHORT).show();
    }

    class BlueDevice{
        public String name;
        public String adrress;

        BlueDevice(String name, String adr){
            this.name = name;
            this.adrress = adr;
        }

        @Override
        public String toString() {
            return "Name:" + name + "\n" +
                    "Adrress:'" + adrress;
        }
    }
}