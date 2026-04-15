package rip.ada.groups.wca.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Me(
        @JsonProperty("me") Person me
) {
}
