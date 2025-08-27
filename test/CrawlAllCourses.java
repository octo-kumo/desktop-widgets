import com.google.gson.reflect.TypeToken;

import me.kumo.widgets.courses.Course;
import me.kumo.widgets.courses.CoursesCrawler;
import me.kumo.widgets.Minerva;

import static me.kumo.widgets.Widgets.prefs;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrawlAllCourses {
    public static void main(String[] args) throws IOException {
        Minerva minerva = new Minerva();
        minerva.login(prefs.get("studentId", ""), prefs.get("minervaPin", ""));
        ArrayList<String> subjects = CoursesCrawler.getSubjects(minerva, 2025, CoursesCrawler.TERM.WINTER);
        ArrayList<Course> allCourses = CoursesCrawler.gson.fromJson(new FileReader("all_courses.json"),
                new TypeToken<ArrayList<Course>>() {
                }.getType());
        for (int i = 0; i < subjects.size(); i++) {
            String subject = subjects.get(i);
            System.out.printf("Crawling %s ... %d/%d%n", subject, i, subjects.size());
            if (allCourses.stream().anyMatch(c -> c.subject.equals(subject))) {
                System.out.println("Already crawled " + subject);
                continue;
            }
            List<Course> courseList = CoursesCrawler.getCourseList(minerva, 2025, CoursesCrawler.TERM.WINTER, subject);
            if (courseList == null) {
                System.out.println("Failed to get course list for " + subject);
                continue;
            }
            for (int j = 0; j < courseList.size(); j++) {
                Course course = courseList.get(j);
                int progress = (int) ((j + 1) * 100.0 / courseList.size());
                StringBuilder bar = new StringBuilder();
                for (int k = 0; k < progress / 5; k++)
                    bar.append("=");
                System.out.printf("\rCrawling %s %s ... [%-20s] %d%% (%d/%d)", course.subject, course.courseCode, bar,
                        progress, j + 1, courseList.size());
                course.sections = CoursesCrawler.getCourseSections(minerva, 2025, CoursesCrawler.TERM.WINTER, subject,
                        course.courseCode);
            }
            System.out.println();
            allCourses.addAll(courseList);
            FileWriter writer = new FileWriter("all_courses.json");
            CoursesCrawler.gson.toJson(allCourses, writer);
            writer.flush();
            writer.close();
        }
    }
}
