package rip.ada.wcascraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public record ScrapedCompetition(boolean isChampionship, String information, Map<String, String> tabs) {
    public static ScrapedCompetition fromString(final String html) {
        final Document document = Jsoup.parse(html);

        final String information = document
                .getElementsByClass("competition-info").getFirst()
                .getElementsByClass("dl-horizontal").get(2)
                .getElementsByTag("dd").getFirst()
                .html();

        final Map<String, String> friendlyTabNames = new HashMap<>();
        for (final Element tab : document.getElementsByAttributeValue("data-toggle", "tab")) {
            final String wcaTabId = tab.attribute("href").getValue().substring(1);
            final String tabName = tab.text();
            friendlyTabNames.put(wcaTabId, tabName);
        }

        final Map<String, String> tabs = new HashMap<>();
        for (final Element tabElement : document.getElementsByClass("tab-pane")) {
            final String elementId = tabElement.id();
            final String content = tabElement.html();
            tabs.put(friendlyTabNames.get(elementId), content);
        }

        final boolean isChampionship = !document.getElementsByClass("championship-trophy").isEmpty();

        return new ScrapedCompetition(isChampionship, information, tabs);
    }
}
