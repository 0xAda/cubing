package rip.ada.links.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.Header;
import org.jetbrains.annotations.NotNull;
import rip.ada.links.Competition;
import rip.ada.links.Competitions;

import java.util.List;

public class APIListHandler implements Handler {

    private final Competitions competitions;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public APIListHandler(final Competitions competitions) {
        this.competitions = competitions;
    }

    @Override
    public void handle(@NotNull final Context ctx) throws Exception {
        final List<Competition> sponsoredComps = competitions.getAll();
        ctx.res().setHeader(Header.CONTENT_TYPE, ContentType.JSON);
        objectMapper.writeValue(ctx.res().getWriter(), sponsoredComps);
    }
}
