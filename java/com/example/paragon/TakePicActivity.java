package com.example.paragon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;

import com.example.paragon.databinding.ActivityTakePicBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class TakePicActivity extends AppCompatActivity {
    private ActivityTakePicBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTakePicBinding.inflate(getLayoutInflater());
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        View view = binding.getRoot();
        setContentView(view);


        cameraProviderFuture.addListener(this::requestCameraProvider, ContextCompat.getMainExecutor(this));
    }

    private void requestCameraProvider() {
        try {
            this.cameraProvider = cameraProviderFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            // No errors need to be handled for this Future.
            // This should never be reached.
        }
    }
}