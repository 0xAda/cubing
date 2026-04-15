package rip.ada.groups.scorecards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScorecardPaginator {

    public List<ScorecardPage> getScorecardPages(final ScorecardSet scorecardSet) {
        final List<Scorecard> scorecards = scorecardSet.scorecards().stream().sorted().toList();

        if (scorecards.isEmpty()) {
            return Collections.emptyList();
        }

        final List<ScorecardPage> scorecardPages = new ArrayList<>();
        ScorecardPage currentPage = new ScorecardPage(new ArrayList<>());

        int currentGroup = scorecards.getFirst().group();
        int amountOnPage = 0;

        for (final Scorecard scorecard : scorecards) {
            if (scorecard.group() != currentGroup || amountOnPage == 4) {
                scorecardPages.add(currentPage);
                currentPage = new ScorecardPage(new ArrayList<>());
                amountOnPage = 0;
                currentGroup = scorecard.group();
            }
            currentPage.scorecards().add(scorecard);
            amountOnPage++;
        }
        if (amountOnPage != 0) {
            scorecardPages.add(currentPage);
        }
        return scorecardPages;
    }

}
