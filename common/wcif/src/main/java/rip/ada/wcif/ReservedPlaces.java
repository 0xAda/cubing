package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReservedPlaces(
        @JsonProperty("nationalities") List<CountryCode> nationalities,
        @JsonProperty("count") int count
) {
}
