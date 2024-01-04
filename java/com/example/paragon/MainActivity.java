package com.example.paragon;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.paragon.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.IntPredicate;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());


        View view = binding.getRoot();
        setContentView(view);

        binding.scanButton.setOnClickListener(this::handleScanClick);
        binding.loadButton.setOnClickListener(this::handleLoadClick);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        IntPredicate denied = (x) -> {return x==PackageManager.PERMISSION_DENIED;};
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(Arrays.stream(grantResults).anyMatch(denied)){
            Log.d("logpermissions", "onRequestPermissionsResult: works");
        }
        else if(requestCode == 1){
            this.startNewActivity(TakePicActivity.class);
        }
        else if(requestCode == 2){
            this.startNewActivity(LoadPicActivity.class);
        }
    }

    private void handleScanClick(View e) {


        if(!Helpers.checkCameraPermission(this)){
            String[] permissions = new String[1];
            permissions[0] = (Manifest.permission.CAMERA);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
        else {
            this.startNewActivity(TakePicActivity.class);
        }


    }
    private void handleLoadClick(View e) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(!Helpers.checkFilesPermission(this)) {
                String[] permissions = new String[1];
                permissions[0] = (Manifest.permission.READ_MEDIA_IMAGES);
                requestPermissions(permissions, 2);
            }
            else {
                this.startNewActivity(LoadPicActivity.class);
            }
        }
        else {
            this.startNewActivity(LoadPicActivity.class);
        }
    }
    private void startNewActivity(Class c) {
        Intent i = new Intent(this, c);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }






}