package com.example.imssystem.models;

public class StatusLog {
    private String id;
    private String complaintId;
    private String updatedBy;
    private String oldStatus;
    private String newStatus;
    private String note;
    private String timestamp;

    public StatusLog() {
    }

    public StatusLog(String complaintId, String updatedBy, String oldStatus, String newStatus, String note, String timestamp) {
        this.complaintId = complaintId;
        this.updatedBy = updatedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
