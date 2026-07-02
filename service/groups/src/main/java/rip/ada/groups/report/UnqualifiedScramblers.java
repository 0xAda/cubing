package rip.ada.groups.report;

import rip.ada.wcif.*;

import java.util.ArrayList;
import java.util.List;

public class UnqualifiedScramblers implements ReportGenerator {
    @Override
    public String getDisplayName() {
        return "Unqualified Scramblers";
    }

    @Override
    public List<Report> generateReport(final Competition competition) {
        final List<List<String>> rows = new ArrayList<>();
        for (final Person person : competition.getPersons()) {
            for (final Assignment assignment : person.assignments()) {
                final ActivityCode activityCode = competition.getActivityById(assignment.activityId()).getActivityCode();
                if (assignment.assignmentCode() != StandardAssignmentCode.SCRAMBLER) {
                    continue;
                }
                if (!isQualified(person, activityCode.event(), competition)) {
                    rows.add(List.of(person.name(), activityCode.toString()));
                }
            }
        }
        return List.of(new Report("Scramblers who don't meet the event qualification requirements", List.of("Name", "Activity"), rows));
    }

    private boolean isQualified(final Person person, final EventType eventType, final Competition competition) {
        for (final Event event : competition.getEvents()) {
            if (!event.eventType().equals(eventType.getBaseEvent())) {
                continue;
            }
            final ResultCondition resultCondition = event.qualification().resultCondition();
            for (final PersonalBest personalBest : person.personalBests()) {
                if (personalBest.event().equals(event.eventType()) && personalBest.type().equals(resultCondition.scope())) {
                    return switch (resultCondition) {
                        case ResultAchievedCondition achieved -> personalBest.value().isSuccess()
                                && (achieved.value() == null || personalBest.value().value() < achieved.value().value());
                        case RankingResultCondition ranking -> personalBest.value().isSuccess()
                                && personalBest.nationalRanking() != 0 && personalBest.worldRanking() < ranking.value();
                        default -> false;
                    };
                }
            }
        }
        return false;
    }
}
