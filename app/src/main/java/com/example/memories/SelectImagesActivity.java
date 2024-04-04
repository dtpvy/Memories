package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SelectImagesActivity extends AppCompatActivity {
    Spinner spinner;
    User user;
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Media> selectMedia;
    RecyclerView photosView;
    TextView albumName;
    SelectPhotoAdapter photoHomeAdapter;
    Album currentAlbum;
    ImageButton closeBtn, selectAllBtn;
    Button doneBtn;
    CollectionReference dbAlbum, dbMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_images);

        Intent intent = getIntent();
        String message = intent.getStringExtra("album_name");
        selectMedia = new ArrayList<>();

        Database db = new Database();
        dbAlbum = db.getDbAlbum();
        dbMedia = db.getDbMedia();

        user = new User().getUser(this);
        spinner = findViewById(R.id.albumList);

        AlertDialog dialog = onCreateDialog();
        albumName = findViewById(R.id.albumName);
        albumName.setText(message);

        photosView = (RecyclerView) findViewById(R.id.allPhotoView);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        photosView.setLayoutManager(gridLayoutManager);
        photoHomeAdapter = new SelectPhotoAdapter(SelectImagesActivity.this);
        photoHomeAdapter.setCallback(new SelectPhotoAdapter.Callback() {
            @Override
            public void onCheckedChanged(Media media, Boolean checked) {
                if (checked) {
                    selectMedia.add(media);
                } else {
                    selectMedia.remove(media);
                }
                System.out.println(selectMedia);
            }

            @Override
            public void onCheckAll(ArrayList<Media> media) {
                selectAll(media.size() > 0);
            }
        });

        photosView.setAdapter(photoHomeAdapter);

        loadAlbums();
        loadPhotos(user.getDefaultAlbum().getId());

        doneBtn = findViewById(R.id.doneBtn);
        closeBtn = findViewById(R.id.cancelButton);
        selectAllBtn = findViewById(R.id.selectAllButton);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        selectAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoHomeAdapter.checkAll();
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }

    public void loadAlbums() {
        dbAlbum.whereEqualTo("userId", user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Album album = document.toObject(Album.class);
                        albums.add(album);
                    }
                    ArrayAdapter ad = new ArrayAdapter(SelectImagesActivity.this, R.layout.spinner_list, albums);
                    ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(ad);
                    for (int i = 0; i < albums.size(); i++) {
                        if (albums.get(i).getId().compareTo(user.getDefaultAlbum().getId()) == 0) {
                            spinner.setSelection(i, true);
                        }
                    }

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            loadPhotos(albums.get(i).getId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
            }
        });
    }

    public void loadPhotos(String albumId) {
        System.out.println(selectMedia);

        dbAlbum.document(albumId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Album album = documentSnapshot.toObject(Album.class);
                currentAlbum = album;
                photoHomeAdapter.setPhotos(album.getPhotos());
                photoHomeAdapter.setSelectedPhotos(selectMedia);
                photoHomeAdapter.notifyDataSetChanged();
            }
        });
    }

    public void selectAll(Boolean isSelect) {
        ArrayList<Media> media = currentAlbum.getPhotos();
        for (int i = 0; i < media.size(); i++) {
            int pos = -1;
            for (int j = 0; j < selectMedia.size(); j++) {
                if (selectMedia.get(j).getId().compareTo(media.get(i).getId()) == 0) {
                    pos = j;
                    break;
                }
            }
            if (!isSelect && pos >= 0) {
                selectMedia.remove(pos);
            }
            if (isSelect && pos == -1) {
                selectMedia.add(media.get(i));
            }
        }
    }

    public AlertDialog onCreateDialog() {
        String[] items = {"Di chuyển", "Sao chép"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectImagesActivity.this);
        builder.setTitle("Đang thêm ảnh")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int pos) {
                        if (pos == 0) {
                            for (Album album : albums) {
                                ArrayList<Media> medias = album.getPhotos();
                                ArrayList<Media> newMedia = new ArrayList<>();
                                for (Media media : medias) {
                                    Boolean include = false;
                                    for (int k = 0; k < selectMedia.size(); k++) {
                                        if (selectMedia.get(k).getId().compareTo(media.getId()) == 0) {
                                            include = true;
                                            break;
                                        }
                                    }
                                    if (!include) {
                                        newMedia.add(media);
                                    }
                                }
                                dbAlbum.document(album.getId()).update("photos", newMedia);
                            }

                            Album album = new Album(user.getId(), selectMedia.get(0).getImgUrl(), albumName.getText().toString(), selectMedia);
                            dbAlbum.document(album.getId()).set(album)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Intent intent = new Intent(SelectImagesActivity.this, PhotoActivity.class);
                                            intent.putExtra("album_id", album.getId());
                                            SelectImagesActivity.this.startActivity(intent);
                                            finish();
                                        }
                                    });
                        } else {
                            Date created = new Date();
                            for (int i = 0; i < selectMedia.size(); i++) {
                                Media p = selectMedia.get(i);
                                p.setId(UUID.randomUUID().toString());
                                p.setCreatedAt(created);
                                dbMedia.document(p.getId()).set(p);
                            }
                            Album album = new Album(user.getId(), selectMedia.get(0).getImgUrl(), albumName.getText().toString(), selectMedia);
                            dbAlbum.document(album.getId()).set(album)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Intent intent = new Intent(SelectImagesActivity.this, PhotoActivity.class);
                                            intent.putExtra("album_id", album.getId());
                                            SelectImagesActivity.this.startActivity(intent);
                                            finish();
                                        }
                                    });
                        }

                    }
                });
        return builder.create();
    }
}