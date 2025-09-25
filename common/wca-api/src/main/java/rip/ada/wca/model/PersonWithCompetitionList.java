package rip.ada.wca.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PersonWithCompetitionList(
        @JsonProperty("user") Person person,
        @JsonProperty("upcoming_competitions") List<CompetitionInfo> upcomingCompetitions,
        @JsonProperty("ongoing_competitions") List<CompetitionInfo> ongoingCompetitions
) {
}
