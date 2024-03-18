package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    ArrayList<DiscoverHomeView> discovers = new ArrayList<>();
    ArrayList<Album> albums;
    ImageButton addButton, settingButton;
    Button seeAlbumButton;
    User user;
    FirebaseFirestore db;
    TextView totalPhoto;
    AlertDialog createAlbumDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        user = new User().getUser(this);

        totalPhoto = findViewById(R.id.totalPhoto);
        createAlbumDialog = createAlbum();

        discovers.add(new DiscoverHomeView("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "A"));
        discovers.add(new DiscoverHomeView("https://media.macphun.com/img/uploads/customer/how-to/608/15542038745ca344e267fb80.28757312.jpg?q=85&w=1340", "B"));
        discovers.add(new DiscoverHomeView("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "C"));

        RecyclerView discoverView = (RecyclerView) findViewById(R.id.discoverView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        discoverView.setLayoutManager(layoutManager);
        DiscoverHomeAdapter discoverHomeAdapter = new DiscoverHomeAdapter(discovers);
        discoverView.setAdapter(discoverHomeAdapter);

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
    }

    public void getPhotos() {
        RecyclerView photosView = (RecyclerView) findViewById(R.id.photosView);
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        photosView.setLayoutManager(gridLayoutManager);
        photosView.setHasFixedSize(true);
        db.collection("photos").whereEqualTo("userId", user.getId()).whereEqualTo("deletedAt", null).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Photo> photos = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Photo photo = document.toObject(Photo.class);
                                photos.add(photo);
                            }
                            PhotoHomeAdapter photoHomeAdapter = new PhotoHomeAdapter(HomeActivity.this, photos);
                            photosView.setAdapter(photoHomeAdapter);
                            totalPhoto.setText(photos.size() + " photos");
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

        db.collection("albums").whereEqualTo("userId", user.getId()).whereEqualTo("mutate", true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Album album = document.toObject(Album.class);
                        if (album.getImgUrl() == null) {
                            ArrayList<Photo> photos = album.getPhotos();
                            if (photos.size() > 0) {
                                album.setImgUrl(photos.get(photos.size()-1).getImgUrl());
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