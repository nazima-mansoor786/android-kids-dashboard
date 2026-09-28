package com.tabnova.aidashboard.Model;

public class CallLogsModel {
    private int id;
    private String name;
    private String number;
    private String type;
    private String duration;
    private String date;
    private long dateLong;
    private boolean isSelected;

    public CallLogsModel(int id, String name, String number, String type, String duration, String date, long dateLong, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.type = type;
        this.duration = duration;
        this.date = date;
        this.dateLong = dateLong;
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDateLong() {
        return dateLong;
    }

    public void setDateLong(long dateLong) {
        this.dateLong = dateLong;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
