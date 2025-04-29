package com.project.scheduleasy;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "class_schedule")
public class ClassSchedule {

    @PrimaryKey
    @NonNull
    public String id;

    public String timetableId;
    public String className;
    public int dayOfWeek;
    public String startTime;
    public String endTime;
    public String room;

    @ColumnInfo(name = "notifyEnabled")
    public boolean notifyEnabled; // ✅ NEW FIELD

    // Constructor
    public ClassSchedule(@NonNull String id, String timetableId, String className,
                         int dayOfWeek, String startTime, boolean notifyEnabled) {
        this.id = id;
        this.timetableId = timetableId;
        this.className = className;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.notifyEnabled = notifyEnabled;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTimetableId() { return timetableId; }
    public void setTimetableId(String timetableId) { this.timetableId = timetableId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public boolean isNotifyEnabled() { return notifyEnabled; }
    public void setNotifyEnabled(boolean notifyEnabled) { this.notifyEnabled = notifyEnabled; }
}
