package com.sanda.truckdoc.client.ui.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.sanda.truckdoc.client.R;

/**
 * Created by k.natallie on 08.08.2016.
 */

public class SoundUtils {

    public static void soundNotification(Context context, boolean isError) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);
        MediaPlayer mPlay = MediaPlayer.create(context, isError ? R.raw.error_tone : R.raw.new_message_tone);
        mPlay.setLooping(false);
        mPlay.start();
    }
}
