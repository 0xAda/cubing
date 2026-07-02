package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import rip.ada.wcif.event.OfficialEvent;

import java.util.List;

public record Event(
        @JsonProperty("id") OfficialEvent eventType,
        @JsonProperty("rounds") List<Round> rounds,
        @JsonProperty("competitorLimit") @JsonInclude(JsonInclude.Include.NON_NULL) Integer competitorLimit,
        @JsonProperty("qualification") Qualification qualification,
        @JsonProperty("extensions") List<Extension> extensions
) {

    public ResultCondition resultConditionAdvancingFrom(final Round round) {
        for (final Round other : rounds) {
            final ResultCondition resultCondition = resultConditionDrawingFrom(other, round);
            if (resultCondition != null) {
                return resultCondition;
            }
        }
        return null;
    }

    public boolean isFinalRound(final Round round) {
        return resultConditionAdvancingFrom(round) == null;
    }

    private static ResultCondition resultConditionDrawingFrom(final Round destination, final Round source) {
        final ParticipationRuleset ruleset = destination.participationRuleset();
        if (ruleset == null) {
            return null;
        }
        final ParticipationSource participationSource = ruleset.participationSource();
        if (participationSource instanceof RoundParticipationSource(
                ActivityCode roundId, ResultCondition resultCondition
        )
                && roundId.equals(source.activityCode())) {
            return resultCondition;
        }
        if (participationSource instanceof LinkedRoundsParticipationSource(
                List<ActivityCode> roundIds, ResultCondition resultCondition
        )
                && roundIds.contains(source.activityCode())) {
            return resultCondition;
        }
        return null;
    }
}
