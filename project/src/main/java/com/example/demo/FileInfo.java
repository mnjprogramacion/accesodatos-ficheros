package com.example.demo;

import java.util.Date;

public class FileInfo {

    private String name;
    private String path;
    private long size;
    private Date lastModified;
    private boolean isDirectory;
    private String extension;

    public FileInfo() {}

    public FileInfo(String name, String path, long size, Date lastModified, boolean isDirectory, String extension) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.lastModified = lastModified;
        this.isDirectory = isDirectory;
        this.extension = extension;
    }

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }

    public Date getLastModified() { return lastModified; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }

    public boolean isDirectory() { return isDirectory; }
    public void setDirectory(boolean directory) { isDirectory = directory; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }
}
