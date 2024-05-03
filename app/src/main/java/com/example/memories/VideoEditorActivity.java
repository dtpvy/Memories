package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideoEditorActivity extends AppCompatActivity {
    VideoView videoView;
    PhotoEditor photoEditor;
    CollectionReference dbMedia, dbAlbum;
    String mediaId, albumId;
    Media media;
    FirebaseStorage storage;
    User user;
    TextView cancelBtn, saveBtn;
    LinearLayout cutBtn, lightBtn, emojiBtn, textBtn, effectBtn;
    ArrayList<Uri> files = new ArrayList<>();
    File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);

        user = new User().getUser(this);

        Intent intent = getIntent();
        mediaId = intent.getStringExtra("media_id");
        albumId = intent.getStringExtra("album_id");

        Database db = new Database();
        dbMedia = db.getDbMedia();
        dbAlbum = db.getDbAlbum();

        storage = FirebaseStorage.getInstance();

        videoView = findViewById(R.id.video);

        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        cutBtn = findViewById(R.id.cutBtn);
        effectBtn = findViewById(R.id.effectBtn);

        cutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trimVideo();
            }
        });

        effectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectVideo();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (Uri uri: files) {
                    File file = new File(uri.getPath());
                    if (file.exists()) {
                        if (file.delete()) {
                            System.out.println("file Deleted :" + uri.getPath());
                        } else {
                            System.out.println("file not Deleted :" + uri.getPath());
                        }
                    }
                }
                files.clear();
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (files.size() == 0) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();

                    Media newMedia = new Media(user.getId(), new Date(), media.getType());
                    String childPath = user.getId() + "/" + newMedia.getId() + ".mp4";
                    StorageReference videoRef = storageRef.child(childPath);

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(media.getImgUrl()).build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                byte[] videoBytes = response.body().bytes();
                                UploadTask uploadTask = videoRef.putBytes(videoBytes);
                                uploadTask.addOnSuccessListener(taskSnapshot -> {
                                    videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        newMedia.setImgUrl(uri.toString());
                                        dbMedia.document(newMedia.getId()).set(newMedia);
                                        dbAlbum.document(albumId).update("photos", FieldValue.arrayUnion(newMedia));
                                        Intent intent = new Intent(VideoEditorActivity.this, VideoActivity.class);
                                        intent.putExtra("media_id", newMedia.getId());
                                        intent.putExtra("album_id", albumId);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                        VideoEditorActivity.this.startActivity(intent);
                                        VideoEditorActivity.this.finish();
                                        Toast.makeText(VideoEditorActivity.this, "Save successfully!", Toast.LENGTH_LONG);
                                    });
                                }).addOnFailureListener(e -> {});
                            } else {
                                throw new IOException("Failed to download file: " + response);
                            }
                        }
                    });
                } else {
                    try {
                        uploadFile(files.get(files.size()-1));
                    } catch (FileNotFoundException e) {}
                }
            }
        });

        loadData();
    }

    public void loadData() {
        dbMedia.document(mediaId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                media = documentSnapshot.toObject(Media.class);
                showVideo(Uri.parse(media.getImgUrl()));
            }
        });

        if (albumId.contains("default")) albumId = user.getDefaultAlbum().getId();
    }

    public String getMimeType(Uri uri) {
        ContentResolver cR = getContentResolver();
        String mime = cR.getType(uri);
        return mime;
    }

    public void uploadFile(Uri uri) throws FileNotFoundException {
        StorageReference storageRef = storage.getReference();

        UUID uuid = UUID.randomUUID();
        String childPath = user.getId() + "/" + uuid.toString() + ".mp4";
        StorageReference mountainsRef = storageRef.child(childPath);

        InputStream stream = getContentResolver().openInputStream(uri);
        UploadTask uploadTask = mountainsRef.putStream(stream);

        Media newMedia = new Media(user.getId(), new Date(), getMimeType(uri));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri firebaseUri) {
                        newMedia.setImgUrl(firebaseUri.toString());
                        dbMedia.document(newMedia.getId()).set(newMedia);
                        dbAlbum.document(albumId).update("photos", FieldValue.arrayUnion(newMedia));
                        Intent intent = new Intent(VideoEditorActivity.this, VideoActivity.class);
                        intent.putExtra("media_id", newMedia.getId());
                        intent.putExtra("album_id", albumId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                        VideoEditorActivity.this.startActivity(intent);
                        VideoEditorActivity.this.finish();
                        Toast.makeText(VideoEditorActivity.this, "Save successfully!", Toast.LENGTH_LONG);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    public void effectVideo() {
        if (files.size() > 0) {
            Intent intent = new Intent(VideoEditorActivity.this, FilterVideoActivity.class);
            intent.putExtra("url", files.get(files.size()-1).getPath());
            VideoEditorActivity.this.startActivityForResult(intent, 0);
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference videoRef = storage.getReferenceFromUrl(media.getImgUrl());

        final File localFile;
        try {
            localFile = File.createTempFile("videos", "mp4");
            videoRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                // File downloaded successfully
                Intent intent = new Intent(VideoEditorActivity.this, FilterVideoActivity.class);
                intent.putExtra("url", localFile.getPath());
                VideoEditorActivity.this.startActivityForResult(intent, 0);
            }).addOnFailureListener(e -> {
                // Handle any errors
            });
        } catch (IOException e) {}
    }

    public void trimVideo() {
        if (files.size() > 0) {
            Intent intent = new Intent(VideoEditorActivity.this, TrimVideoActivity.class);
            intent.putExtra("url", files.get(files.size()-1).getPath());
            VideoEditorActivity.this.startActivityForResult(intent, 0);
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference videoRef = storage.getReferenceFromUrl(media.getImgUrl());

        final File localFile;
        try {
            localFile = File.createTempFile("videos", "mp4");
            videoRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                // File downloaded successfully
                Intent intent = new Intent(VideoEditorActivity.this, TrimVideoActivity.class);
                intent.putExtra("url", localFile.getPath());
                VideoEditorActivity.this.startActivityForResult(intent, 0);
            }).addOnFailureListener(e -> {
                // Handle any errors
            });
        } catch (IOException e) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            Uri uri = Uri.parse(data.getStringExtra("url"));
            files.add(uri);
            showVideo(uri);
        }
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
        MediaController mediaController = new MediaController(VideoEditorActivity.this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(videoView != null){
            videoView.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Uri uri: files) {
            File file = new File(uri.getPath());
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("file Deleted :" + uri.getPath());
                } else {
                    System.out.println("file not Deleted :" + uri.getPath());
                }
            }
        }
    }
}