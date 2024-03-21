package com.example.memories;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.google.common.io.Files;

import java.io.File;

public class ImageAction {
    private Context context;

    public ImageAction(Context context) {
        this.context = context;
    }

    public void downloadImage(String downloadUrlOfImage){
        File file = new File(downloadUrlOfImage);
        String filename = file.getName();
        String type = "image/" + Files.getFileExtension(filename);
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(downloadUrlOfImage);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setMimeType(type)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename);
        dm.enqueue(request);
    }
}
