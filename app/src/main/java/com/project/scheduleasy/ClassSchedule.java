package com.project.scheduleasy;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ClassSchedule {
    @PrimaryKey
    public String id;
    public String className;
    public int dayOfWeek; // Calendar.MONDAY, etc.
    public String startTime; // "HH:mm" format
    public boolean notificationEnabled = true;
}
