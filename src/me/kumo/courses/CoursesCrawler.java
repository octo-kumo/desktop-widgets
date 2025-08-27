package me.kumo.courses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.kumo.Minerva;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

import static org.jsoup.helper.HttpConnection.KeyVal.create;

public class CoursesCrawler {
    public static final Gson gson = new GsonBuilder().create();

    public enum TERM {
        FALL("09"),
        WINTER("01"),
        SUMMER("05");

        public final String suffix;

        TERM(String suffix) {
            this.suffix = suffix;
        }
    }

    public static List<Course> getCourseList(Minerva minerva, int year, TERM term, String subject) {
        Document doc = minerva.post("/pban1/bwskfcls.P_GetCrse", new ArrayList<Connection.KeyVal>() {{
            add(create("rsts", "dummy"));
            add(create("crn", "dummy"));
            add(create("term_in", year + term.suffix));
            add(create("sel_subj", "dummy"));
            add(create("sel_day", "dummy"));
            add(create("sel_schd", "dummy"));
            add(create("sel_insm", "dummy"));
            add(create("sel_camp", "dummy"));
            add(create("sel_levl", "dummy"));
            add(create("sel_sess", "dummy"));
            add(create("sel_instr", "dummy"));
            add(create("sel_ptrm", "dummy"));
            add(create("sel_attr", "dummy"));
            add(create("sel_subj", subject));
            add(create("sel_crse", ""));
            add(create("sel_title", ""));
            add(create("sel_from_cred", ""));
            add(create("sel_to_cred", ""));
            add(create("sel_ptrm", "%"));
            add(create("begin_hh", "0"));
            add(create("begin_mi", "0"));
            add(create("end_hh", "0"));
            add(create("end_mi", "0"));
            add(create("begin_ap", "x"));
            add(create("end_ap", "y"));
            add(create("path", "1"));
            add(create("SUB_BTN", "Course Search"));
        }});
        Elements rows = doc.select("table[summary='This layout table is used to present the course found'] tr");
        if (rows.isEmpty()) return null;
        return rows.stream().skip(2).map(row -> {
            Elements cells = row.select("td");
            if (cells.isEmpty()) return null;
            return new Course(subject, cells.get(0).text().trim(), cells.get(1).text().trim(), year, term);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    public static Course.Section[] getCourseSections(Minerva minerva, int year, TERM term, String subject, String course) {
        Document doc = minerva.post("/pban1/bwskfcls.P_GetCrse", new ArrayList<Connection.KeyVal>() {{
            add(create("term_in", year + term.suffix));
            add(create("sel_subj", "dummy"));
            add(create("sel_subj", subject));
            add(create("SEL_CRSE", course));
            add(create("SEL_TITLE", ""));
            add(create("BEGIN_HH", "0"));
            add(create("BEGIN_MI", "0"));
            add(create("BEGIN_AP", "a"));
            add(create("SEL_DAY", "dummy"));
            add(create("SEL_PTRM", "dummy"));
            add(create("END_HH", "0"));
            add(create("END_MI", "0"));
            add(create("END_AP", "a"));
            add(create("SEL_CAMP", "dummy"));
            add(create("SEL_SCHD", "dummy"));
            add(create("SEL_SESS", "dummy"));
            add(create("SEL_INSTR", "dummy"));
            add(create("SEL_INSTR", "%"));
            add(create("SEL_ATTR", "dummy"));
            add(create("SEL_ATTR", "%"));
            add(create("SEL_LEVL", "dummy"));
            add(create("SEL_LEVL", "%"));
            add(create("SEL_INSM", "dummy"));
            add(create("sel_dunt_code", ""));
            add(create("sel_dunt_unit", ""));
            add(create("call_value_in", ""));
            add(create("rsts", "dummy"));
            add(create("crn", "dummy"));
            add(create("path", "1"));
            add(create("SUB_BTN", "View Sections"));
        }});
        Elements rows = doc.select("table[summary='This layout table is used to present the sections found'] tr");
        if (rows.isEmpty()) return null;
        ArrayList<Course.Section> sections = new ArrayList<>();
        Course.Section section = null;
        ArrayList<Course.Slot> slots = new ArrayList<>();
        int o = 0;
        for (int i = 2; i < rows.size(); i++) {
            Element row = rows.get(i);
            if (row.text().trim().isEmpty()) continue;
            if (row.childrenSize() == 2) {
                String note = row.text().replace("NOTES: ", "").trim();
                if (section != null) section.note = note;
            } else {
                String crn = row.child(1).text().trim();
                if (!crn.isEmpty()) {//add to section
                    if (section != null) {
                        section.slots = slots.toArray(new Course.Slot[0]);
                        slots = new ArrayList<>();
                        sections.add(section);
                    }
                    o = row.child(8).text().trim().equals("TBA") ? 1 : 0;
                    String credits = row.child(6).text().trim();
                    if (credits.contains("/")) credits = credits.split("/")[1];
                    section = new Course.Section(crn,
                            row.child(2).text().trim(),
                            row.child(3).text().trim(),
                            row.child(4).text().trim(),
                            row.child(5).text().trim(),
                            Double.parseDouble(credits),
                            row.child(7).text().trim(),
                            nullIfTBA(row.child(16 - o).text().trim()),
                            row.child(19 - o).text().trim(),
                            Integer.parseInt(row.child(10 - o).text().trim()),
                            Integer.parseInt(row.child(11 - o).text().trim()),
                            Integer.parseInt(row.child(12 - o).text().trim()),
                            Integer.parseInt(row.child(13 - o).text().trim()),
                            Integer.parseInt(row.child(14 - o).text().trim()),
                            Integer.parseInt(row.child(15 - o).text().trim()));
                }
                String[] times = (o == 1 || row.child(8).text().trim().equals("TBA") ||
                        row.child(9).text().trim().equals("TBA")) ? null : row.child(9).text().trim().split("-");
                String[] dates = row.child(17 - o).text().trim().equals("-") ? null :
                        row.child(17 - o).text().trim().split("-");
                slots.add(new Course.Slot(
                        parseDays(row.child(8).text().trim()),
                        nullIfTBA(row.child(18 - o).text().trim()),
                        times == null ? null : times[0].trim(),
                        times == null ? null : times[1].trim(),
                        dates == null ? null : dates[0].trim(),
                        dates == null ? null : dates[1].trim()));
            }
        }
        if (section != null) {
            section.slots = slots.toArray(new Course.Slot[0]);
            sections.add(section);
        }
        return sections.toArray(new Course.Section[0]);
    }

    private static Set<DayOfWeek> parseDays(String days) {
        if (days.equals("TBA")) return null;
        return days.chars().mapToObj(c -> {
            switch (c) {
                case 'M':
                    return DayOfWeek.MONDAY;
                case 'T':
                    return DayOfWeek.TUESDAY;
                case 'W':
                    return DayOfWeek.WEDNESDAY;
                case 'R':
                    return DayOfWeek.THURSDAY;
                case 'F':
                    return DayOfWeek.FRIDAY;
                case 'S':
                    return DayOfWeek.SATURDAY;
                case 'U':
                    return DayOfWeek.SUNDAY;
                default:
                    return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static ArrayList<String> getSubjects(Minerva minerva, int year, TERM term) {
        Document doc = minerva.post("/pban1/bwckgens.p_proc_term_date", new HashMap<String, String>() {{
            put("p_calling_proc", "P_CrseSearch");
            put("search_mode_in", "NON_NT");
            put("p_term", year + term.suffix);
        }});
        Elements subjects = doc.select("select[name=sel_subj] option");
        return subjects.stream().map(option -> option.attr("value")).collect(Collectors.toCollection(ArrayList::new));
    }

    private static String nullIfTBA(String s) {
        return s.equals("TBA") ? null : s;
    }
}
