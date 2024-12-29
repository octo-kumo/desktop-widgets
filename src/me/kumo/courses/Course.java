package me.kumo.courses;

import java.time.DayOfWeek;
import java.util.Set;

public class Course {
    public String subject;
    public String courseCode;
    public String courseName;
    public int year;
    public CoursesCrawler.TERM term;
    public Section[] sections;

    public Course(String subject, String courseCode, String courseName, int year, CoursesCrawler.TERM term) {
        this.subject = subject;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.year = year;
        this.term = term;
    }

    public static class Section {
        public String CRN;
        public String subject;
        public String courseCode;
        public String section;
        public String type;
        public double credits;
        public String title;

        public String instructor;
        public String status;

        public Slot[] slots;

        public int capacity;
        public int enrolled;
        public int remaining;

        public int waitlistCapacity;
        public int waitlistEnrolled;
        public int waitlistRemaining;

        public String note;

        public Section(String CRN, String subject, String courseCode, String section, String type, double credits, String title, String instructor, String status, int capacity, int enrolled, int remaining, int waitlistCapacity, int waitlistEnrolled, int waitlistRemaining) {
            this.CRN = CRN;
            this.subject = subject;
            this.courseCode = courseCode;
            this.section = section;
            this.type = type;
            this.credits = credits;
            this.title = title;
            this.instructor = instructor;
            this.status = status;
            this.capacity = capacity;
            this.enrolled = enrolled;
            this.remaining = remaining;
            this.waitlistCapacity = waitlistCapacity;
            this.waitlistEnrolled = waitlistEnrolled;
            this.waitlistRemaining = waitlistRemaining;
        }
    }

    public static class Slot {
        public Set<DayOfWeek> days;
        public String location;
        public String startTime;
        public String endTime;
        public String startDate;
        public String endDate;

        public Slot(Set<DayOfWeek> days, String location, String startTime, String endTime, String startDate, String endDate) {
            this.days = days;
            this.location = location;
            this.startTime = startTime;
            this.endTime = endTime;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}