package com.sanda.truckdoc.client.ui;

import android.content.Intent;

import com.sanda.truckdoc.client.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import app.camera.tdoc.camera_library.CamActivity;
import app.camera.tdoc.camera_library.PrefixList;

import static app.camera.tdoc.camera_library.ImageType.HQ_SCAN;
import static app.camera.tdoc.camera_library.ImageType.MQ_SCAN;

@EActivity(R.layout.activity_scanner)
public class ScannerActivity extends AppCompatActivity {

    @Extra
    long recipientId;

    @SuppressWarnings("ConstantConditions")
    @AfterViews
    void afterViews() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
    }

    @OptionsItem(android.R.id.home)
    void onHome() {
        finish();
    }

    @Click(R.id.btnCMRScan)
    void onCMRScan() {
        startCameraActivity(HQ_SCAN, recipientId);
    }

    @Click(R.id.btnScanOthers)
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

/*    @Receiver(actions = ServiceResultReceiver.NOTIFICATION_MESSAGE,
            registerAt = OnResumeOnPause)
    void onNotificationMessage(Intent intent) {
        String message = intent.getStringExtra(NotificationHelper.PARAM_MSG);
        Boolean isError = intent.getBooleanExtra(NotificationHelper.PARAM_IS_ERROR, false);
        if (isError) {
            NotificationHelper.showErrorMessage(message, this);
        } else {
            NotificationHelper.showNotificationMessage(message, this);
        }
    }*/
}
