package rip.ada.groups.session;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import rip.ada.wca.AuthenticatedWcaApi;
import rip.ada.wca.OauthSession;
import rip.ada.wca.model.CompetitionInfo;
import rip.ada.wca.model.Person;
import rip.ada.wcif.Competition;

import java.time.Instant;
import java.util.*;

public class Session {

    private final UUID sessionToken;
    private final OauthSession oauthSession;
    private final Instant created = Instant.now();
    private final Map<String, CachedCompetition> cachedCompetitions = Collections.synchronizedMap(new HashMap<>());
    private Person person;
    private List<CompetitionInfo> competitions;
    private GoogleCredential googleCredential;

    public Session(final UUID sessionToken, final OauthSession oauthSession) {
        this.sessionToken = sessionToken;
        this.oauthSession = oauthSession;
    }

    public OauthSession getWcaSession() {
        return oauthSession;
    }

    public UUID getSessionToken() {
        return sessionToken;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(final Person person) {
        this.person = person;
    }

    public List<CompetitionInfo> getCompetitions() {
        return competitions;
    }

    public void setCompetitions(final List<CompetitionInfo> competitions) {
        this.competitions = competitions;
    }

    public Competition getCachedCompetition(final String competitionId) {
        final CachedCompetition cachedCompetition = cachedCompetitions.get(competitionId);
        if (cachedCompetition == null) {
            return null;
        }
        if (cachedCompetition.retrieved.isBefore(Instant.now().minusSeconds(300))) {
            cachedCompetitions.remove(competitionId);
            return null;
        }
        return cachedCompetition.competition();
    }

    public boolean isCompetitionCachedAfter(final String competitionId, final Instant instant) {
        final CachedCompetition cachedCompetition = cachedCompetitions.get(competitionId);
        return cachedCompetition != null && cachedCompetition.retrieved.isAfter(instant);
    }

    public void cacheCompetition(final String competitionId, final Competition competition) {
        cachedCompetitions.put(competitionId, new CachedCompetition(competition, Instant.now()));
    }

    public Competition getStaleCachedCompetition(final String competitionId, final AuthenticatedWcaApi wcaApi) {
        final CachedCompetition cachedCompetition = cachedCompetitions.get(competitionId);
        if (cachedCompetition == null) {
            cacheCompetition(competitionId, wcaApi.getCompetition(oauthSession, competitionId));
        }
        return cachedCompetitions.get(competitionId).competition();
    }

    public void clearCompetitionCache() {
        cachedCompetitions.clear();
    }

    public GoogleCredential getGoogleCredential() {
        return googleCredential;
    }

    public void setGoogleCredential(final GoogleCredential googleCredential) {
        this.googleCredential = googleCredential;
    }

    public void clearInvalidCachedCompetitions() {
        cachedCompetitions.entrySet().removeIf(entry -> entry.getValue().retrieved.isBefore(Instant.now().minusSeconds(300)));
    }

    private record CachedCompetition(Competition competition, Instant retrieved) {
    }
}
