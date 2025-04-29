package com.project.scheduleasy;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "student_classes", primaryKeys = {"studentId", "classId"})
public class StudentClass {

    @NonNull
    public String studentId;

    @NonNull
    public String classId;

    public long enrollmentDate;

    public StudentClass(@NonNull String studentId, @NonNull String classId) {
        this.studentId = studentId;
        this.classId = classId;
        this.enrollmentDate = System.currentTimeMillis();
    }

    @NonNull
    public String getStudentId() {
        return studentId;
    }

    @NonNull
    public String getClassId() {
        return classId;
    }

    public long getEnrollmentDate() {
        return enrollmentDate;
    }
}
