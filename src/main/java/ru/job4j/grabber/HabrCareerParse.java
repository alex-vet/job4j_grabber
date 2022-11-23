package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final int PAGE_COUNT = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        try {
            Document document = connection.get();
            Elements rows = document.select(".style-ugc");
            return rows.text();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> post = new ArrayList<>();
        for (int i = 1; i <= PAGE_COUNT; i++) {
            Connection connection = Jsoup.connect(String.format("%s%d", link, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.child(0);
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String unparsedDate = dateElement.child(0).attr("datetime");
                String vacancyName = titleElement.text();
                String linkVacancy = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String description = retrieveDescription(linkVacancy);
                LocalDateTime dateTime = dateTimeParser.parse(unparsedDate);
                post.add(new Post(vacancyName, linkVacancy, description, dateTime));
            });
        }
        return post;
    }
}
