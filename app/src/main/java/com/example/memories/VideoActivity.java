package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class VideoActivity extends AppCompatActivity {
    VideoView videoView;
    String mediaId, albumId;
    CollectionReference dbMedia, dbAlbum;
    Media media, savedMedia;
    ImageButton backBtn, addBtn;
    LinearLayout shareBtn, editBtn, likeBtn, removeBtn, moreBtn, restoreBtn, deleteBtn, photoControl, trashControl;
    TextView nameText, timeText;
    ImageView heart;
    Album album;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent intent = getIntent();
        mediaId = intent.getStringExtra("media_id");
        albumId = intent.getStringExtra("album_id");

        user = new User().getUser(this);

        Database database = new Database();
        dbMedia = database.getDbMedia();
        dbAlbum = database.getDbAlbum();

        shareBtn = findViewById(R.id.shareBtn);
        editBtn = findViewById(R.id.editBtn);
        likeBtn = findViewById(R.id.likeBtn);
        removeBtn = findViewById(R.id.removeBtn);
        moreBtn = findViewById(R.id.moreBtn);
        backBtn = findViewById(R.id.backBtn);
        addBtn = findViewById(R.id.addBtn);
        nameText = findViewById(R.id.nameText);
        timeText = findViewById(R.id.timeText);
        heart = findViewById(R.id.heart);
        photoControl = findViewById(R.id.photoControl);
        trashControl = findViewById(R.id.trashControl);
        restoreBtn = findViewById(R.id.restore);
        deleteBtn = findViewById(R.id.deleteBtn);
        videoView = findViewById(R.id.video);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VideoActivity.this, SelectAlbumActivity.class);
                Gson gson = new Gson();
                ArrayList<Media> arrayList = new ArrayList<>();
                arrayList.add(media);
                String intentData = gson.toJson(arrayList);
                intent.putExtra("photos", intentData);
                if (albumId != null && albumId.equals("default") == false) {
                    intent.putExtra("album_id", albumId);
                }
                VideoActivity.this.startActivity(intent);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VideoActivity.this, VideoEditorActivity.class);
                intent.putExtra("media_id", mediaId);
                intent.putExtra("album_id", albumId);
                VideoActivity.this.startActivity(intent);
                if (videoView != null){
                    videoView.stopPlayback();
                    videoView.setVideoURI(null);
                }
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (albumId.contains("default")) {
                    Date deletedAt = new Date();
                    dbMedia.document(media.getId()).update("deletedAt", deletedAt);
                    dbAlbum.whereArrayContains("photos", media).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                                    Album album = queryDocumentSnapshot.toObject(Album.class);
                                    for (Media media1 : album.getPhotos()) {
                                        if (media1.getId().contains(media.getId())) {
                                            media1.setDeletedAt(deletedAt);
                                            break;
                                        }
                                    }
                                    dbAlbum.document(album.getId()).set(album);
                                }
                                finish();
                            }
                        }
                    });
                } else {
                    dbAlbum.document(albumId).update("photos", FieldValue.arrayRemove(media));
                    finish();
                }
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VideoActivity.this, PhotoInfoActivity.class);
                intent.putExtra("media_id", media.getId());
                VideoActivity.this.startActivity(intent);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentValues content = new ContentValues(4);
                content.put(MediaStore.Video.VideoColumns.DATE_ADDED,
                        System.currentTimeMillis() / 1000);
                content.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                content.put(MediaStore.Video.Media.DATA, media.getImgUrl());

                ContentResolver resolver = getApplicationContext().getContentResolver();
                Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, content);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("video/*");
                startActivity(Intent.createChooser(shareIntent, null));
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (savedMedia != null) {
                    dbAlbum.document(user.getFavouriteAlbum().getId()).update("photos", FieldValue.arrayRemove(savedMedia)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            heart.setImageResource(R.drawable.heart);
                            savedMedia = null;
                        }
                    });
                } else {
                    dbAlbum.document(user.getFavouriteAlbum().getId()).update("photos", FieldValue.arrayUnion(media)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            savedMedia = media;
                            heart.setImageResource(R.drawable.heartbold);
                        }
                    });
                }
            }
        });
        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbMedia.document(mediaId).update("deletedAt", null);
                finish();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveDialog();
            }
        });

        loadData();
        if (albumId != null && albumId.contains("trash")) {
            loadAlbum();
            photoControl.setVisibility(View.GONE);
            trashControl.setVisibility(View.VISIBLE);
        } else {
            trashControl.setVisibility(View.GONE);
            photoControl.setVisibility(View.VISIBLE);
        }
    }

    public void loadData() {
        dbMedia.document(mediaId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                media = documentSnapshot.toObject(Media.class);
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(media.getImgUrl());
                nameText.setText(storageReference.getName());

                Locale locale = new Locale("vi", "VN");
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                String date = dateFormat.format(media.getCreatedAt());
                timeText.setText(date);

                showVideo(Uri.parse(media.getImgUrl()));
            }
        });
    }

    public void loadAlbum() {
        dbAlbum.document(user.getFavouriteAlbum().getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                album = documentSnapshot.toObject(Album.class);
                for (Media media1 : album.getPhotos()) {
                    if (media1.getId().contains(mediaId)) {
                        savedMedia = media1;
                        break;
                    }
                }
                if (savedMedia != null) {
                    heart.setImageResource(R.drawable.heartbold);
                } else {
                    heart.setImageResource(R.drawable.heart);
                }
            }
        });
    }

    public void onRemoveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Xác nhận xoá?");
        builder.setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removePhoto();
            }
        });
        builder.setNegativeButton("Huỷ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    public void removePhoto() {
        dbMedia.document(mediaId).delete();
        dbAlbum.whereArrayContains("photos", media).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        Album album = queryDocumentSnapshot.toObject(Album.class);
                        for (Media media1 : album.getPhotos()) {
                            if (media1.getId().contains(media.getId())) {
                                dbAlbum.document(album.getId()).update("photos", FieldValue.arrayRemove(media1));
                                break;
                            }
                        }
                    }
                    finish();
                }
            }
        });
    }

    void showVideo (Uri trimVideo){
        videoView.setVideoURI(trimVideo);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        MediaController mediaController = new MediaController(VideoActivity.this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (videoView != null){
            videoView.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoView != null){
            videoView.stopPlayback();
            videoView.setVideoURI(null);
        }
    }
}