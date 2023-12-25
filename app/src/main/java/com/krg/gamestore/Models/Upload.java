package com.krg.gamestore.Models;

public class Upload {
    private String fileName;
    private String downloadUrl;

    public Upload() {
        // Default constructor required for calls to DataSnapshot.getValue(Upload.class)
    }

    public Upload(String fileName, String downloadUrl) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
