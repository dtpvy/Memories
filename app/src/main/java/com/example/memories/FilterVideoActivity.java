package com.example.memories;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sherazkhilji.videffects.BlackAndWhiteEffect;
import com.sherazkhilji.videffects.DuotoneEffect;
import com.sherazkhilji.videffects.filter.GrainFilter;
import com.sherazkhilji.videffects.filter.NoEffectFilter;
import com.sherazkhilji.videffects.interfaces.ConvertResultListener;
import com.sherazkhilji.videffects.interfaces.Filter;
import com.sherazkhilji.videffects.model.Converter;
import com.sherazkhilji.videffects.view.VideoSurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class FilterVideoActivity extends AppCompatActivity {
    VideoSurfaceView mVideoView;
    Resources mResources;
    MediaPlayer mMediaPlayer = null;
    LinearLayout blackWhiteBtn, grainBtn, dueToneBtn;
    TextView cancelBtn, saveBtn;
    AssetFileDescriptor afd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_video);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        blackWhiteBtn = findViewById(R.id.blackwhite);
        grainBtn = findViewById(R.id.grain);
        dueToneBtn = findViewById(R.id.duetone);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);

        mResources = getResources();
        mMediaPlayer = new MediaPlayer();

        try {
            File myAssetFile = new File(url);
            afd = new AssetFileDescriptor(ParcelFileDescriptor.open(myAssetFile, ParcelFileDescriptor.MODE_READ_ONLY), 0L, myAssetFile.length());
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        mVideoView = (VideoSurfaceView) findViewById(R.id.mVideoSurfaceView);
        mVideoView.init(mMediaPlayer, new NoEffectFilter());

        blackWhiteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoView.init(mMediaPlayer, new BlackAndWhiteEffect());
            }
        });

        grainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GrainFilter grainFilter = new GrainFilter(10, 10);
                grainFilter.setIntensity(0.5f);
                mVideoView.init(mMediaPlayer, grainFilter);
            }
        });

        dueToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVideoView.init(mMediaPlayer, new DuotoneEffect(Color.BLUE, Color.YELLOW));
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = "/storage/emulated/0/Download/" + new Date().getTime() + ".mp4";
                AssetConverterThread assetConverterThread = new AssetConverterThread(new AssetsConverter(afd), mVideoView.getFilter(), path, new ConvertResultListener() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent();
                        intent.putExtra("url", path);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void onFail() {
                        System.out.println("not oke");
                    }
                });

                assetConverterThread.start();
            }
        });
    }

    private class AssetConverterThread extends Thread {
        AssetsConverter assetsConverter;
        Filter filter;
        String outPath;
        ConvertResultListener listener;

        public AssetConverterThread(AssetsConverter assetsConverter, Filter filter, String outPath, ConvertResultListener listener) {
            this.assetsConverter = assetsConverter;
            this.filter = filter;
            this.outPath = outPath;
            this.listener = listener;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("run");
            assetsConverter.startConverter(filter, outPath, listener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
    }
}