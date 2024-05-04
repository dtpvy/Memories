package com.example.memories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.homesoft.encoder.Mp4FrameMuxer;
import com.homesoft.encoder.Muxer;
import com.homesoft.encoder.MuxerConfig;
import com.homesoft.encoder.MuxingCompletionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PhotoActivity extends AppCompatActivity {
    ArrayList<PhotoList> photoLists = new ArrayList<>();
    ArrayList<Media> selected = new ArrayList<>();
    Album album;
    ImageView backBtn, selectAllBtn, moreBtn, editBtn;
    TextView albumName, chooseText;
    User user;
    ListView listView;
    ConstraintLayout photoControl;
    LinearLayout addBtn, trashBtn, downBtn, restoreBtn, deleteBtn, shareBtn, creativeBtn;
    Boolean isEdit = false;
    PhotoListAdapter photoListAdapter;
    String albumId, password = "";
    ImageAction imageAction;
    CollectionReference dbAlbum, dbMedia, dbUser;
    Boolean isAsc = true;
    String[] ratio = {"854 : 480", "1024 : 576", "1280 : 720", "800 : 600", "600 : 600"};
    int[] widths = {854, 1024, 1280, 800, 600};
    int[] heights = {480, 576, 720, 600, 600};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        imageAction = new ImageAction(this);

        Database db = new Database();
        dbAlbum = db.getDbAlbum();
        dbMedia = db.getDbMedia();
        dbUser = db.getDbUser();

        user = new User().getUser(this);
        Intent intent = getIntent();
        albumId = intent.getStringExtra("album_id");

        albumName = findViewById(R.id.albumName);
        listView = findViewById(R.id.photoList);
        chooseText = findViewById(R.id.chooseText);
        shareBtn = findViewById(R.id.sharePhoto);
        editBtn = findViewById(R.id.editPassword);
        creativeBtn = findViewById(R.id.creativeBtn);

        if (user.getPrivateAlbum().getId().compareTo(albumId) == 0) {
            editBtn.setVisibility(View.VISIBLE);
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPassword();
                }
            });
        }

        if (albumId.compareTo("trash") != 0) {
            photoControl = findViewById(R.id.photoControl);
        } else {
            photoControl = findViewById(R.id.deleteControl);
        }

        selectAllBtn = findViewById(R.id.selectAllBtn);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEdit) {
                    onChangeMode(false);
                } else finish();
            }
        });

        selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoListAdapter.onSelectAll();
            }
        });

        addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected.size() == 0) return;
                Intent intent = new Intent(PhotoActivity.this, SelectAlbumActivity.class);
                Gson gson = new Gson();
                String intentData = gson.toJson(selected);
                intent.putExtra("photos", intentData);
                intent.putExtra("album_id", albumId);
                PhotoActivity.this.startActivity(intent);
                onChangeMode(false);
            }
        });

        trashBtn = findViewById(R.id.removePhoto);
        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePhoto();
                onChangeMode(false);
            }
        });

        restoreBtn = findViewById(R.id.restoreBtn);
        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trashPhoto(true);
                onChangeMode(false);
            }
        });

        deleteBtn = findViewById(R.id.deletePhoto);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trashPhoto(false);
                onChangeMode(false);
            }
        });

        downBtn = findViewById(R.id.downloadBtn);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadImage();
                onChangeMode(false);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Uri> imageUris = new ArrayList<>();
                for (Media media: selected) {
                    Glide.with(getApplicationContext()).asBitmap().load(media.getImgUrl()).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Uri imageUri= Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), resource, media.getId(), null));
                            imageUris.add(imageUri);
                            if (imageUris.size() == selected.size()) {
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, "Share images");
                                shareIntent.setType("image/*");
                                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                                startActivity(Intent.createChooser(shareIntent, null));
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });


                }
            }
        });

        moreBtn = findViewById(R.id.moreBtn);
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSort();
            }
        });

        creativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateDialog();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        selected = new ArrayList<>();
        if (albumId.compareTo("trash") != 0) loadData(albumId);
        else loadTrash();
    }

    public void onChangeMode(Boolean isEdit) {
        if (!isEdit) {
            photoControl.setVisibility(View.INVISIBLE);
            selectAllBtn.setVisibility(View.INVISIBLE);
            backBtn.setImageResource(R.drawable.back);
        } else {
            photoControl.setVisibility(View.VISIBLE);
            selectAllBtn.setVisibility(View.VISIBLE);
            backBtn.setImageResource(R.drawable.x);
        }
        this.isEdit = isEdit;
        photoListAdapter.setIsEdit(isEdit);
    }

    public void loadData(String albumId) {
        photoLists = new ArrayList<>();
        Map<Date, ArrayList<Media>> photos = new HashMap<>();;
        dbAlbum.document(albumId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                album = documentSnapshot.toObject(Album.class);
                albumName.setText(album.getName());
                for (int i = 0; i < album.getPhotos().size(); i++) {
                    Media media = album.getPhotos().get(i);
                    if (media.getDeletedAt() != null) continue;
                    Date date = getDate(media.getCreatedAt());
                    if (photos.get(date) == null) {
                        ArrayList<Media> arr = new ArrayList<>();
                        arr.add(media);
                        photos.put(date, arr);
                    } else {
                        ArrayList<Media> arr = photos.get(date);
                        arr.add(media);
                    }
                }
                List<Date> dates = new ArrayList<>(photos.keySet());
                Collections.sort(dates);
                for (int i = 0; i < dates.size(); i++) {
                    photoLists.add(new PhotoList(dates.get(i), album.getId(), photos.get(dates.get(i))));
                }

                photoListAdapter = new PhotoListAdapter(PhotoActivity.this, photoLists, albumId);
                photoListAdapter.setCallback(new PhotoListAdapter.Callback() {
                    @Override
                    public void onLongClick() {
                        onChangeMode(true);
                    }
                    public void onChange(ArrayList<Media> media) {
                        if (media.size() > 0) chooseText.setText("Đã chọn " + media.size());
                        else chooseText.setText("");
                        selected = media;
                    }
                });
                listView.setAdapter(photoListAdapter);
            }
        });
    }

    public void loadTrash() {
        photoLists = new ArrayList<>();
        Map<Date, ArrayList<Media>> photos = new HashMap<>();;
        albumName.setText("Thùng rác");
        dbMedia.whereNotEqualTo("deletedAt", null).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        Media media = queryDocumentSnapshot.toObject(Media.class);
                        Date date = getDate(media.getCreatedAt());
                        if (photos.get(date) == null) {
                            ArrayList<Media> arr = new ArrayList<>();
                            arr.add(media);
                            photos.put(date, arr);
                        } else {
                            ArrayList<Media> arr = photos.get(date);
                            arr.add(media);
                        }
                    }
                    List<Date> dates = new ArrayList<>(photos.keySet());
                    Collections.sort(dates);
                    for (int i = 0; i < dates.size(); i++) {
                        photoLists.add(new PhotoList(dates.get(i), photos.get(dates.get(i))));
                    }

                    photoListAdapter = new PhotoListAdapter(PhotoActivity.this, photoLists, albumId);
                    photoListAdapter.setCallback(new PhotoListAdapter.Callback() {
                        @Override
                        public void onLongClick() {
                            onChangeMode(true);
                        }
                        public void onChange(ArrayList<Media> media) {
                            if (media.size() > 0) chooseText.setText("Đã chọn " + media.size());
                            else chooseText.setText("");
                            selected = media;
                        }
                    });
                    listView.setAdapter(photoListAdapter);
                }
            }
        });
    }

    public Date getDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public void removePhoto() {
        Date deletedAt = new Date();
        ArrayList<Media> aMedia = album.getPhotos();
        for (Media media : selected) {
            int position = aMedia.indexOf(media);
            System.out.println(position);
            aMedia.get(position).setDeletedAt(deletedAt);
            dbMedia.document(media.getId()).update("deletedAt", deletedAt);
        }
        dbAlbum.document(album.getId()).set(album);
        loadData(albumId);
        Toast.makeText(PhotoActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
    }

    public void trashPhoto(Boolean isRestore) {
        for (int i = 0; i < selected.size(); i++) {
            Media media = selected.get(i);
            Boolean end = i+1 == selected.size();
            dbAlbum.whereEqualTo("userId", user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                            Album album = queryDocumentSnapshot.toObject(Album.class);
                            Optional<Media> ph = album.getPhotos().stream().filter(p -> p.getId().compareTo(media.getId()) == 0).findFirst();
                            if (!ph.isPresent()) continue;
                            int position = album.getPhotos().indexOf(ph.get());
                            if (isRestore) {
                                album.getPhotos().get(position).setDeletedAt(null);
                                dbAlbum.document(album.getId()).set(album);
                                dbMedia.document(media.getId()).update("deletedAt", null);
                            } else {
                                album.getPhotos().remove(position);
                                dbAlbum.document(album.getId()).set(album);
                                dbMedia.document(media.getId()).delete();
                            }

                            if (end) {
                                loadTrash();
                                Toast.makeText(PhotoActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        }
    }

    public void downloadImage() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < selected.size(); i++) {
                            try {
                                imageAction.downloadImage(selected.get(i).getImgUrl());
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
        ).start();
        Toast.makeText(PhotoActivity.this, "Tải xuống thành công", Toast.LENGTH_SHORT).show();
    }

    public void showSort() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PhotoActivity.this);
        builder.setTitle("Sắp xếp");
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.sort_bottom_sheet, null);
        AlertDialog dialog = builder.setView(view).create();

        TextView nameAsc = view.findViewById(R.id.nameAsc);
        TextView nameDesc = view.findViewById(R.id.nameDesc);
        TextView dateAsc = view.findViewById(R.id.dateAsc);
        TextView dateDesc = view.findViewById(R.id.dateDesc);

        nameAsc.setVisibility(View.GONE);
        nameDesc.setVisibility(View.GONE);

        dateAsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAsc) {
                    isAsc = true;
                    List<PhotoList> aList = new ArrayList<>(photoLists);
                    Collections.reverse(aList);
                    photoLists = new ArrayList<>(aList);
                    photoListAdapter.setPhotoLists(photoLists);
                    dialog.dismiss();
                }
            }
        });

        dateDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAsc) {
                    isAsc = false;
                    List<PhotoList> aList = new ArrayList<>(photoLists);
                    Collections.reverse(aList);
                    photoLists = new ArrayList<>(aList);
                    photoListAdapter.setPhotoLists(photoLists);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void showPassword() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.private_album, null);
        android.app.AlertDialog dialog = builder.setView(view).create();
        dialog.setTitle("Nhập mật khẩu mới");

        PatternLockView mPatternLockView = view.findViewById(R.id.pattern_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {}

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {}

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                String value = PatternLockUtils.patternToString(mPatternLockView, pattern);
                if (password.length() == 0) {
                    password = value;
                    dialog.setTitle("Xác nhận mật khẩu");
                    mPatternLockView.clearPattern();
                } else {
                    if (password.compareTo(value) != 0) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    } else {
                        user.setPassword(value);
                        dbUser.document(user.getId()).update("password", value);
                        Toast.makeText(PhotoActivity.this, "Update successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            }
            @Override
            public void onCleared() {

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                password = "";
            }
        });

        dialog.show();
    }

    public void createVideo(ArrayList<Bitmap> bitmaps, int width, int height) {
        String path = "/storage/emulated/0/Download/" + new Date().getTime() + ".mp4";
        File file = new File(path);
        MuxerConfig muxerConfig = new MuxerConfig(file, width, height, MediaFormat.MIMETYPE_VIDEO_AVC, 3, 1F, 10000000, new Mp4FrameMuxer(file.getPath(), 1F), 1);
        Muxer muxer = new Muxer(this, muxerConfig);
        muxer.setOnMuxingCompletedListener(new MuxingCompletionListener() {
            @Override
            public void onVideoSuccessful(@NonNull File file) {
                System.out.println(file.getPath());
                try {
                    uploadFile(file);
                } catch (FileNotFoundException e) {
                    System.out.println(e.getMessage());
                    file.delete();
                }
            }

            @Override
            public void onVideoError(@NonNull Throwable throwable) {

            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                muxer.mux(bitmaps, R.raw.bensound_happyrock);
            }
        }).start();
    }

    public void uploadFile(File file) throws FileNotFoundException {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        Uri uri = Uri.fromFile(file);

        Media newMedia = new Media(user.getId(), new Date());
        String childPath = user.getId() + "/" + newMedia.getId() + ".mp4";
        StorageReference mountainsRef = storageRef.child(childPath);

        InputStream stream = getContentResolver().openInputStream(uri);
        UploadTask uploadTask = mountainsRef.putStream(stream);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri firebaseUri) {
                        newMedia.setImgUrl(firebaseUri.toString());
                        newMedia.setType("video/mp4");
                        dbMedia.document(newMedia.getId()).set(newMedia);
                        dbAlbum.document(albumId).update("photos", FieldValue.arrayUnion(newMedia));
                        Intent intent = new Intent(PhotoActivity.this, VideoActivity.class);
                        intent.putExtra("media_id", newMedia.getId());
                        intent.putExtra("album_id", albumId);
                        Toast.makeText(PhotoActivity.this, "Save successfully!", Toast.LENGTH_LONG);
                        PhotoActivity.this.startActivity(intent);
                        file.delete();
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

    public void creativeVideo(int newWidth, int newHeight) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>(Collections.nCopies(selected.size(), null));
        for (int i = 0; i < selected.size(); i++) {
            int index = i;
            Glide.with(this)
                    .asBitmap()
                    .load(selected.get(i).getImgUrl())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            int width = resource.getWidth();
                            int height = resource.getHeight();

                            float scaleWidth = ((float) newWidth) / width;
                            float scaleHeight = ((float) newHeight) / height;
                            float scaleFactor = Math.min(scaleWidth, scaleHeight);
                            android.graphics.Matrix matrix = new android.graphics.Matrix();
                            matrix.postScale(scaleFactor, scaleFactor);
                            Bitmap bitmap = Bitmap.createBitmap(resource, 0, 0, width, height, matrix, true);

                            Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(newBitmap);
                            canvas.drawColor(Color.BLACK);
                            int left = (newWidth - bitmap.getWidth()) / 2;
                            int top = (newHeight - bitmap.getHeight()) / 2;
                            canvas.drawBitmap(bitmap, left, top, new Paint());

                            bitmaps.set(index, newBitmap);
                            if (index + 1 == selected.size()) {
                                createVideo(bitmaps, newWidth, newHeight);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }

    public void onCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn tỉ lệ")
                .setItems(ratio, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        creativeVideo(widths[which], heights[which]);
                        dialog.dismiss();
//                        onChangeMode(false);
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }
}