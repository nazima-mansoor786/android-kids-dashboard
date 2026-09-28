package com.tabnova.aidashboard.Model;

import java.io.Serializable;

public class ApplicationTimerModel implements Serializable {
    int id;
    String time;
    String appName;
    String appPackageName;
    String date;
    String startTime;
    String endTime;

    public ApplicationTimerModel(int id, String time, String appName, String appPackageName, String date, String startTime, String endTime) {
        this.id = id;
        this.time = time;
        this.appName = appName;
        this.appPackageName = appPackageName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public ApplicationTimerModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    @Override
    public String toString() {
        return "Employee [id=" + id +", time=" + time +", appName=" + appName +", appPackageName=" + appPackageName + ", date=" + date +", startTime=" + startTime +", endTime=" + endTime + "]";
    }
}
