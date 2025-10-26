package rip.ada.links;

import com.fasterxml.jackson.annotation.JsonFormat;
import rip.ada.wcif.event.OfficialEvent;

import java.time.LocalDate;
import java.util.List;

public record Competition(
        String name,
        String id,
        boolean displayIrelandLogo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate,
        List<OfficialEvent> events,
        List<Sponsor> sponsor,
        int lat,
        int lon,
        int competitorLimit,
        int registrations,
        int newcomers
) {

}
