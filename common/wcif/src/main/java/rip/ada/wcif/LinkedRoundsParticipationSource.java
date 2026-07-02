package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LinkedRoundsParticipationSource(
        @JsonProperty("roundIds") List<ActivityCode> roundIds,
        @JsonProperty("resultCondition") ResultCondition resultCondition
) implements ParticipationSource {
}
