package com.sanda.truckdoc.client.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.sanda.truckdoc.client.R;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import app.camera.tdoc.camera_library.CamActivity;
import app.camera.tdoc.camera_library.PrefixList;

import static app.camera.tdoc.camera_library.ImageType.HQ_SCAN;
import static app.camera.tdoc.camera_library.ImageType.MQ_SCAN;

public class ScannerActivity extends AppCompatActivity {

    private long recipientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        
        // Get recipientId from intent
        recipientId = getIntent().getLongExtra("recipientId", -1);
        
        setupViews();
    }

    private void setupViews() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowHomeEnabled(true);
        }
        
        // Set up click listeners
        Button btnCMRScan = findViewById(R.id.btnCMRScan);
        Button btnScanOthers = findViewById(R.id.btnScanOthers);
        
        btnCMRScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMRScan();
            }
        });
        
        btnScanOthers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onScanOthers();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onCMRScan() {
        startCameraActivity(HQ_SCAN, recipientId);
    }

    void onScanOthers() {
        startCameraActivity(MQ_SCAN, recipientId);
    }

    public void startCameraActivity(app.camera.tdoc.camera_library.ImageType type, Long recipientId) {
        ArrayList<PrefixList> prefixes = new ArrayList<>();
        prefixes.add(new PrefixList("СМR-накладная", "CMR"));
        prefixes.add(new PrefixList("Инвойс", "INVOICE"));
        prefixes.add(new PrefixList("Упаковочный лист", "LIST"));
        prefixes.add(new PrefixList("Книжка МДП", "TIR"));
        prefixes.add(new PrefixList("Коммерческое описание", "COM_DESC"));
        prefixes.add(new PrefixList("ДКД", "DDC"));
        prefixes.add(new PrefixList("Экспортная декларация", "EXPORT"));
        prefixes.add(new PrefixList("Прочее", "OTHER"));

        Intent i = CamActivity.newBuilder()
                .setFolderName("TruckDoc")
                .setGalleryFolderName("Gallery")
                .setImagePrefixList(prefixes)
                .setPrefixEnable(true)
                .setRecipient(recipientId)
                .setImageType(type)
                .setDeleteButtonVisibility(true)
                .setSendButtonVisibility(true)
                .setSettingButtonVisibility(true)
                .setVideoButtonVisibility(false)
                .setExposureEnable(true)
                .setWhiteBalanceEnable(true)
                .setColorEffectsEnable(true)
                .setBordersOptionEnable(true)
                .setResolutionOptionEnable(true)
                .setIsoOptionEnable(true)
                .setFocusOptionEnable(true)
                .setFlashOptionEnable(true)
                .setAutoStabiliseOptionEnable(true)
                .setTimeStampeEnable(!type.isForDoc())
                .setLocationStampEnable(!type.isForDoc())
                .build(this);
        startActivity(i);
    }
}
