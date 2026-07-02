package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PercentResultCondition(
        @JsonProperty("scope") ResultType scope,
        @JsonProperty("value") int value
) implements ResultCondition {
}
