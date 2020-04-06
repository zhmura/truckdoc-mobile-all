package com.sanda.truckdoc.client.ui.floating;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sanda.truckdoc.client.R;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * This implementation provides multiple windows. You may extend this class or
 * use it as a reference for a basic foundation for your own windows.
 * <p>
 * <p>
 * Functionality includes system window decorators, moveable, resizeable,
 * hideable, closeable, and bring-to-frontable.
 * <p>
 * <p>
 * The persistent notification creates new windows. The hidden notifications
 * restores previously hidden windows.
 *
 * @author Mark Wei <markwei@gmail.com>
 */
public class HelpWindow extends StandOutWindow {

    private static final String MESSAGE = "message";

    public static void start(final Context context, String message) {
        Intent i = StandOutWindow.getShowIntent(context, HelpWindow.class, StandOutWindow.DEFAULT_ID);
        i.putExtra(MESSAGE, message);
        context.startService(i);
        Toast.makeText(context, R.string.floating_window_help_message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getAppName() {
        return getString(R.string.floating_window_name);
    }

    @Override
    public int getAppIcon() {
        return R.drawable.icon;
    }

    @Override
    public String getTitle(int id) {
        return getAppName() + " " + id;
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        Intent intent = getIntent();
        String message = intent.getStringExtra(MESSAGE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.widget_floating_window, frame, true);

        TextView text = view.findViewById(R.id.textView);
        text.setText(message);
    }

    // every window is initially same size
    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        int width = getResources().getDimensionPixelSize(R.dimen.floating_window_width);
        int height = getResources().getDimensionPixelSize(R.dimen.floating_window_height);
        return new StandOutLayoutParams(id, width, height, StandOutLayoutParams.RIGHT, StandOutLayoutParams.BOTTOM);
    }

    // we want the system window decorations, we want to drag the body, we want
    // the ability to hide windows, and we want to tap the window to bring to
    // front
    @Override
    public int getFlags(int id) {
        return StandOutFlags.FLAG_DECORATION_SYSTEM | StandOutFlags.FLAG_BODY_MOVE_ENABLE |
                StandOutFlags.FLAG_WINDOW_HIDE_ENABLE | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP |
                StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE | StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;
    }

    @Override
    public String getPersistentNotificationTitle(int id) {
        return getString(R.string.floating_window_persistent_title);
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return getString(R.string.floating_window_persistent_message);
    }

    // return an Intent that creates a new MultiWindow
    @Override
    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, getClass(), id);
    }

    @Override
    public int getHiddenIcon() {
        return android.R.drawable.ic_menu_info_details;
    }

    @Override
    public String getHiddenNotificationTitle(int id) {
        return getString(R.string.floating_window_hidden_title);
    }

    @Override
    public String getHiddenNotificationMessage(int id) {
        return getString(R.string.floating_window_hidden_message);
    }

    // return an Intent that restores the MultiWindow
    @Override
    public Intent getHiddenNotificationIntent(int id) {
        return StandOutWindow.getShowIntent(this, getClass(), id);
    }

    @Override
    public Animation getShowAnimation(int id) {
        if (isExistingId(id)) {
            // restore
            return AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        } else {
            // show
            return super.getShowAnimation(id);
        }
    }

    @Override
    public Animation getHideAnimation(int id) {
        return AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
    }

//    @Override
//    public List<DropDownListItem> getDropDownItems(int id) {
//        List<DropDownListItem> items = new ArrayList<DropDownListItem>();
//        items.add(new DropDownListItem(android.R.drawable.ic_menu_help, "About", new Runnable() {
//
//            @Override
//            public void run() {
//                Toast.makeText(MultiWindow.this, getAppName() + " is a demonstration of StandOut.", Toast.LENGTH_SHORT).show();
//            }
//        }));
//        items.add(new DropDownListItem(android.R.drawable.ic_menu_preferences, "Settings", new Runnable() {
//
//            @Override
//            public void run() {
//                Toast.makeText(MultiWindow.this, "There are no settings.", Toast.LENGTH_SHORT).show();
//            }
//        }));
//        return items;
//    }
}
