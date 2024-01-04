package com.example.paragon;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.paragon.databinding.ActivityLoadPicBinding;
import com.example.paragon.databinding.ActivityMainBinding;

public class LoadPicActivity extends AppCompatActivity {
    private ActivityLoadPicBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadPicBinding.inflate(getLayoutInflater());


        View view = binding.getRoot();
        setContentView(view);
    }

}
