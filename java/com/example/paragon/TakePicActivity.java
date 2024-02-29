package com.example.paragon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.impl.utils.CompareSizesByArea;
import androidx.camera.core.internal.CameraUseCaseAdapter;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.example.paragon.databinding.ActivityTakePicBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class TakePicActivity extends AppCompatActivity {
    private ActivityTakePicBinding binding;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }
    private Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private ImageReader mReader;
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
    private TextureView.SurfaceTextureListener txtViewStateCallback = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };
    private static  int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation){
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private ImageReader.OnImageAvailableListener mImageReaderStateCallback = reader -> {
        Image image = reader.acquireLatestImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmapImage, "paragon", "picture to get Text");
        image.close();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTakePicBinding.inflate(getLayoutInflater());
        //cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        View view = binding.getRoot();
        setContentView(view);
        startBackgroundThread();
        int x = binding.txtView.getWidth();
        int y = binding.txtView.getHeight();
        setUpCamera(binding.txtView.getWidth(), binding.txtView.getHeight());
        if(binding.txtView.isAvailable())
            startPreview();
        else
            binding.txtView.setSurfaceTextureListener(txtViewStateCallback);
        binding.takePicBtn.setOnClickListener(this::handleButtonClick);
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

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
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

        int x = binding.txtView.getWidth();
        int y = binding.txtView.getHeight();
        mReader = ImageReader.newInstance(x, y, ImageFormat.JPEG, 1);
        mReader.setOnImageAvailableListener(mImageReaderStateCallback, mBackgroundHandler);
        SurfaceTexture surfaceTexture = binding.txtView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(x, y);
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
    @SuppressLint("MissingPermission")//cant enter this activity without permission
    private void handleButtonClick(View v){
        Executor ex = command -> {

        };
        String pathtoImage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+ "/Camera/IMG_20150521_144859.jpg";
        File file = new File(pathtoImage);
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file).build();
        new ImageCapture.Builder()
                .build().takePicture(options, ex, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(TakePicActivity.this);
                          dialog.setMessage("Pomślnie zrobiono zdjęcie");
                          dialog.setTitle("Zdjęcie");
                          dialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                  return;
                              }
                          });
                        dialog.show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(TakePicActivity.this);
                        dialog.setMessage(exception.getMessage());
                        dialog.setTitle("Error");
                        dialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                        dialog.show();
                    }
                });
//            mCameraDevice.createCaptureSession(new SessionConfiguration(SessionConfiguration.SESSION_REGULAR, ));
//            mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).build();

    }
}