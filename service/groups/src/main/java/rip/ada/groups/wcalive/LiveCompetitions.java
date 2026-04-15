package rip.ada.groups.wcalive;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LiveCompetitions<T>(@JsonProperty("competitions") List<T> competitions) {
}
