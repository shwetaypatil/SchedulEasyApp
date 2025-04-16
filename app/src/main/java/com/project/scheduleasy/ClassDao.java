package com.project.scheduleasy;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ClassDao {
    @Query("SELECT * FROM class_schedule")
    List<ClassSchedule> getAllClassesWithNotifications();
}
