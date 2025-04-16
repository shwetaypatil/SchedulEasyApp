package com.project.scheduleasy;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TimetableDao {
    @Insert
    void insert(TimetableMeta timetable);

    @Query("SELECT * FROM timetables")
    List<TimetableMeta> getAllTimetables();

    @Delete
    void delete(TimetableMeta timetable);
}
