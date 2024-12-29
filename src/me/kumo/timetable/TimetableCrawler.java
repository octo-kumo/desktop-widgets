package me.kumo.timetable;

import me.kumo.Minerva;
import me.kumo.Widgets;
import me.kumo.utils.Tuple;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;

import static java.util.stream.IntStream.range;

public class TimetableCrawler {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private static final Map<String, Timetable> TIMETABLES = new HashMap<>();

    public static Timetable getSchedule(Minerva minerva) {
        return getSchedule(minerva, 0, true);
    }

    public static Timetable getSchedule(Minerva minerva, int offset, boolean useCache) {
        LocalDate now = LocalDate.now();
        while (now.getDayOfWeek() != DayOfWeek.MONDAY) now = now.minusDays(1);
        now = now.plusDays(offset * 7L);
        String date = now.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        if (useCache && TIMETABLES.containsKey(date)) return TIMETABLES.get(date);
        if (useCache && Widgets.cache.get("timetable-" + date, null) != null) {
            TIMETABLES.put(date, Timetable.fromString(Widgets.cache.get("timetable-" + date, null)));
            return TIMETABLES.get(date);
        }
        if (minerva == null) return null;
        Document document = minerva.get("/pban1/bwskfshd.P_CrseSchd?start_date_in=" + date);
        Timetable.Class[][] classes = transpose_clean(document.select("table[summary='This layout table is used to present the weekly course schedule.'] tr")
                .stream().map(tr -> tr.select("th, td")).reduce(new ArrayList<List<Element>>(), (acc, row) -> {
                    if (!acc.isEmpty() && acc.get(acc.size() - 1).stream().anyMatch(td -> td.hasAttr("rowspan"))) {
                        AtomicInteger index = new AtomicInteger(0);
                        acc.get(acc.size() - 1).stream()
                                .map(td -> new Tuple<>(index.getAndIncrement(), Integer.parseInt("0" + td.attr("rowspan")), td))
                                .filter(it -> it.b() > 1)
                                .forEach(it -> row.add(it.a(), new Element("td").attr("rowspan", String.valueOf(it.b() - 1))));
                    }
                    acc.add(row);
                    return acc;
                }, (a, b) -> a).stream().map(tr -> tr.stream().map(el -> {
                    Element child = el.firstElementChild();
                    if (child != null) {
                        List<TextNode> nodes = child.textNodes();
                        String[] timeStrings = nodes.get(2).toString().split("-");
                        return new Timetable.Class(
                                nodes.get(0).toString().trim(),
                                nodes.get(3).toString().trim(),
                                parseTime(timeStrings[0].trim()),
                                parseTime(timeStrings[1].trim()));
                    } else return null;
                }).toArray(Timetable.Class[]::new)).toArray(Timetable.Class[][]::new));
        if (classes.length != 7 || Arrays.stream(classes).allMatch(d -> d.length == 0)) classes = null;
        Timetable _instance = new Timetable(date, classes);
        Widgets.cache.put("timetable-" + date, _instance.toString());
        TIMETABLES.put(date, _instance);
        return _instance;
    }

    private static Timetable.Class[][] transpose_clean(Timetable.Class[][] array) {
        return range(0, array[0].length).parallel().mapToObj(r -> range(0, array.length).parallel()
                        .mapToObj(c -> array[c][r]).toArray(Timetable.Class[]::new))
                .map(a -> Arrays.stream(a).filter(Objects::nonNull)
                        .toArray(Timetable.Class[]::new)).skip(1).limit(7).toArray(Timetable.Class[][]::new);
    }

    private static long parseTime(String time) {
        try {
            return LocalTime.parse(time.toLowerCase(), TIME_FORMATTER).toSecondOfDay();
        } catch (Exception e) {
            return LocalTime.parse(time.toUpperCase(), TIME_FORMATTER).toSecondOfDay();
        }
    }

    public static void clearCache() {
        TIMETABLES.clear();
        try {
            Widgets.cache.clear();
        } catch (BackingStoreException ignored) {
        }
    }
}
