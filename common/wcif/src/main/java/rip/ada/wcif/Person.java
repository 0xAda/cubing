package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import rip.ada.wcif.event.OfficialEvent;

import java.time.LocalDate;
import java.util.List;

public record Person(
        @JsonProperty("registrantId") Integer registrantId,
        @JsonProperty("name") String name,
        @JsonProperty("wcaUserId") int wcaUserId,
        @JsonProperty("wcaId") String wcaId,
        @JsonProperty("countryIso2") CountryCode country,
        @JsonProperty("gender") Gender gender,
        @JsonProperty("birthdate") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") @JsonInclude(JsonInclude.Include.NON_NULL) LocalDate birthdate,
        @JsonProperty("email") @JsonInclude(JsonInclude.Include.NON_NULL) String email,
        @JsonProperty("avatar") Avatar avatar,
        @JsonProperty("roles") List<Role> roles,
        @JsonProperty("registration") Registration registration,
        @JsonProperty("assignments") List<Assignment> assignments,
        @JsonProperty("personalBests") List<PersonalBest> personalBests,
        @JsonProperty("extensions") List<Extension> extensions
) {

    public ResultValue getPersonalBest(final OfficialEvent event, final ResultType resultType) {
        for (final PersonalBest personalBest : personalBests) {
            if (personalBest.type() == resultType && personalBest.event() == event) {
                return personalBest.value();
            }
        }
        return ResultValue.DNF;
    }

}
