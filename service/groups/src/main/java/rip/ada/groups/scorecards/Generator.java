package rip.ada.groups.scorecards;

import rip.ada.wcif.*;
import rip.ada.wcif.event.OfficialEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Generator {

    public ScorecardSet generateScorecards(final Competition competition, final EventType eventType, final int roundNumber, final List<String> roomFilter) {
        for (final Event event : competition.getEvents()) {
            if (event.eventType() == eventType) {
                for (final Round round : event.rounds()) {
                    if (round.roundNumber() == roundNumber) {
                        return generatedScorecardsForRound(competition, event, round, roomFilter);
                    }
                }
            }
        }
        throw new RuntimeException("Failed to find round");
    }

    private ScorecardSet generatedScorecardsForRound(final Competition competition, final Event event, final Round round, final List<String> roomFilter) {
        final List<Scorecard> scorecards = new ArrayList<>();

        for (final Person person : competition.getPersons()) {
            for (final Assignment assignment : person.assignments()) {
                for (final Venue venue : competition.getSchedule().getVenues()) {
                    for (final Room room : venue.getRooms()) {
                        if (!roomFilter.isEmpty() && !roomFilter.contains(room.name())) {
                            continue;
                        }
                        for (final Activity activity : room.activities()) {
                            for (final Activity childActivity : activity.getChildActivities()) {
                                if (childActivity.getId() == assignment.activityId() &&
                                        assignment.assignmentCode().equals(StandardAssignmentCode.COMPETITOR) &&
                                        childActivity.getActivityCode().event() == round.event() &&
                                        childActivity.getActivityCode().round().equals(round.roundNumber())) {
                                    scorecards.add(new Scorecard(
                                            competition.getName(),
                                            person.registrantId(),
                                            person.wcaId() != null ? person.wcaId() : "New competitor",
                                            person.name(),
                                            getRankingInRound(competition, event, round.roundNumber() - 1, person.registrantId()),
                                            round.event(),
                                            round.roundNumber(),
                                            childActivity.getActivityCode().group(),
                                            round.cutoff(),
                                            round.timeLimit(),
                                            round.format(),
                                            needsDoubleCheck(person, event.eventType(), competition, round),
                                            Objects.equals(person.wcaId(), "2022KILG02") || Objects.equals(person.wcaId(), "2020LONG05"),
                                            assignment.stationNumber()
                                    ));
                                }
                            }
                        }
                    }
                }
            }
        }
        return new ScorecardSet(scorecards);
    }

    private int getRankingInRound(final Competition competition, final Event event, final int roundNumber, final int personId) {
        if (roundNumber == 0) {
            final List<Person> rankings = competition.getPersonsRegisteredForEvent(event.eventType()).stream()
                    .filter(person ->
                            person.personalBests().stream()
                                    .anyMatch(personalBest ->
                                            personalBest.type() == (event.eventType().getPreferredRoundFormat().getSortBy().equals("average") ? ResultType.AVERAGE : ResultType.SINGLE) &&
                                                    personalBest.event() == event.eventType() &&
                                                    personalBest.best().isSuccess()))
                    .sorted(Comparator.comparingInt(person -> person.personalBests().stream().filter(personalBest ->
                            personalBest.type() == (event.eventType().getPreferredRoundFormat().getSortBy().equals("average") ? ResultType.AVERAGE : ResultType.SINGLE) &&
                                    personalBest.event() == event.eventType()
                    ).map(personalBest -> personalBest.best().value()).findFirst().orElse(Integer.MAX_VALUE))).toList();
            final int ranking = rankings.indexOf(competition.getPersonById(personId));
            if (ranking == -1) {
                return rankings.size();
            }
            return ranking + 1;
        }
        for (final Round round : event.rounds()) {
            if (round.roundNumber() == roundNumber) {
                for (final Result result : round.results()) {
                    if (result.personId() == personId) {
                        return result.ranking();
                    }
                }
            }
        }
        return 0;
    }

    private boolean needsDoubleCheck(final Person person, final EventType eventType, final Competition competition, final Round round) {
        if (eventType == OfficialEvent.SIX_BY_SIX || eventType == OfficialEvent.SEVEN_BY_SEVEN || eventType == OfficialEvent.MEGAMINX) {
            return false;
        }

        if (round.advancementCondition() == null && competition.getName().contains("Championship")) { //TODO: Grab this info from somewhere else, maybe configurable
            return true;
        }
        for (final PersonalBest personalBest : person.personalBests()) {
            if (personalBest.event() == eventType && personalBest.type() == (eventType == OfficialEvent.MULTI_BLIND ? ResultType.SINGLE : ResultType.AVERAGE)) {
                if ((personalBest.worldRanking() <= 75 || personalBest.nationalRanking() <= 4) && (personalBest.nationalRanking() != 0 && personalBest.worldRanking() != 0) && personalBest.worldRanking() <= 1000) {
                    return true;
                }
            }
        }
        if (person.country() != CountryCode.GB) {
            return false;
        }
        int nrRank = 0;
        switch (eventType) {
            case OfficialEvent.FOUR_BY_FOUR, OfficialEvent.SKEWB -> nrRank = 7;
            case OfficialEvent.FIVE_BY_FIVE -> nrRank = 6;
            case OfficialEvent.ONE_HANDED -> nrRank = 5;
            case OfficialEvent.CLOCK -> nrRank = 10;
            default -> {
            }
        }
        for (final PersonalBest personalBest : person.personalBests()) {
            if (personalBest.event() == eventType && personalBest.type() == (eventType == OfficialEvent.MULTI_BLIND ? ResultType.SINGLE : ResultType.AVERAGE)) {
                if ((personalBest.worldRanking() <= 75 || personalBest.nationalRanking() <= nrRank) && (personalBest.nationalRanking() != 0 && personalBest.worldRanking() != 0) && personalBest.worldRanking() <= 1000) {
                    return true;
                }
            }
        }
        return false;
    }

}
