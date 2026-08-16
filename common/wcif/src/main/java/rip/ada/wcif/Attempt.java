package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Attempt(@JsonProperty("value") ResultValue value,
                      @JsonProperty("reconstruction") String reconstruction) {
}
