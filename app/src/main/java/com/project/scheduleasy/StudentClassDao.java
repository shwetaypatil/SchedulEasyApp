package com.project.scheduleasy;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;


@Dao
public interface StudentClassDao {
    @Insert
    void insert(StudentClass studentClass);

    @Query("SELECT * FROM student_classes WHERE classId = :classId")
    List<StudentClass> getStudentsForClass(@NonNull String classId);

    @Query("DELETE FROM student_classes WHERE studentId = :studentId AND classId = :classId")
    void deleteEnrollment(@NonNull String studentId, @NonNull String classId);
}

