package com.krg.gamestore.Models;

public class AppModel {
    private String name, title, description;
    private String logoUrl;

    public AppModel(String name, String title, String description, String logoUrl) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.logoUrl = logoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getDescription(){
        return description;
    }

    public String getFileName(){
        return name;
    }
}
