package com.sanda.truckdoc.client.ui.floating;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sanda.truckdoc.client.R;

import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * Created by astra on 21.10.2015.
 */
public class ApnHelpWindow extends HelpWindow {

    public static final String CONFIRM = "CONFIRM";

    @BindView(R.id.pager)
    ViewPager pager;
    private WizardPagerAdapter adapter;

    public static void start(final Context context) {
        Intent i = StandOutWindow.getShowIntent(context, ApnHelpWindow.class, StandOutWindow.DEFAULT_ID);
        context.startService(i);
        Toast.makeText(context, R.string.floating_window_help_message, Toast.LENGTH_SHORT).show();
    }

    public static void startConfirmation(final Context context) {
        Intent i = StandOutWindow.getShowIntent(context, ApnHelpWindow.class, StandOutWindow.DEFAULT_ID);
        i.putExtra(CONFIRM, true);
        context.startService(i);
        Toast.makeText(context, R.string.floating_window_help_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        View view = LayoutInflater.from(this).inflate(R.layout.widget_floating_wizard, frame, true);
        adapter = new WizardPagerAdapter();
        ButterKnife.bind(this, view);
        pager.setAdapter(adapter);
        pager.setTag(id);
    }

    @OnClick(R.id.apn_page1_btn_yes)
    void goToApnDataPage() {
        pager.setCurrentItem(adapter.page(R.id.apn_page2));
    }

    @OnClick(R.id.apn_page1_btn_no)
    void askUserToSetElisaApn() {
        pager.setCurrentItem(adapter.page(R.id.apn_page4));
        reload();
        ApnWaiterReceiver.startApnWaiterReceiver(this);
    }

    @OnClick(R.id.apn_page2_btn_yes)
    void dataApnConfirmed() {
        Toast.makeText(this, "Sms send", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.apn_page2_btn_no)
    void askUserToSetApn() {
        pager.setCurrentItem(adapter.page(R.id.apn_page3));
        reload();
        ApnWaiterReceiver.startApnWaiterReceiver(this);
    }

    @OnClick(R.id.apn_page_confirmation_btn_yes)
    void confirmRestart() {
        pager.setCurrentItem(adapter.page(R.id.apn_page_restart));
        reload();
    }

    @OnClick(R.id.apn_page_confirmation_btn_no)
    void startFromBeginning() {
        pager.setCurrentItem(adapter.page(R.id.apn_page1));
        reload();
    }

    private void reload() {
        int id = (Integer) pager.getTag();
        if (getWindow(id) != null) {
            bringToFront(id);
        }
    }

    @Override
    public boolean onShow(int id, Window window) {
        Intent intent = getIntent();
        boolean confirm = intent.getBooleanExtra(CONFIRM, false);
        if (confirm) {
            pager.setCurrentItem(adapter.page(R.id.apn_page_confirmation));
            reload();
        }
        return false;
    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        int width = getResources().getDimensionPixelSize(R.dimen.floating_window_width);
        int height = getResources().getDimensionPixelSize(R.dimen.floating_wizard_height);
        return new StandOutLayoutParams(id, width, height, StandOutLayoutParams.RIGHT, StandOutLayoutParams.BOTTOM);
    }

    // we want the system window decorations, we want to drag the body, we want
    // the ability to hide windows, and we want to tap the window to bring to
    // front
    @Override
    public int getFlags(int id) {
        return StandOutFlags.FLAG_DECORATION_SYSTEM | StandOutFlags.FLAG_BODY_MOVE_ENABLE |
                StandOutFlags.FLAG_WINDOW_HIDE_ENABLE | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE |
                StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;
    }
}
