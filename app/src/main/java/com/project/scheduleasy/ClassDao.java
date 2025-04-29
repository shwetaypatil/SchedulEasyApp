package com.project.scheduleasy;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ClassDao {
    @Insert
    void insertAll(ClassSchedule schedule);

    @Query("DELETE FROM class_schedule WHERE timetableId = :timetableId AND startTime = :time")
    void deleteByTimeAndTimetableId(String time, String timetableId);

    @Query("SELECT * FROM class_schedule WHERE timetableId = :timetableId")
    List<ClassSchedule> getClassScheduleForTimetable(String timetableId);

    @Query("SELECT * FROM class_schedule WHERE notifyEnabled = 1")
    List<ClassSchedule> getAllClassesWithNotificationsDirect(); // <-- Not LiveData

}
