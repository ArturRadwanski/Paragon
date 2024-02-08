package com.example.paragon;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.util.Comparator;

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
    public static class ComopareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size o1, Size o2) {
            return Long.signum((long) o1.getWidth() * o1.getWidth() /
                    (long) o2.getWidth() * o2.getHeight());
        }
    }
}
