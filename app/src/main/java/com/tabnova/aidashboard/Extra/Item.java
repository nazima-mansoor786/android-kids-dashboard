package com.tabnova.aidashboard.Extra;

import android.graphics.drawable.Drawable;

import java.util.Comparator;

/**
 *
 *
 */

public class Item implements Comparator<Item> {
    private Drawable image;
    private String urlPath;
    private String Label;
    private String type;
    private String faviconUrl;
    private int pageNumber;
    private Integer position;
    private Integer rowNumber;
    private Integer columnNumber;


    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public String getLabel() {
        return Label;
    }

    public void setLabel(String label) {
        Label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    @Override
    public int compare(Item item, Item t1) {
        return item.getPosition().compareTo(t1.getPosition());
    }

    @Override
    public String toString() {
        return "Item{" +
                "faviconUrl='" + faviconUrl + '\'' +
                ", image=" + image +
                ", urlPath='" + urlPath + '\'' +
                ", Label='" + Label + '\'' +
                ", type='" + type + '\'' +
                ", pageNumber=" + pageNumber +
                ", position='" + position + '\'' +
                '}';
    }
}
