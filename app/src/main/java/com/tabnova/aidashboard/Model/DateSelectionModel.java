package com.tabnova.aidashboard.Model;

public class DateSelectionModel {
    int id;
    String selectedType;
    boolean isSelected;

    public DateSelectionModel(int id, String selectedType, boolean isSelected) {
        this.id = id;
        this.selectedType = selectedType;
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return selectedType;
    }
}
