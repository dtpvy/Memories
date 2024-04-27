package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    ArrayList<Album> albums;
    ArrayList<Media> media;
    ArrayList<Object> objects;
    ImageButton addButton, settingButton;
    Button seeAlbumButton;
    User user;
    TextView totalPhoto;
    AlertDialog createAlbumDialog;
    RecyclerView photosView, albumView, discoverView;
    PhotoHomeAdapter photoHomeAdapter;
    DiscoverHomeAdapter discoverHomeAdapter;
    CollectionReference dbAlbum, dbMedia, dbObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Database db = new Database();
        dbAlbum = db.getDbAlbum();
        dbMedia = db.getDbMedia();
        dbObject = db.getDbObject();
        user = new User().getUser(this);

        totalPhoto = findViewById(R.id.totalPhoto);
        createAlbumDialog = createAlbum();

        discoverView = (RecyclerView) findViewById(R.id.discoverView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        discoverView.setLayoutManager(layoutManager);

        photosView = (RecyclerView) findViewById(R.id.photosView);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        photosView.setLayoutManager(gridLayoutManager);
        photoHomeAdapter = new PhotoHomeAdapter(HomeActivity.this);
        photosView.setAdapter(photoHomeAdapter);

        albumView = (RecyclerView) findViewById(R.id.albumView);
        LinearLayoutManager albumLayoutManager = new LinearLayoutManager(this);
        albumLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        albumView.setLayoutManager(albumLayoutManager);

        addButton = (ImageButton) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAlbumDialog.show();
            }
        });

        settingButton = (ImageButton) findViewById(R.id.settingButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SettingActivity.class);
                view.getContext().startActivity(intent);
            }
        });

        seeAlbumButton = findViewById(R.id.seeAlbumButton);
        seeAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AlbumActivity.class);
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        this.getAlbumData();
        this.getPhotos();
        this.getObjects();
    }

    public void getPhotos() {
        media = new ArrayList<>();
        dbMedia.whereEqualTo("userId", user.getId()).whereEqualTo("deletedAt", null).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Media media = document.toObject(Media.class);
                                HomeActivity.this.media.add(media);
                            }
                            photoHomeAdapter.setData(media);
                            totalPhoto.setText(media.size() + " photos");
                        }
                    }
                });
    }

    public void getObjects() {
        objects = new ArrayList<>();
        dbObject.whereEqualTo("userId", user.getId()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Object object = document.toObject(Object.class);
                                objects.add(object);
                            }
                            discoverHomeAdapter = new DiscoverHomeAdapter(objects);
                            discoverView.setAdapter(discoverHomeAdapter);
                        }
                    }
                });
    }

    public void getAlbumData() {
        RecyclerView albumView = (RecyclerView) findViewById(R.id.albumView);
        LinearLayoutManager albumLayoutManager = new LinearLayoutManager(this);
        albumLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        albumView.setLayoutManager(albumLayoutManager);
        albums = new ArrayList<>();

        dbAlbum.whereEqualTo("userId", user.getId()).whereEqualTo("mutate", true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Album album = document.toObject(Album.class);
                        if (album.getImgUrl() == null) {
                            ArrayList<Media> media = album.getPhotos();
                            if (media.size() > 0) {
                                album.setImgUrl(media.get(media.size()-1).getImgUrl());
                            } else {
                                album.setImgUrl(getString(R.string.empty_img));
                            }
                        }
                        albums.add(album);
                    }
                    AlbumHomeAdapter albumHomeAdapter = new AlbumHomeAdapter(HomeActivity.this, albums);
                    albumView.setAdapter(albumHomeAdapter);
                }
            }
        });
    }

    public AlertDialog createAlbum() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.create_album, null);
        AlertDialog dialog = builder.setView(view).create();

        EditText nameInput = (EditText) view.findViewById(R.id.albumName);
        TextView error = (TextView) view.findViewById(R.id.errorAlbumName);
        Button addBtn = (Button) view.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = nameInput.getText().toString();
                if (value.length() == 0) {
                    error.setText("Bắt buộc");
                } else if (!checkAlbumName(value)) {
                    error.setText("Tên album đã tồn tại");
                } else {
                    error.setText("");
                    dialog.dismiss();
                    Intent intent = new Intent(HomeActivity.this, SelectImagesActivity.class);
                    intent.putExtra("album_name", value);
                    HomeActivity.this.startActivity(intent);
                }
            }
        });

        Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameInput.setText("");
                error.setText("");
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public Boolean checkAlbumName(String name) {
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getName().compareTo(name) == 0) {
                return false;
            }
        }
        return true;
    }
}