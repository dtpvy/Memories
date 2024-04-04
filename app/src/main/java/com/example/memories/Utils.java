package com.example.memories;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class Utils {
    public String convertMillieToHMmSs(long millie) {
        long seconds = (millie / 1000);
        long second = seconds % 60;
        long minute = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;

        String result = "";
        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        }
        else {
            return String.format("%02d:%02d" , minute, second);
        }
    }

    public int getVideoDuration(String videoPath, Context context) {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(videoPath));
            int duration = mediaPlayer.getDuration();
            mediaPlayer.release();
            return duration;
        } catch (Exception e) {
            return 0;
        }
    }
}
