package com.tabnova.aidashboard.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class SmsLogModel implements Serializable {

    String number;
    String message;
    String type;
    String dateStr;
    String lastUpdatedData;
    int dataType;

    public SmsLogModel() {
    }

    public SmsLogModel(String number, String message, String type, String dateStr,String lastUpdatedData, int dataType) {
        this.number = number;
        this.message = message;
        this.type = type;
        this.dateStr = dateStr;
        this.dataType = dataType;
        this.lastUpdatedData = lastUpdatedData;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getLastUpdatedData() {
        return lastUpdatedData;
    }

    public void setLastUpdatedData(String lastUpdatedData) {
        this.lastUpdatedData = lastUpdatedData;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    @NonNull
    @Override
    public String toString() {
        return "SmsLog [phoneNumber=" +  number + ", message=" +  message + ", type=" +  type + ", dateStr=" +  dateStr + ", lastUpdatedData=" +  lastUpdatedData + ", dataType=" +  dataType +"]";
    }
}
