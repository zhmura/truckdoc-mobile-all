package com.sanda.truckdoc.client.to;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.to.adapters.TrailerTypeAdapter;
import com.sanda.truckdoc.client.to.data.Model;
import com.sanda.truckdoc.client.to.data.TrailerType;
import com.sanda.truckdoc.client.to.service.NewMntService;
import com.sanda.truckdoc.client.to.utils.LocalStorage;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by k.natallie on 08.02.2016.
 */
public class EnterTruckDataActivity extends AppCompatActivity {
    private Button btnSubmit;
    private Button btnDelete;
    private EditText trackNumber;
    private EditText trailerNumber;
    private Spinner trailerType;
    private String trailerId;
    private LocalStorage storage;
    private ArrayAdapter<TrailerType> dataAdapter;
    private ActionBar actionBar;
    private TextView error;
    private TextView info;
    private ResponseReceiver receiver = new ResponseReceiver();
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.to_title);
        }
        setContentView(R.layout.activity_mnt_enter_numbers);
        storage = LocalStorage.getInstance(getApplicationContext());
        model = Model.getInstance(getApplicationContext());
        dataAdapter = new TrailerTypeAdapter(this,
                android.R.layout.simple_spinner_item, new TrailerType[]{
                new TrailerType(getString(R.string.select_trailer_type), getString(R.string.select_trailer_type_id)),
                new TrailerType(getString(R.string.trailer_tent), getString(R.string.trailer_tent_id)),
                new TrailerType(getString(R.string.trailer_fridge), getString(R.string.trailer_fridge_id)),

        });
        initUIElements();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ServiceResultReceiver.ACTION_SENT_MAINTENANCE_OK);
        filter.addAction(ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void initUIElements() {
        btnSubmit = findViewById(R.id.btn_submit);
        //btnDelete = (Button)findViewById(R.id.delete);
        error = findViewById(R.id.error);
        info = findViewById(R.id.mnt_info);
        Long mntReportDate = storage.readLongPreference(LocalStorage.LAST_MNT_REPORT, 0);
        if (mntReportDate != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", this.getResources().getConfiguration().locale);
            String reportInfo = "Последний отчет ТО успешно отправлен " + sdf.format(new Date(mntReportDate));
            info.setText(reportInfo);
            info.setVisibility(View.VISIBLE);
        }
        showEditMode();

     /*   btnDelete.setOnClickListener((View v) ->{
            LocalStorage.getInstance(getApplicationContext()).removePreference(LocalStorage.TO_PROGRESS);
        });
        */
        btnSubmit.setOnClickListener((View v) -> {
            //TODO implement request with sending numbers to server
            if (isValid()) {
                Model.getInstance(getApplicationContext()).setupMaintenance(trailerId);
                startActivity(new Intent(getApplicationContext(), TOTreeActivity.class));
                storage.removePreference(LocalStorage.TO_SEND_PROGRESS);
            } else {

            }
        });

    }

    private void showEditMode() {
        String trackNumberString = storage.readStringPreference(LocalStorage.TRACK_NUMBER, "");

        btnSubmit.setText(getString(R.string.mnt_btn_start_mnt));

        trackNumber = findViewById(R.id.track_name);
        trackNumber.setText(trackNumberString);
        trailerNumber = findViewById(R.id.trailer_number);
        trailerNumber.setText(storage.readStringPreference(LocalStorage.TENT_NUMBER));
        trailerType = findViewById(R.id.trailerType);
        trailerType.setAdapter(dataAdapter);
        int tentTypeId = storage.readIntPreference(LocalStorage.TENT_TYPE, 0);
        if (tentTypeId >= dataAdapter.getCount()) {
            tentTypeId = 0;
        }
        trailerType.setSelection(tentTypeId);
        trackNumber.setText(storage.readStringPreference(LocalStorage.TRACK_NUMBER, ""));
        trailerNumber.setText(storage.readStringPreference(LocalStorage.TENT_NUMBER));
        trailerNumber.requestFocus();
        trackNumber.requestFocus();
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(trackNumber.getText())) {
            trackNumber.setError(getString(R.string.to_error_tack));
            return false;
        }
        if (!TextUtils.isEmpty(trailerNumber.getText()) && trailerType.getSelectedItemId() == 0) {
            error.setText(getString(R.string.to_trailer_type));
            error.setVisibility(View.VISIBLE);
            return false;
        } else {
            error.setText("");
            error.setVisibility(View.GONE);
        }
        trailerId = ((TrailerType) trailerType.getSelectedItem()).getId();
        storage.writeStringPreference(LocalStorage.TRACK_NUMBER, trackNumber.getText().toString());
        storage.writeStringPreference(LocalStorage.TENT_NUMBER, trailerNumber.getText().toString());
        storage.writeIntPreference(LocalStorage.TENT_TYPE, trailerType.getSelectedItemPosition());
        storage.writeStringPreference(LocalStorage.TENT_TYPE_ID, trailerId);

        return true;
    }

    public class ResponseReceiver extends ServiceResultReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR.equals(intent.getAction())) {
                String text = intent.getStringExtra(MessageCheckService.PARAM_OUT_MSG);
                error.setText(text);
                error.setVisibility(View.VISIBLE);
                btnSubmit.setEnabled(false);
                btnSubmit.setText("Идет отправка отчета ТО ...");
            } else if (ServiceResultReceiver.ACTION_SENT_MAINTENANCE_OK.equals(intent.getAction())) {
                btnSubmit.setText(getString(R.string.mnt_btn_start_mnt));
                btnSubmit.setEnabled(true);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        String jsonTO = storage.readStringPreference(LocalStorage.TO_PROGRESS, null);
        Log.d("tojson", jsonTO + " ");
        if (jsonTO != null) {
            String progress = storage.readStringPreference(LocalStorage.TO_SEND_PROGRESS);
            if (progress != null && model.isFullFilled()) {
                NewMntService.Status status = NewMntService.Status.valueOf(progress);
                switch (status) {
                    case UPLOAD_FILES:
                    case SEND_ATTEMTP:
                    case SEND_ERROR: {
                        btnSubmit.setEnabled(false);
                        btnSubmit.setBackgroundColor(getResources().getColor(R.color.mnt_wait));
                        btnSubmit.setText("Идет отправка отчета ТО ...");
                        break;
                    }
                    case SEND_FINISHED:
                        btnSubmit.setEnabled(true);
                        btnSubmit.setBackgroundColor(getResources().getColor(R.color.mnt_neutral));
                        btnSubmit.setText(getString(R.string.mnt_btn_start_mnt));
                }


            } else {
                btnSubmit.setText(getString(R.string.mtn_btn_continue_mnt));
                btnSubmit.setEnabled(true);
            }

        } else {
            btnSubmit.setText(getString(R.string.mnt_btn_start_mnt));
            btnSubmit.setEnabled(true);
        }
    }


}