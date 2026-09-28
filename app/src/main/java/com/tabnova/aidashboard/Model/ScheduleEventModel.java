package com.tabnova.aidashboard.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class ScheduleEventModel implements Serializable {

    int id;
    String profileId;
    String eventName;
    int days;
    String startTime;
    String endTime;
    String groupId;
    String day;

    ArrayList<ScheduleDaysModel> scheduleDaysModels;

    public ScheduleEventModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public ArrayList<ScheduleDaysModel> getScheduleDaysModels() {
        return scheduleDaysModels;
    }

    public void setScheduleDaysModels(ArrayList<ScheduleDaysModel> scheduleDaysModels) {
        this.scheduleDaysModels = scheduleDaysModels;
    }


    @Override
    public String toString() {
        return "Employee [id=" + id +", profileId=" + profileId +", eventName=" + eventName +", days=" + days + ", startTime=" + startTime +", endTime=" + endTime +", groupId=" + groupId + ", day=" + day +"]";
    }
}
