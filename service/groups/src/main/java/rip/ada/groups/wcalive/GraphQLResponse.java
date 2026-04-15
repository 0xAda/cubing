package rip.ada.groups.wcalive;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GraphQLResponse<T>(@JsonProperty("data") T data) {
}
