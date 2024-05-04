package com.example.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {
    TextView tvVideo, tvImage;
    PieChart pieChart;
    ValueLineChart lineChart;
    int video, image, trash;
    long videoSize, imageSize;
    CollectionReference dbMedia;
    User user;
    Map<Date, ArrayList<Media>> photos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        user = new User().getUser(this);
        dbMedia = new Database().getDbMedia();

        tvVideo = findViewById(R.id.tvR);
        tvImage = findViewById(R.id.tvPython);
        pieChart = findViewById(R.id.piechart);
        lineChart = findViewById(R.id.lineChart);

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadData();
    }

    public void loadData() {
        dbMedia.whereEqualTo("userId", user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    photos = new HashMap<>();
                    video = image = trash = 0;
                    videoSize = imageSize = 0;
                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()) {
                        Media media = documentSnapshot.toObject(Media.class);
                        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(media.getImgUrl());
                        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                long size = storageMetadata.getSizeBytes();
                                if (media.isVideo()) {
                                    videoSize += size;
                                } else {
                                    imageSize += size;
                                }
                                if (video + image + trash == task.getResult().size()) {
                                    tvVideo.setText("Tổng file : " + video + "  Dung lượng: " + Utils.formatSize(videoSize));
                                    tvImage.setText("Tổng file : " + image + "  Dung lượng: " + Utils.formatSize(imageSize));
                                }
                            }
                        });

                        if (media.getDeletedAt() != null) {
                            trash++;
                            continue;
                        }
                        if (media.isVideo()) {
                            video++;
                        } else {
                            image++;
                        }

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

                    loadPieChart();
                    loadLineChart();
                }
            }
        });
    }

    public void loadPieChart() {
        pieChart.addPieSlice(
                new PieModel(
                        "Video",
                        video,
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Image",
                        image,
                        Color.parseColor("#66BB6A")));
        pieChart.addPieSlice(
                new PieModel(
                        "Trash",
                        trash,
                        Color.parseColor("#EF5350")));

        pieChart.startAnimation();
    }

    public void loadLineChart() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM");

        ValueLineSeries series = new ValueLineSeries();
        series.setColor(0xFF56B7F1);

        for (int i = 7; i >= 0; i--) {
            LocalDate localDate = currentDate.minusDays(i);
            LocalDateTime localDateTime = localDate.atStartOfDay();
            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
            Date date = getDate(Date.from(instant));

            series.addPoint(new ValueLinePoint(localDate.format(formatter), photos.get(date) == null ? 0 : photos.get(date).size()));
        }

        lineChart.addSeries(series);
        lineChart.startAnimation();
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
}