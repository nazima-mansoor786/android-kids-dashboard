package com.tabnova.aidashboard.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class WebHistoryModel implements Serializable {

    int id;
    String linkStr;
    String startTime;
    String endTime;
    String time;
    String date;

    public WebHistoryModel()  {
    }

    public WebHistoryModel(int id, String linkStr) {
        this.id = id;
        this.linkStr = linkStr;
    }

    public WebHistoryModel( String linkStr, String startTime, String endTime, String time, String date) {
        this.linkStr = linkStr;
        this.startTime = startTime;
        this.endTime = endTime;
        this.time = time;
        this.date = date;
    }

    public WebHistoryModel(String linkStr) {
        this.linkStr = linkStr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLinkStr() {
        return linkStr;
    }

    public void setLinkStr(String linkStr) {
        this.linkStr = linkStr;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @NonNull
    @Override
    public String toString() {
        return "Employee [id=" + id +", time=" + time +", appName=" + linkStr + ", date=" + date +", startTime=" + startTime +", endTime=" + endTime + "]";
    }
}
