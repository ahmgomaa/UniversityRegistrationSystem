package service;

import model.AcademicRecord;
import model.Enrollment;
import model.Semester;
import model.Student;
import model.enums.EnrollmentStatus;

import java.util.ArrayList;
import java.util.Collections;

public class ReportService {

    public static class SemesterSummary {

        private final Semester semester;
        private final ArrayList<Enrollment> enrollments;
        private final double gpa;
        private final int registeredCredits;

        public SemesterSummary(Semester semester, ArrayList<Enrollment> enrollments,
                               double gpa, int registeredCredits) {
            this.semester = semester;
            this.enrollments = enrollments;
            this.gpa = gpa;
            this.registeredCredits = registeredCredits;
        }

        public Semester getSemester() {
            return semester;
        }

        public ArrayList<Enrollment> getEnrollments() {
            return enrollments;
        }

        public double getGpa() {
            return gpa;
        }

        public int getRegisteredCredits() {
            return registeredCredits;
        }
    }

    public static class Transcript {

        private final Student student;
        private final ArrayList<SemesterSummary> semesters;
        private final double cgpa;
        private final int earnedCredits;

        public Transcript(Student student, ArrayList<SemesterSummary> semesters,
                          double cgpa, int earnedCredits) {
            this.student = student;
            this.semesters = semesters;
            this.cgpa = cgpa;
            this.earnedCredits = earnedCredits;
        }

        public Student getStudent() {
            return student;
        }

        public ArrayList<SemesterSummary> getSemesters() {
            return semesters;
        }

        public double getCgpa() {
            return cgpa;
        }

        public int getEarnedCredits() {
            return earnedCredits;
        }
    }

    public Transcript transcript(Student student) {
        AcademicRecord record = student.getAcademicRecord();

        ArrayList<Semester> semesters = new ArrayList<Semester>();
        for (Enrollment e : record.getEnrollments()) {
            Semester sem = e.getCourseSection().getSemester();
            if (!semesters.contains(sem)) {
                semesters.add(sem);
            }
        }

        Collections.sort(semesters);

        ArrayList<SemesterSummary> summaries = new ArrayList<SemesterSummary>();
        for (Semester sem : semesters) {
            ArrayList<Enrollment> list = record.getEnrollmentsInSemester(sem);
            int credits = 0;
            for (Enrollment e : list) {
                if (e.getStatus() != EnrollmentStatus.WITHDRAWN) {
                    credits += e.getCourseSection().getCourse().getCreditHours();
                }
            }
            summaries.add(new SemesterSummary(sem, list, record.calculateGPA(sem), credits));
        }

        return new Transcript(student, summaries, record.calculateCGPA(),
                record.getEarnedCredits());
    }
}
