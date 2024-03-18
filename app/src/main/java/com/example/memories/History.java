package com.example.memories;

import java.util.Date;
import java.util.UUID;

public class History {
    private String id;
    private String userId;
    private Date date;
    private String deviceId;

    public History() {};

    public History(String userId, Date date, String deviceId) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public History(String id, String userId, Date date, String deviceId) {
        this.id = id;
        this.date = date;
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
