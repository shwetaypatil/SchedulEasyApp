package com.project.scheduleasy;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "timetables")
public class TimetableMeta {
    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private long createdAt;

    public TimetableMeta(String id, String title, long createdAt){
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
    }

    public String getId(){ return id; }
    public String getTitle(){ return title; }
    public long getCreatedAt(){ return createdAt; }
    public void setTitle(String title){ this.title = title; }

    // Utility method
    public String getFormattedDate() {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date(createdAt));
    }
}
