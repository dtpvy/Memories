package com.example.memories;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PhotoActivity extends AppCompatActivity {
    ArrayList<PhotoList> photoLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        ArrayList<Photo> photos = new ArrayList<>();
        photos.add(new Photo("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", new Date()));
        photos.add(new Photo("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", new Date()));
        photos.add(new Photo("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", new Date()));
        photos.add(new Photo("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", new Date()));
        photos.add(new Photo("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", new Date()));
        photos.add(new Photo("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", new Date()));
        photos.add(new Photo("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", new Date()));
        photos.add(new Photo("https://images.pexels.com/photos/414612/pexels-photo-414612.jpeg?cs=srgb&dl=pexels-james-wheeler-414612.jpg&fm=jpg", new Date()));

        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, 3, 1);
        photoLists.add(new PhotoList(calendar.getTime(), photos));

        calendar.set(2024, 2, 29);
        photoLists.add(new PhotoList(calendar.getTime(), photos));

        calendar.set(2024, 2, 20);
        photoLists.add(new PhotoList(calendar.getTime(), photos));

        calendar.set(2024, 2, 10);
        photoLists.add(new PhotoList(calendar.getTime(), photos));

        calendar.set(2024, 1, 20);
        photoLists.add(new PhotoList(calendar.getTime(), photos));

        ListView listView = (ListView) findViewById(R.id.photoList);
        PhotoListAdapter photoListAdapter = new PhotoListAdapter(this, photoLists);

        listView.setAdapter(photoListAdapter);
    }
}