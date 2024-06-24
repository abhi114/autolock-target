package com.example.autolock_target;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminComponentName;
    private Socket mSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this,MyDeviceAdminReciever.class);
        if(!mDPM.isAdminActive(mAdminComponentName)){
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,mAdminComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"device admin explanation");
            startActivityForResult(intent,REQUEST_CODE_ENABLE_ADMIN);
        }
        try {
            mSocket = IO.socket("https://south-bright-suggestion.glitch.me");
            mSocket.connect();
        }catch (URISyntaxException e){
            e.printStackTrace();
        }

        mSocket.on("control", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String signal = (String) args[0];
                if("lock".equals(signal)){
                    lockDevice();
                }else if("unlock".equals(signal)){  
                    unlockDevice();
                }
            }
        });
    }

    private void unlockDevice() {
    }

    private void lockDevice() {
        if(mDPM.isAdminActive(mAdminComponentName)){
            mDPM.lockNow();
        }else{
            Toast.makeText(this, "Admin Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ENABLE_ADMIN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Admin Enabled", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Admin enable failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}