package com.example.paragon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.internal.CameraUseCaseAdapter;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.example.paragon.databinding.ActivityTakePicBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class TakePicActivity extends AppCompatActivity {
    private ActivityTakePicBinding binding;
    private CameraDevice mCameraDevice;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CameraDevice.StateCallback mCameraDevicaStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            Log.d("cameraId", "onOpened: ");
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTakePicBinding.inflate(getLayoutInflater());
        //cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        View view = binding.getRoot();
        setContentView(view);
        startBackgroundThread();
        setUpCamera(binding.txtView.getWidth(), binding.txtView.getHeight());
        startPreview();

    }
    @SuppressLint("MissingPermission")
    private void setUpCamera(int width, int height)  {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            for(String cameraId: cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK)
                {
                    cameraManager.openCamera(cameraId, mCameraDevicaStateCallback, mBackgroundHandler);
                }
            }
        }
        catch(CameraAccessException e){

        }
    }

    private void closeCamera(){
        if(mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
        //stopBackgroundThread();

    }
    private void startPreview() {
        SurfaceTexture surfaceTexture = binding.txtView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(binding.txtView.getWidth(), binding.txtView.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(),null, mBackgroundHandler );
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d("CameraId", "onConfigureFailed: ");
                }
            }, null);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

    }
    private void startBackgroundThread() {
        mBackgroundHandlerThread  = new HandlerThread("Camera2Image");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }
    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}