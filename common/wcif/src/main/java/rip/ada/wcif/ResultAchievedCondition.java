package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultAchievedCondition(
        @JsonProperty("scope") ResultType scope,
        @JsonProperty("value") ResultValue value
) implements ResultCondition {
}
