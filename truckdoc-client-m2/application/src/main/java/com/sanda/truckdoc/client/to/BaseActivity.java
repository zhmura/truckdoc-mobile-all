package com.sanda.truckdoc.client.to;

import android.os.Bundle;

import com.sanda.truckdoc.client.to.data.Model;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by k.natallie on 17.02.2016.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected Model model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = Model.getInstance(getApplicationContext());
    }
}
