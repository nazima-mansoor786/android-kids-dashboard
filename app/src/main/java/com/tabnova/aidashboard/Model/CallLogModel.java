package com.tabnova.aidashboard.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CallLogModel implements Serializable {

    String name;
    String phoneNumber;
    String callType;
    String callDuration;
    String dateStr;
    String lastUpdatedData;
    int dataType;

    public CallLogModel() {
    }

    public CallLogModel(String name,String phoneNumber, String callType, String callDuration, String dateStr, String lastUpdatedData, int dataType) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.callType = callType;
        this.callDuration = callDuration;
        this.dateStr = dateStr;
        this.dataType = dataType;
        this.lastUpdatedData = lastUpdatedData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(String callDuration) {
        this.callDuration = callDuration;
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
        return "CallLog [name=" +name+ ", phoneNumber=" +  phoneNumber + ", callType=" +  callType + ", callDuration=" +  callDuration + ", dateStr=" +  dateStr +  ", lastUpdatedData=" +  lastUpdatedData +", dataType=" +  dataType +"]";
    }
}
