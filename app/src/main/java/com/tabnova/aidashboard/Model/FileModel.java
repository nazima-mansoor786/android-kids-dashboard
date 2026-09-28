package com.tabnova.aidashboard.Model;

public class FileModel {
    private int id;
    private String fileName;
    private String filePath;
    long fileLastModifiedDate;
    private boolean isFileDirectory;
    private boolean isSelected;

    public FileModel(int id, String fileName, String filePath, boolean isFileDirectory, boolean isSelected) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.isFileDirectory = isFileDirectory;
        this.isSelected = isSelected;
    }

    public FileModel(int id, String fileName, String filePath, long fileLastModifiedDate, boolean isFileDirectory, boolean isSelected) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileLastModifiedDate = fileLastModifiedDate;
        this.isFileDirectory = isFileDirectory;
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileLastModifiedDate() {
        return fileLastModifiedDate;
    }

    public void setFileLastModifiedDate(long fileLastModifiedDate) {
        this.fileLastModifiedDate = fileLastModifiedDate;
    }

    public boolean isFileDirectory() {
        return isFileDirectory;
    }

    public void setFileDirectory(boolean fileDirectory) {
        isFileDirectory = fileDirectory;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
