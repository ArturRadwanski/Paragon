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
            Log.d("permissions", "onRequestPermissionsResult: works");
        }
        else {
            this.startNewActivity(TakePicActivity.class);
        }
    }

    private void handleScanClick(View e) {
        ArrayList<String> permissions = new ArrayList<>();

        if(!Helpers.checkCameraPermission(this)){
            permissions.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(!Helpers.checkFilesPermission(this)) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        }
        String[] s =  permissions.toArray(new String[permissions.size()]);

        if(permissions.size() > 0) {
            requestPermissions(s, 1);
        }
        else {
            this.startNewActivity(TakePicActivity.class);
        }


    }
    private void handleLoadClick(View e) {
        Intent i = new Intent(this, LoadPicActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }
    private void startNewActivity(Class c) {
        Intent i = new Intent(this, c);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }






}