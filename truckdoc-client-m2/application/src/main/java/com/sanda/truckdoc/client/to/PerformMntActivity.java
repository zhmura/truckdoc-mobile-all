package com.sanda.truckdoc.client.to;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.to.data.ToNode;

import java.util.ArrayList;
import java.util.regex.Pattern;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import app.camera.tdoc.camera_library.CamActivity;
import app.camera.tdoc.camera_library.ImageType;
import app.camera.tdoc.camera_library.PrefixList;

import static com.sanda.truckdoc.client.to.data.TOState.UNDEFINED;
import static com.sanda.truckdoc.client.to.data.TOState.NOT_OK;

/**
 * Created by k.natallie on 04.02.2016.
 */
public class PerformMntActivity extends BaseActivity {
    public static final String IS_COMMENT = "isComment";
    //private ImageButton btnPhoto;
    private Button btnSaveComment, btnCancel, btnOk, btnNotOk, btnCanNotCheck, btnPhoto;
    // private ToSubItem item;
    private ToNode item;
    private EditText edtComment;

    private TextView commentTitle;
    private RelativeLayout commentSection;
    private ActionBar actionBar;
    private LinearLayout btns;
    private Boolean isComment = null;
    private boolean isKeyboardOn;
    private View activityRootView;
    private ImageView titleImageView;
    private CollapsingToolbarLayout collapsingToolbar;
    private NestedScrollView scrollView;
    private String resName;
    private ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > 600) { // if more than 100 pixels, its probably a keyboard...
                isKeyboardOn = true;
                scrollView.scrollTo(0, 0);
            } else {
                isKeyboardOn = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_to);
        activityRootView = findViewById(R.id.root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        scrollView = findViewById(R.id.scroll);

        btns = findViewById(R.id.btn_section);
        item = model.getCurrentNode();
        collapsingToolbar =
                findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(item.getName());


        titleImageView = findViewById(R.id.image);
        resName = model.getNameOfIconForNode(model.getConfigNodes(), item);

        int drawableResourceId = getResources().getIdentifier("mnt_icon_" + resName, "drawable", getPackageName());
        if (drawableResourceId > 0) {
            titleImageView.setImageResource(drawableResourceId);
        } else {
            titleImageView.setImageResource(R.drawable.mnt_icon_unknown);
        }

        titleImageView.setColorFilter(getResources().getColor(R.color.mnt_neutral), PorterDuff.Mode.XOR);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {

                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setShowHideAnimationEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        TextView titleText = findViewById(R.id.title_text);
        titleText.setText((item.getParent() != null) ?
                item.getParent().getName() : "");
        commentSection = findViewById(R.id.comment_section);

        btnPhoto = findViewById(R.id.btn_camera);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTOCameraActivity(app.camera.tdoc.camera_library.ImageType.SCENE_PHOTO, 0);
            }


        });

        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highlightOK();
                showCommentSection(false);
                saveOkState();
                finish();
            }


        });

        btnCanNotCheck = findViewById(R.id.btn_can_not_check);
        if (Boolean.TRUE.equals(item.getMayNotBeChecked())) {
            btnCanNotCheck.setVisibility(View.VISIBLE);
            btnCanNotCheck.setOnClickListener(v -> {
                saveUndefinedState();
                showCommentSection(false);
                finish();
            });
        }

        btnNotOk = findViewById(R.id.btn_not_ok);
        btnNotOk.setOnClickListener(v -> {
            saveBadState();
            showCommentSection(true);
        });

        if (savedInstanceState != null) {
            isComment = savedInstanceState.getBoolean(IS_COMMENT);
        }

        edtComment = findViewById(R.id.comment);
        edtComment.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
        edtComment.setText(item.getComment());

        btnSaveComment = findViewById(R.id.save_comment);
        btnSaveComment.setEnabled(!Boolean.TRUE.equals(item.getValidated())
                || (edtComment.getText() != null
                && isValidComment(edtComment.getText().toString())));
        btnSaveComment.setOnClickListener(v -> {
            item.setComment(edtComment.getText().toString());
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (PerformMntActivity.this.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(PerformMntActivity.this.getCurrentFocus().getWindowToken(), 0);
            }
            if (Boolean.TRUE.equals(item.getValidated())) {
                saveOkState();
            } else {
                saveBadState();
            }
            finish();
        });



        if (item != null
                && item.getValue() != null
                && (item.getValue().equals(NOT_OK.name()))) {
            edtComment.setText(item.getComment());
            showCommentSection(true);
        }
        showSelectedItem();
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (Boolean.TRUE.equals(item.getValidated())) {
            showCommentSection(true);
            btnOk.setVisibility(View.GONE);
            btnNotOk.setVisibility(View.GONE);
            btnPhoto.setVisibility(View.GONE);
            btnCanNotCheck.setVisibility(View.GONE);
            edtComment.addTextChangedListener(new ValidationTextWatcher(edtComment));
            btnSaveComment.setText("Сохранить");
            validateCommentText();
        }
    }

    private void saveOkState() {
        if (item != null) {
            model.modifyState(item, "OK");
            if (item.getParent() != null) {
                model.setCurrentNode(item.getParent());
            }
        }
    }

    public void startTOCameraActivity(ImageType type, long recipientId) {
        ArrayList<PrefixList> prefixes = new ArrayList<>();
        prefixes.add(new PrefixList("Tехнический осмотр", "TO"));
        Intent i = CamActivity.newBuilder()
                .setFolderName("TruckDoc")
                .setGalleryFolderName("Maintenance")
                .setImagePrefixList(prefixes)
                .setPrefixEnable(true)
                .setRecipient(recipientId)
                .setImageType(type)
                .setServiceCameraModeEnabled(true)
                .setDeleteButtonVisibility(false)
                .setSendButtonVisibility(false)
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
                .setTimeStampeEnable(true)
                .setLocationStampEnable(true)
                .build(this);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }

    private void highlightOK() {
        btnOk.setBackgroundColor(getResources().getColor(R.color.mnt_ok));
        btnNotOk.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
        btnCanNotCheck.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
        collapsingToolbar.setBackgroundColor(getResources().getColor(R.color.mnt_ok));
        titleImageView.setColorFilter(getResources().getColor(R.color.mnt_ok), PorterDuff.Mode.XOR);

    }

    private void highlightCanNotCheck() {
        btnNotOk.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
        btnCanNotCheck.setBackgroundColor(getResources().getColor(R.color.mnt_neutral));
        btnOk.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
        collapsingToolbar.setBackgroundColor(getResources().getColor(R.color.mnt_neutral));
        titleImageView.setColorFilter(getResources().getColor(R.color.mnt_neutral), PorterDuff.Mode.XOR);
    }

    private void highlightNotOk() {
        btnNotOk.setBackgroundColor(getResources().getColor(R.color.mnt_problem));
        btnCanNotCheck.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
        btnOk.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
        collapsingToolbar.setBackgroundColor(getResources().getColor(R.color.mnt_problem));
        titleImageView.setColorFilter(getResources().getColor(R.color.mnt_problem), PorterDuff.Mode.XOR);

    }

    private void showSelectedItem() {
        if (item.getValue() == null) {
            btnNotOk.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
            btnCanNotCheck.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
            btnOk.setBackgroundColor(getResources().getColor(R.color.mnt_group_title));
            collapsingToolbar.setBackgroundColor(getResources().getColor(R.color.mnt_neutral));
            titleImageView.setColorFilter(getResources().getColor(R.color.mnt_neutral), PorterDuff.Mode.XOR);
            return;
        }
        switch (item.getValue()) {
            case "OK":
                highlightOK();
                break;
            case "NOT_OK":
                highlightNotOk();
                break;
            case "UNDEFINED":
                highlightCanNotCheck();
                break;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_COMMENT, isComment != null && isComment);
    }

    @Override
    public void onBackPressed() {
        if (btns.getVisibility() == View.VISIBLE) {
            if (item.getParent() != null) {
                model.setCurrentNode(item.getParent());
            }
        }
        super.onBackPressed();
    }


    private String getNodeTitleText(ToNode item) {
        String titleText = item.getTitleText();
        return titleText == null ? getString(R.string.to_title) : titleText;
    }

    private void showCommentSection(boolean show) {
        isComment = show;
        commentSection.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void saveUndefinedState() {
        highlightCanNotCheck();
        if (item != null) {
            model.modifyState(model.getCurrentNode(), UNDEFINED.name());
            if (item.getParent() != null) {
                model.setCurrentNode(item.getParent());
            }
        }
    }

    private void saveBadState() {
        highlightNotOk();
        if (item != null) {
            model.modifyState(model.getCurrentNode(), NOT_OK.name());
        }
    }

    @Override
    protected void onDestroy() {
        activityRootView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        super.onDestroy();
    }

    public boolean isValidComment(String input) {
        Pattern PATTERN = Pattern.compile(item.getValidationRegExp());
        return PATTERN.matcher(input).matches();
    }

    private class ValidationTextWatcher implements TextWatcher {
        private View view;
        private ValidationTextWatcher(View view) {
            this.view = view;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            validateCommentText();
        }
    }

    private void validateCommentText() {
        if (item.getValidationRegExp() != null && !isValidComment(edtComment.getText().toString())) {
            TextView commentTitle = findViewById(R.id.comment_title);
            commentTitle.setText(item.getValidationMessage() != null ? item.getValidationMessage() : "Wrong field value. Contact help");
            commentTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.mnt_problem));
            Button save = findViewById(R.id.save_comment);
            save.setEnabled(false);
        } else {
            TextView commentTitle = findViewById(R.id.comment_title);
            commentTitle.setText("Введенное значение допустимо");
            commentTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.mnt_ok));
            Button save = findViewById(R.id.save_comment);
            save.setEnabled(true);
        }
    }
}
