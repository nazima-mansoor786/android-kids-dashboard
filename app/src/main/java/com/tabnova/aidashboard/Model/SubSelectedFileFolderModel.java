package com.tabnova.aidashboard.Model;

public class SubSelectedFileFolderModel {
    private int id;
    private String fileStr;
    long fileCreateDate;
    private boolean isSelected;
    private boolean isFileDirectory;

    public SubSelectedFileFolderModel(int id, String fileStr, boolean isSelected, boolean isFileDirectory) {
        this.id = id;
        this.fileStr = fileStr;
        this.isSelected = isSelected;
        this.isFileDirectory = isFileDirectory;
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
}
