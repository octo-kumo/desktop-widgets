package me.kumo;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Minerva {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private final HashMap<String, String> cookies = new HashMap<>();
    private final int timeout = 5000;

    public static final String origin = "https://horizon.mcgill.ca";

    public Minerva() {
    }

    public Document get(String path) {
        try {
            Connection.Response res = Jsoup.connect(origin + path)
                    .method(Connection.Method.GET)
                    .userAgent(USER_AGENT)
                    .timeout(timeout)
                    .cookies(cookies)
                    .execute();
            cookies.putAll(res.cookies());
            return res.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Document post(String path, Map<String, String> body) {
        try {
            Connection.Response res = Jsoup.connect(origin + path)
                    .method(Connection.Method.POST)
                    .userAgent(USER_AGENT)
                    .timeout(timeout)
                    .cookies(cookies)
                    .data(body)
                    .execute();
            cookies.putAll(res.cookies());
            return res.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean login(String username, String password) {
        cookies.clear();
        get("/pban1/twbkwbis.P_WWWLogin");
        Document doc = post("/pban1/twbkwbis.P_ValLogin", Map.of("sid", username, "PIN", password));
        if (!doc.select("meta[http-equiv=refresh][content*=url=/pban1/twbkwbis.P_GenMenu?name=bmenu.P_MainMnu]").isEmpty()) {
            System.out.println("logged in");
            return true;
        } else return false;
    }
}
