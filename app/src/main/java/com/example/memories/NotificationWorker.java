package com.example.memories;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationWorker extends Worker {
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/firebase.messaging"
    );
    CollectionReference dbMedia;

    private static String getAccessToken(Context context) throws IOException {
        Resources resource = context.getResources();
        InputStream is = resource.openRawResource(R.raw.service_account);

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(is)
                .createScoped(SCOPES);
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        dbMedia = new Database().getDbMedia();
    }

    @NonNull
    @Override
    public Result doWork() {
        sendNotification();

        return Result.success();
    }

    private void sendNotification() {
        System.out.println("???????");
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("current_user", Context.MODE_PRIVATE);
        String data = sharedPref.getString("current_user", "");
        Gson gson = new Gson();
        User user = gson.fromJson(data, User.class);

        if (data.isEmpty()) return;

        try {
            String accessToken = getAccessToken(getApplicationContext());
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String token) {
                    System.out.println(token);
                    sendPushNotification(accessToken, token, "Kỷ niệm", "Ngày này năm trước bạn đã có " + 0 + " tấm hình");

                    LocalDate currentDate = LocalDate.now();

                    ValueLineSeries series = new ValueLineSeries();
                    series.setColor(0xFF56B7F1);
                    LocalDate localDate = currentDate.minusYears(1);
                    LocalDateTime localDateTime = localDate.atStartOfDay();
                    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                    Date date = getDate(Date.from(instant));

                    dbMedia.whereEqualTo("userId", user.getId()).whereEqualTo("deletedAt", null).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int cnt = 0;
                                for (QueryDocumentSnapshot documentSnapshot: task.getResult()) {
                                    Media media = documentSnapshot.toObject(Media.class);
                                    if (getDate(media.getCreatedAt()).getTime() == date.getTime()) {
                                        cnt++;
                                    }
                                }

                                if (cnt == 0) return;
                                sendPushNotification(accessToken, token, "Kỷ niệm", "Ngày này năm trước bạn đã có " + cnt + " tấm hình");
                            }
                        }
                    });
                }
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void sendPushNotification(String accessToken, String deviceToken, String title, String message) {
        PushNotificationTask pushNotificationTask = new PushNotificationTask(accessToken, deviceToken, title, message);
        pushNotificationTask.execute();
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

