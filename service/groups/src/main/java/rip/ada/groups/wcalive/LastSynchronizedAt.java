package rip.ada.groups.wcalive;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record LastSynchronizedAt(@JsonProperty("wcaId") String wcaId,
                                 @JsonProperty("synchronizedAt") Instant synchronizedAt) {
}
