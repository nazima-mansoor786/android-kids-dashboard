package com.tabnova.aidashboard.Model;

import androidx.annotation.NonNull;

public class SaveCallHistoryModel {

    String name;
    String phoneNumber;
    String callType;
    String callDuration;
    String message;
    String  makeOn;
    String type;

    public SaveCallHistoryModel() {
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMakeOn() {
        return makeOn;
    }

    public void setMakeOn(String makeOn) {
        this.makeOn = makeOn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "saveCallHistory [name=" +  name + ", phoneNumber=" +  phoneNumber + ", callType=" +  callType + ", callDuration=" +  callDuration + ", message=" +  message + ", makeOn=" +  makeOn + ", type=" +  type +"]";
    }
}
