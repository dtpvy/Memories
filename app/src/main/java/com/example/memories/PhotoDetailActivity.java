package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.StructuredQuery;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PhotoDetailActivity extends AppCompatActivity {
    String mediaId, albumId;
    CollectionReference dbMedia, dbAlbum;
    Media media, savedMedia;
    ImageButton backBtn, addBtn;
    LinearLayout shareBtn, editBtn, likeBtn, removeBtn, moreBtn, restoreBtn, deleteBtn, photoControl, trashControl;
    TextView nameText, timeText;
    ImageView photoView, heart;
    WallpaperManager wallpaperManager;
    Album album;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        user = new User().getUser(this);

        Intent intent = getIntent();
        mediaId = intent.getStringExtra("media_id");
        albumId = intent.getStringExtra("album_id");
        Database db = new Database();
        dbMedia = db.getDbMedia();
        dbAlbum = db.getDbAlbum();

        shareBtn = findViewById(R.id.shareBtn);
        editBtn = findViewById(R.id.editBtn);
        likeBtn = findViewById(R.id.likeBtn);
        removeBtn = findViewById(R.id.removeBtn);
        moreBtn = findViewById(R.id.moreBtn);
        backBtn = findViewById(R.id.backBtn);
        addBtn = findViewById(R.id.addBtn);
        nameText = findViewById(R.id.nameText);
        timeText = findViewById(R.id.timeText);
        photoView = findViewById(R.id.photo);
        heart = findViewById(R.id.heart);
        photoControl = findViewById(R.id.photoControl);
        trashControl = findViewById(R.id.trashControl);
        restoreBtn = findViewById(R.id.restore);
        deleteBtn = findViewById(R.id.deleteBtn);

        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

        loadData();
        if (albumId != null &&  albumId.contains("trash")) {
            loadAlbum();
            photoControl.setVisibility(View.GONE);
            trashControl.setVisibility(View.VISIBLE);
        } else {
            trashControl.setVisibility(View.GONE);
            photoControl.setVisibility(View.VISIBLE);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PhotoDetailActivity.this, SelectAlbumActivity.class);
                Gson gson = new Gson();
                ArrayList<Media> arrayList = new ArrayList<>();
                arrayList.add(media);
                String intentData = gson.toJson(arrayList);
                intent.putExtra("photos", intentData);
                if (albumId != null && albumId.equals("default") == false) {
                    intent.putExtra("album_id", albumId);
                }
                PhotoDetailActivity.this.startActivity(intent);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PhotoDetailActivity.this, PhotoEditorActivity.class);
                intent.putExtra("media_id", mediaId);
                intent.putExtra("album_id", albumId);
                PhotoDetailActivity.this.startActivity(intent);
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
                onCreateDialog();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoView.setDrawingCacheEnabled(true);
                Uri imageUri= Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), photoView.getDrawingCache(), media.getId(), null));
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("image/*");
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

                Glide.with(photoView).load(media.getImgUrl()).placeholder(R.drawable.stockphoto).into(photoView);
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

    public void onCreateDialog() {
        MoreSheetFragment bottomSheetFragment = new MoreSheetFragment();
        bottomSheetFragment.setCallback(new MoreSheetFragment.Callback() {
            @Override
            public void setWrapper() {
                BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                try {
                    wallpaperManager.setBitmap(bitmap);
                    Toast.makeText(PhotoDetailActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(PhotoDetailActivity.this, "Có lỗi xảy ra vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
                bottomSheetFragment.dismiss();
            }

            @Override
            public void seeInformation() {
                Intent intent = new Intent(PhotoDetailActivity.this, PhotoInfoActivity.class);
                intent.putExtra("media_id", media.getId());
                PhotoDetailActivity.this.startActivity(intent);
                bottomSheetFragment.dismiss();
            }
        });
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

}