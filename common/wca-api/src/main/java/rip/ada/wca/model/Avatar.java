package rip.ada.wca.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Avatar(@JsonProperty("thumb_url") String thumbnailUrl) {
}
