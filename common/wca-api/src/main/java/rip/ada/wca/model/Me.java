package rip.ada.wca.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Me(
        @JsonProperty("me") Person me
) {
}
