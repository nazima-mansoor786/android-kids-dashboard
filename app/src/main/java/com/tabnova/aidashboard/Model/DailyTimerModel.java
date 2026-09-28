package com.tabnova.aidashboard.Model;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class DailyTimerModel implements Serializable {

    int id;
    String dailyTimeStr;
    TimeUnit dailyTimeUnit;
    long dailyTime;

    public DailyTimerModel() {
    }

    public DailyTimerModel(int id, String dailyTimeStr, TimeUnit dailyTimeUnit, long dailyTime) {
        this.id = id;
        this.dailyTimeStr = dailyTimeStr;
        this.dailyTimeUnit = dailyTimeUnit;
        this.dailyTime = dailyTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDailyTimeStr() {
        return dailyTimeStr;
    }

    public void setDailyTimeStr(String dailyTimeStr) {
        this.dailyTimeStr = dailyTimeStr;
    }

    public TimeUnit getDailyTimeUnit() {
        return dailyTimeUnit;
    }

    public void setDailyTimeUnit(TimeUnit dailyTimeUnit) {
        this.dailyTimeUnit = dailyTimeUnit;
    }

    public long getDailyTime() {
        return dailyTime;
    }

    public void setDailyTime(long dailyTime) {
        this.dailyTime = dailyTime;
    }


    @Override
    public String toString() {
        return dailyTimeStr;
    }
}
