package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoActivity extends AppCompatActivity {
    ArrayList<PhotoList> photoLists = new ArrayList<>();
    Album album;
    ImageView backBtn;
    TextView albumName;
    FirebaseFirestore db;
    User user;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        db = FirebaseFirestore.getInstance();
        user = new User().getUser(this);

        Intent intent = getIntent();
        String albumId = intent.getStringExtra("album_id");

        albumName = findViewById(R.id.albumName);
        listView = findViewById(R.id.photoList);

        if (albumId.compareTo("trash") != 0) loadData(albumId);
        else loadTrash();

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void loadData(String albumId) {
        Map<Date, ArrayList<Photo>> photos = new HashMap<>();;
        db.collection("albums").document(albumId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                album = documentSnapshot.toObject(Album.class);
                albumName.setText(album.getName());
                for (int i = 0; i < album.getPhotos().size(); i++) {
                    Photo photo = album.getPhotos().get(i);
                    Date date = getDate(photo.getCreatedAt());
                    if (photos.get(date) == null) {
                        ArrayList<Photo> arr = new ArrayList<>();
                        arr.add(photo);
                        photos.put(date, arr);
                    } else {
                        ArrayList<Photo> arr = photos.get(date);
                        arr.add(photo);
                    }
                }
                List<Date> dates = new ArrayList<>(photos.keySet());
                Collections.sort(dates);
                for (int i = 0; i < dates.size(); i++) {
                    photoLists.add(new PhotoList(dates.get(i), photos.get(dates.get(i))));
                }

                PhotoListAdapter photoListAdapter = new PhotoListAdapter(PhotoActivity.this, photoLists);
                listView.setAdapter(photoListAdapter);
            }
        });
    }

    public void loadTrash() {
        Map<Date, ArrayList<Photo>> photos = new HashMap<>();;
        albumName.setText("Thùng rác");
        db.collection("photos").whereNotEqualTo("deletedAt", null).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot queryDocumentSnapshot: task.getResult()) {
                        Photo photo = queryDocumentSnapshot.toObject(Photo.class);
                        Date date = getDate(photo.getCreatedAt());
                        if (photos.get(date) == null) {
                            ArrayList<Photo> arr = new ArrayList<>();
                            arr.add(photo);
                            photos.put(date, arr);
                        } else {
                            ArrayList<Photo> arr = photos.get(date);
                            arr.add(photo);
                        }
                    }
                    List<Date> dates = new ArrayList<>(photos.keySet());
                    Collections.sort(dates);
                    for (int i = 0; i < dates.size(); i++) {
                        photoLists.add(new PhotoList(dates.get(i), photos.get(dates.get(i))));
                    }

                    PhotoListAdapter photoListAdapter = new PhotoListAdapter(PhotoActivity.this, photoLists);
                    listView.setAdapter(photoListAdapter);
                }
            }
        });
    }

    public Date getDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        return newCalendar.getTime();
    }
}