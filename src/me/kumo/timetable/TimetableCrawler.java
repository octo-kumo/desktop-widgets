package me.kumo.timetable;

import me.kumo.Minerva;
import me.kumo.Widgets;
import me.kumo.utils.Tuple;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.IntStream.range;

public class TimetableCrawler {
    public static Timetable _instance;
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    static {
        var T = Widgets.prefs.get("timetable", null);
        if (T != null && !T.isBlank()) {
            _instance = Timetable.fromString(T);
        }
    }

    public static Timetable getSchedule(Minerva minerva) {
        if (_instance != null) return _instance;
        Document document = minerva.get("/pban1/bwskfshd.P_CrseSchd");
        Timetable.Class[][] classes = transpose_clean(document.select("table[summary='This layout table is used to present the weekly course schedule.'] tr")
                .stream().map(tr -> tr.select("th, td")).reduce(new ArrayList<List<Element>>(), (acc, row) -> {
                    if (!acc.isEmpty() && acc.getLast().stream().anyMatch(td -> td.hasAttr("rowspan"))) {
                        AtomicInteger index = new AtomicInteger(0);
                        acc.getLast().stream()
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
                                LocalTime.parse(timeStrings[0].trim(), TIME_FORMATTER).toSecondOfDay(),
                                LocalTime.parse(timeStrings[1].trim(), TIME_FORMATTER).toSecondOfDay());
                    } else return null;
                }).toArray(Timetable.Class[]::new)).toArray(Timetable.Class[][]::new));
        if (classes.length != 7) {
            JOptionPane.showMessageDialog(null, "Crawler failed, weekdays=" + classes.length, "Error with Fetch", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (Arrays.stream(classes).allMatch(d -> d.length == 0)) {
            JOptionPane.showMessageDialog(null, "No classes found!", "Error with Fetch", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        _instance = new Timetable(classes);
        Widgets.prefs.put("timetable", _instance.toString());
        return _instance;
    }

    private static Timetable.Class[][] transpose_clean(Timetable.Class[][] array) {
        return range(0, array[0].length).parallel().mapToObj(r -> range(0, array.length).parallel()
                        .mapToObj(c -> array[c][r]).toArray(Timetable.Class[]::new))
                .map(a -> Arrays.stream(a).filter(Objects::nonNull)
                        .toArray(Timetable.Class[]::new)).skip(1).limit(7).toArray(Timetable.Class[][]::new);
    }
}
