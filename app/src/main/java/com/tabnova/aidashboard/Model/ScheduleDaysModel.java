package com.tabnova.aidashboard.Model;

public class ScheduleDaysModel {

    int id;
    String dayName;
    boolean isSelected;

    public ScheduleDaysModel(int id, String dayName, boolean isSelected) {
        this.id = id;
        this.dayName = dayName;
        this.isSelected = isSelected;
    }

    public ScheduleDaysModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    @Override
    public String toString() {
        return "Employee [id=" + id +", dayName=" + dayName +", isSelected=" + isSelected + "]";
    }
}
