package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import rip.ada.wcif.event.OfficialEvent;

public record PersonalBest(
        @JsonProperty("eventId") OfficialEvent event,
        @JsonProperty("value") @JsonAlias("best") ResultValue value,
        @JsonProperty("type") ResultType type,
        @JsonProperty("worldRanking") int worldRanking,
        @JsonProperty("continentalRanking") int continentalRanking,
        @JsonProperty("nationalRanking") int nationalRanking
) {
}
