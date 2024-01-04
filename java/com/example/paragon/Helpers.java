package com.example.paragon;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Helpers {
    public static boolean checkCameraPermission(Context Ctx) {
        String cameraPermission = Manifest.permission.CAMERA;
        return Ctx.checkSelfPermission(cameraPermission) == PackageManager.PERMISSION_GRANTED;

    }
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static boolean checkFilesPermission(Context Ctx) {
        String filePermission = Manifest.permission.READ_MEDIA_IMAGES;
        return Ctx.checkSelfPermission(filePermission) == PackageManager.PERMISSION_GRANTED;
    }
}
