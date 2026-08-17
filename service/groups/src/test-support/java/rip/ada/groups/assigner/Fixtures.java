package rip.ada.groups.assigner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import rip.ada.wcif.Competition;

import java.io.IOException;

public class Fixtures {

    public static Competition getCompetition(final String name) {
        try {
            final String s = new String(Fixtures.class.getClassLoader().getResourceAsStream(name + ".json").readAllBytes());
            final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
            return objectMapper.readValue(s, Competition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
