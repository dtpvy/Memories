package com.example.memories;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;

import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {
    ArrayList<Album> albums = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        albums.add(new Album("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "A"));
        albums.add(new Album("https://media.macphun.com/img/uploads/customer/how-to/608/15542038745ca344e267fb80.28757312.jpg?q=85&w=1340", "B"));
        albums.add(new Album("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "C"));
        albums.add(new Album("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "A"));
        albums.add(new Album("https://media.macphun.com/img/uploads/customer/how-to/608/15542038745ca344e267fb80.28757312.jpg?q=85&w=1340", "B"));
        albums.add(new Album("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "C"));

        RecyclerView albumView = (RecyclerView) findViewById(R.id.albumList);
        GridLayoutManager albumLayoutManager = new GridLayoutManager(this, 3);
        albumView.setLayoutManager(albumLayoutManager);
        AlbumAdapter albumHomeAdapter = new AlbumAdapter(albums);
        albumView.setAdapter(albumHomeAdapter);
    }
}