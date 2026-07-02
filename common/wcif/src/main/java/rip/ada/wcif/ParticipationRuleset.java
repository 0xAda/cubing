package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParticipationRuleset(
        @JsonProperty("participationSource") ParticipationSource participationSource,
        @JsonProperty("reservedPlaces") ReservedPlaces reservedPlaces
) {
}
