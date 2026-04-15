package rip.ada.groups.wcalive;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GraphQLQuery(@JsonProperty("query") String query,
                           @JsonProperty("operationName") Object operationName) {
}
