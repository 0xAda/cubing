package rip.ada.links.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.Header;
import rip.ada.links.Competition;
import rip.ada.links.Competitions;
import rip.ada.links.Sponsor;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SponsoredListHandler implements Handler {
    private final Competitions competitions;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public SponsoredListHandler(final Competitions competitions) {
        this.competitions = competitions;
    }

    @Override
    public void handle(final Context ctx) throws IOException {
        final Sponsor sponsor = Sponsor.valueOf(ctx.pathParam("sponsor").toUpperCase(Locale.UK));
        final List<Competition> sponsoredComps = competitions.getSponsoredBy(sponsor);
        ctx.res().setHeader(Header.CONTENT_TYPE, ContentType.JSON);
        objectMapper.writeValue(ctx.res().getWriter(), sponsoredComps);
    }
}
