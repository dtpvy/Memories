package com.example.memories;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.memories.OnK4LVideoListener;
import com.example.memories.R;

import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

public class TrimVideoActivity extends AppCompatActivity implements OnTrimVideoListener, OnK4LVideoListener {

    private K4LVideoTrimmer mVideoTrimmer;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_video);

        Intent intent = getIntent();
        String path = intent.getStringExtra("url");

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("message");

        mVideoTrimmer = ((K4LVideoTrimmer) findViewById(R.id.trim_video_view_trim_video_ac));
        if (mVideoTrimmer != null) {
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setDestinationPath("/storage/emulated/0/Download/");
            mVideoTrimmer.setVideoURI(Uri.parse(path));
        }
    }

    @Override
    public void onTrimStarted() {
        mProgressDialog.show();
    }

    @Override
    public void getResult(final Uri uri) {
        mProgressDialog.cancel();
        Intent intent = new Intent();
        intent.putExtra("url",uri.toString());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        mVideoTrimmer.destroy();
        finish();
    }

    @Override
    public void onError(final String message) {
        mProgressDialog.cancel();
    }

    @Override
    public void onVideoPrepared() {}
}