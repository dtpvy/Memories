package com.example.memories;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Locale;

public class PhotoInfoActivity extends AppCompatActivity {
    ImageButton backBtn;
    TextView createdAt, info, name, deletedAtText, syncText, url;
    LinearLayout delete, sync;
    String mediaId;
    Media media;
    CollectionReference dbMedia, dbHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_info);

        Intent intent = getIntent();
        mediaId = intent.getStringExtra("media_id");

        Database database = new Database();
        dbMedia = database.getDbMedia();
        dbHistory = database.getDbHistories();

        backBtn = findViewById(R.id.backBtn);
        createdAt = findViewById(R.id.createdAt);
        info = findViewById(R.id.info);
        name = findViewById(R.id.name);
        deletedAtText = findViewById(R.id.deletedAtText);
        syncText = findViewById(R.id.syncAtText);
        url = findViewById(R.id.url);

        delete = findViewById(R.id.deletedAt);
        sync = findViewById(R.id.syncAt);
        delete.setVisibility(View.INVISIBLE);
        sync.setVisibility(View.INVISIBLE);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        loadData();
    }

    public void loadData() {
        dbMedia.document(mediaId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                media = documentSnapshot.toObject(Media.class);

                if (media.getHistoryId() != null) {
                    loadHistory(media.getHistoryId());
                }

                Locale locale = new Locale("vi", "VN");
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
                String date = dateFormat.format(media.getCreatedAt());
                createdAt.setText(date);

                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(media.getImgUrl());
                name.setText(storageReference.getName());
                url.setText(media.getImgUrl());

                storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        long size = storageMetadata.getSizeBytes();
                        info.setText("Size: " + size + " bytes  Type: " + storageMetadata.getContentType() );
                    }
                });

                if (media.getDeletedAt() != null) {
                    delete.setVisibility(View.VISIBLE);
                    deletedAtText.setText(dateFormat.format(media.getDeletedAt()));
                }
            }
        });
    }

    public void loadHistory(String historyId) {
        dbHistory.document(historyId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                History history = documentSnapshot.toObject(History.class);
                sync.setVisibility(View.VISIBLE);
                Locale locale = new Locale("vi", "VN");
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
                String date = dateFormat.format(history.getDate());
                syncText.setText(date);
            }
        });
    }
}