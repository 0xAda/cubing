package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes(
        value = {
                @JsonSubTypes.Type(name = "registrations", value = RegistrationsParticipationSource.class),
                @JsonSubTypes.Type(name = "round", value = RoundParticipationSource.class),
                @JsonSubTypes.Type(name = "linkedRounds", value = LinkedRoundsParticipationSource.class)
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface ParticipationSource {
}
