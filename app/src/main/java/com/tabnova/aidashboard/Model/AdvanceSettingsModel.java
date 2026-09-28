package com.tabnova.aidashboard.Model;

import java.io.Serializable;

public class AdvanceSettingsModel implements Serializable {
    int id = 0;
    int status;
    String advanceSettingName;
    boolean isChecked = false;

    public AdvanceSettingsModel(int id, int status, String advanceSettingName, boolean isChecked) {
        this.id = id;
        this.status = status;
        this.advanceSettingName = advanceSettingName;
        this.isChecked = isChecked;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAdvanceSettingName() {
        return advanceSettingName;
    }

    public void setAdvanceSettingName(String advanceSettingName) {
        this.advanceSettingName = advanceSettingName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AdvanceSettingsModel{" +
                "id=" + id +
                ", status=" + status +
                ", advanceSettingName='" + advanceSettingName + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }
}
