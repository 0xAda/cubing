package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record Qualification(
        @JsonProperty("earliestResultDate") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate earliestResultDate,
        @JsonProperty("latestResultDate") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate latestResultDate,
        @JsonProperty("resultCondition") ResultCondition resultCondition
) {
}
