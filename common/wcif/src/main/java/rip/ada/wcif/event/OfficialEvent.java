package rip.ada.wcif.event;

import com.fasterxml.jackson.annotation.JsonValue;
import rip.ada.wcif.EventType;
import rip.ada.wcif.RoundFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum OfficialEvent implements EventType {
    THREE_BY_THREE("333", "3x3x3 Cube", false, RoundFormat.AVERAGE_OF_FIVE),
    TWO_BY_TWO("222", "2x2x2 Cube", false, RoundFormat.AVERAGE_OF_FIVE),
    FOUR_BY_FOUR("444", "4x4x4 Cube", false, RoundFormat.AVERAGE_OF_FIVE),
    FIVE_BY_FIVE("555", "5x5x5 Cube", false, RoundFormat.AVERAGE_OF_FIVE),
    SIX_BY_SIX("666", "6x6x6 Cube", false, RoundFormat.MEAN_OF_THREE),
    SEVEN_BY_SEVEN("777", "7x7x7 Cube", false, RoundFormat.MEAN_OF_THREE),
    THREE_BLIND("333bf", "3x3x3 Blindfolded", false, RoundFormat.BEST_OF_THREE, THREE_BY_THREE),
    FOUR_BLIND("444bf", "4x4x4 Blindfolded", false, RoundFormat.BEST_OF_THREE, FOUR_BY_FOUR),
    FIVE_BLIND("555bf", "5x5x5 Blindfolded", false, RoundFormat.BEST_OF_THREE, FIVE_BY_FIVE),
    ONE_HANDED("333oh", "3x3x3 One-Handed", false, RoundFormat.AVERAGE_OF_FIVE, THREE_BY_THREE),
    CLOCK("clock", "Clock", false, RoundFormat.AVERAGE_OF_FIVE),
    MEGAMINX("minx", "Megaminx", false, RoundFormat.AVERAGE_OF_FIVE),
    PYRAMINX("pyram", "Pyraminx", false, RoundFormat.AVERAGE_OF_FIVE),
    SKEWB("skewb", "Skewb", false, RoundFormat.AVERAGE_OF_FIVE),
    MULTI_BLIND("333mbf", "3x3x3 Multi-Blind", false, RoundFormat.BEST_OF_THREE, THREE_BY_THREE),
    SQUARE_ONE("sq1", "Square-1", false, RoundFormat.AVERAGE_OF_FIVE),
    FMC("333fm", "3x3x3 Fewest Moves", false, RoundFormat.MEAN_OF_THREE, THREE_BY_THREE),
    //Removed events, still need to be able to parse in WCIF
    MULTI_BLIND_OLD_STYLE("333mbo", "3x3x3 Multi-Blind Old Style", true, RoundFormat.AVERAGE_OF_FIVE, THREE_BY_THREE),
    THREE_BY_THREE_WITH_FEET("333ft", "3x3x3 With Feet", true, RoundFormat.AVERAGE_OF_FIVE, THREE_BY_THREE),
    MAGIC("magic", "Magic", true, RoundFormat.AVERAGE_OF_FIVE),
    MASTER_MAGIC("mmagic", "Master Magic", true, RoundFormat.AVERAGE_OF_FIVE);

    private static final Map<String, OfficialEvent> ID_TO_EVENT;
    private final String wcaEventId;
    private final String friendlyName;
    private final boolean removed;
    private final RoundFormat preferredRoundFormat;
    private final OfficialEvent baseEvent;

    static {
        final Map<String, OfficialEvent> map = new HashMap<>();
        for (final OfficialEvent value : values()) {
            map.put(value.wcaEventId, value);
        }
        ID_TO_EVENT = Collections.unmodifiableMap(map);
    }

    OfficialEvent(final String wcaEventId, final String friendlyName, final boolean removed, final RoundFormat preferredRoundFormat, final OfficialEvent baseEvent) {
        this.wcaEventId = wcaEventId;
        this.friendlyName = friendlyName;
        this.removed = removed;
        this.preferredRoundFormat = preferredRoundFormat;
        this.baseEvent = baseEvent;
    }

    OfficialEvent(final String wcaEventId, final String friendlyName, final boolean removed, final RoundFormat preferredRoundFormat) {
        this.wcaEventId = wcaEventId;
        this.friendlyName = friendlyName;
        this.removed = removed;
        this.preferredRoundFormat = preferredRoundFormat;
        this.baseEvent = this;
    }

    @JsonValue
    @Override
    public String getEventId() {
        return wcaEventId;
    }

    @Override
    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public boolean isCompetingEvent() {
        return true;
    }

    @Override
    public boolean isWcaEvent() {
        return true;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }

    public static OfficialEvent fromString(final String wcaEventId) {
        return ID_TO_EVENT.get(wcaEventId);
    }
}
