package com.tabnova.aidashboard.Model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class SelectedFileFolderModel implements Serializable {
    private int id;
    private String fileStr;
    private long fileCreateDate;
    private boolean isSelected;
    private boolean isFileDirectory;
    private ArrayList<SubSelectedFileFolderModel> filePathList;

    public SelectedFileFolderModel(int id, String fileStr, boolean isSelected, boolean isFileDirectory) {
        this.id = id;
        this.fileStr = fileStr;
        this.isSelected = isSelected;
        this.isFileDirectory = isFileDirectory;
    }

    public SelectedFileFolderModel(int id, String fileStr, long fileCreateDate, boolean isSelected, boolean isFileDirectory, ArrayList<SubSelectedFileFolderModel> filePathList) {
        this.id = id;
        this.fileStr = fileStr;
        this.fileCreateDate = fileCreateDate;
        this.isSelected = isSelected;
        this.isFileDirectory = isFileDirectory;
        this.filePathList = filePathList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileStr() {
        return fileStr;
    }

    public void setFileStr(String fileStr) {
        this.fileStr = fileStr;
    }

    public long getFileCreateDate() {
        return fileCreateDate;
    }

    public void setFileCreateDate(long fileCreateDate) {
        this.fileCreateDate = fileCreateDate;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isFileDirectory() {
        return isFileDirectory;
    }

    public void setFileDirectory(boolean fileDirectory) {
        isFileDirectory = fileDirectory;
    }

    public ArrayList<SubSelectedFileFolderModel> getFilePathList() {
        return filePathList;
    }

    public void setFilePathList(ArrayList<SubSelectedFileFolderModel> filePathList) {
        this.filePathList = filePathList;
    }

    @NonNull
    @Override
    public String toString() {
        return "SelectedFileFolderModel(id=" + id + ", fileStr=" + fileStr + ", fileCreateDate=" + fileCreateDate + ", isSelected=" + isSelected + " isFileDirectory=" + isFileDirectory + ")";
    }
}
