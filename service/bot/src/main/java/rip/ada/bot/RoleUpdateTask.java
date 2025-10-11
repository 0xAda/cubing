package rip.ada.bot;

import rip.ada.wca.UnauthenticatedWcaApi;
import rip.ada.wca.model.CompetitionInfo;
import rip.ada.wcif.CountryCode;

import java.util.ArrayList;
import java.util.List;

public class RoleUpdateTask implements Runnable {

    private final UnauthenticatedWcaApi unauthenticatedWcaApi;
    private final ReactionBikeshed reactionBikeshed;

    public RoleUpdateTask(final UnauthenticatedWcaApi unauthenticatedWcaApi, final ReactionBikeshed reactionBikeshed) {
        this.unauthenticatedWcaApi = unauthenticatedWcaApi;
        this.reactionBikeshed = reactionBikeshed;
    }

    @Override
    public void run() {
        try {
            final List<CompetitionInfo> upcomingCompetitionsInCountry = new ArrayList<>(unauthenticatedWcaApi.getUpcomingCompetitionsInCountry(CountryCode.GB));
            upcomingCompetitionsInCountry.addAll(unauthenticatedWcaApi.getUpcomingCompetitionsUsingSearchTerm("FMC Europe 20"));
            final List<CompetitionInfo> maybeFMCWorld = unauthenticatedWcaApi.getUpcomingCompetitionsUsingSearchTerm("FMC 20");
            maybeFMCWorld.removeIf(x -> !x.name().startsWith("FMC 20"));
            upcomingCompetitionsInCountry.addAll(maybeFMCWorld);
            upcomingCompetitionsInCountry.addAll(unauthenticatedWcaApi.getUpcomingCompetitionsUsingSearchTerm("World Championship"));
            upcomingCompetitionsInCountry.addAll(unauthenticatedWcaApi.getUpcomingCompetitionsUsingSearchTerm("European Championship"));
            reactionBikeshed.onCompetitionsUpdate(upcomingCompetitionsInCountry);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
