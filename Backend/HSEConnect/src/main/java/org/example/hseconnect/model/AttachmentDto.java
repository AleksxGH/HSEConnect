package org.example.hseconnect.model;

public class AttachmentDto {
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;

    public AttachmentDto(String fileUrl, String fileName, String fileType, Long fileSize) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }
}