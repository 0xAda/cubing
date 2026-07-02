package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RoundParticipationSource(
        @JsonProperty("roundId") ActivityCode roundId,
        @JsonProperty("resultCondition") ResultCondition resultCondition
) implements ParticipationSource {
}
