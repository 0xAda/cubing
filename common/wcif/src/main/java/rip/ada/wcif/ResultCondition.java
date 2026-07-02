package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes(
        value = {
                @JsonSubTypes.Type(name = "resultAchieved", value = ResultAchievedCondition.class),
                @JsonSubTypes.Type(name = "ranking", value = RankingResultCondition.class),
                @JsonSubTypes.Type(name = "percent", value = PercentResultCondition.class)
        }
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface ResultCondition {

    ResultType scope();
}
