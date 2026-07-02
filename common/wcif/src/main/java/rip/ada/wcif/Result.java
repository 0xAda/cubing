package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Result(
        @JsonProperty("personId") int personId,
        @JsonProperty("ranking") Integer ranking,
        @JsonProperty("attempts") List<Attempt> attempts,
        @JsonProperty("best") ResultValue best,
        @JsonProperty("average") ResultValue average
) {
    public static Result empty(final int personId) {
        return new Result(personId, null, List.of(), new ResultValue(0), new ResultValue(0));
    }
}
