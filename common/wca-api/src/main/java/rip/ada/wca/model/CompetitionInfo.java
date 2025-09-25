package rip.ada.wca.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import rip.ada.wcif.CountryCode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompetitionInfo(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("venue") String venue,
        @JsonProperty("start_date") String startDate,
        @JsonProperty("end_date") String endDate,
        @JsonProperty("short_name") String shortName,
        @JsonProperty("short_display_name") String shortDisplayName,
        @JsonProperty("date_range") String dateRange,
        @JsonProperty("delegate") List<Person> delegates,
        @JsonProperty("organizers") List<Person> organizers,
        @JsonProperty("country_iso2") CountryCode country,
        @JsonProperty("city") String city
        ) {
}
