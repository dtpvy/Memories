package com.example.memories;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    ArrayList<DiscoverHomeView> discovers = new ArrayList<>();
    ArrayList<Album> albums = new ArrayList<>();
    ImageButton addButton, settingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        discovers.add(new DiscoverHomeView("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "A"));
        discovers.add(new DiscoverHomeView("https://media.macphun.com/img/uploads/customer/how-to/608/15542038745ca344e267fb80.28757312.jpg?q=85&w=1340", "B"));
        discovers.add(new DiscoverHomeView("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "C"));

        RecyclerView discoverView = (RecyclerView) findViewById(R.id.discoverView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        discoverView.setLayoutManager(layoutManager);
        DiscoverHomeAdapter discoverHomeAdapter = new DiscoverHomeAdapter(discovers);
        discoverView.setAdapter(discoverHomeAdapter);

        albums.add(new Album("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "A"));
        albums.add(new Album("https://media.macphun.com/img/uploads/customer/how-to/608/15542038745ca344e267fb80.28757312.jpg?q=85&w=1340", "B"));
        albums.add(new Album("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", "C"));
        RecyclerView albumView = (RecyclerView) findViewById(R.id.albumView);
        LinearLayoutManager albumLayoutManager = new LinearLayoutManager(this);
        albumLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        albumView.setLayoutManager(albumLayoutManager);
        AlbumHomeAdapter albumHomeAdapter = new AlbumHomeAdapter(albums);
        albumView.setAdapter(albumHomeAdapter);

        RecyclerView photosView = (RecyclerView) findViewById(R.id.photosView);

        // Using ArrayList to store images data
        ArrayList images = new ArrayList<>();
        images.add("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg");
        images.add("https://media.macphun.com/img/uploads/customer/how-to/608/15542038745ca344e267fb80.28757312.jpg?q=85&w=1340");
        images.add("https://media.macphun.com/img/uploads/customer/how-to/608/15542038745ca344e267fb80.28757312.jpg?q=85&w=1340");
        images.add("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg");
        images.add("https://media.macphun.com/img/uploads/customer/how-to/608/15542038745ca344e267fb80.28757312.jpg?q=85&w=1340");
        images.add("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg");
        images.add("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg");

        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        photosView.setLayoutManager(gridLayoutManager);
        photosView.setHasFixedSize(true);

        PhotoHomeAdapter photoHomeAdapter = new PhotoHomeAdapter(this, images);
        photosView.setAdapter(photoHomeAdapter);

        addButton = (ImageButton) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SelectImagesActivity.class);
                view.getContext().startActivity(intent);
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
    }
}