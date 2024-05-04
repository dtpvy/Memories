package com.example.memories;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PushNotificationTask extends AsyncTask<Void, Void, Void> {

    private static final String FCM_API = "https://fcm.googleapis.com/v1/projects/memories-4dcba/messages:send";
    private String accessToken;
    private String token;
    private String title;
    private String body;

    public PushNotificationTask(String accessToken, String token, String title, String body) {
        this.token = token;
        this.title = title;
        this.body = body;
        this.accessToken = accessToken;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(FCM_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/json");

            JSONObject json = new JSONObject();
            JSONObject message = new JSONObject();
            JSONObject notification = new JSONObject();

            notification.put("title", title);
            notification.put("body", body);
            message.put("token", token);
            message.put("notification", notification);
            json.put("message", message);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
            outputStreamWriter.write(json.toString());
            outputStreamWriter.flush();
            outputStreamWriter.close();

            int responseCode = conn.getResponseCode();
            Log.d("PushNotificationTask", "Response Code : " + responseCode);

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

