package rip.ada.wca.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Person(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("wca_id") String wcaId,
        @JsonProperty("avatar") Avatar avatar
) {
}
