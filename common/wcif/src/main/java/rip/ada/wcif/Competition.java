package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public final class Competition {
    private String formatVersion;
    private String id;
    private String name;
    private String shortName;
    private Series series;
    private List<Person> persons;
    private List<Event> events;
    private Schedule schedule;
    private RegistrationInfo registrationInfo;
    private Integer competitorLimit;
    private List<Extension> extensions;

    private volatile Map<Integer, Person> personCache = null;
    private int nextActivityId = -1;

    public Competition(
            final @JsonProperty("formatVersion") String formatVersion,
            final @JsonProperty("id") String id,
            final @JsonProperty("name") String name,
            final @JsonProperty("shortName") String shortName,
            final @JsonProperty("series") Series series,
            final @JsonProperty("persons") List<Person> persons,
            final @JsonProperty("events") List<Event> events,
            final @JsonProperty("schedule") Schedule schedule,
            final @JsonProperty("registrationInfo") RegistrationInfo registrationInfo,
            final @JsonProperty("competitorLimit") Integer competitorLimit,
            final @JsonProperty("extensions") List<Extension> extensions
    ) {
        this.formatVersion = formatVersion;
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.series = series;
        this.persons = persons;
        this.events = events;
        this.schedule = schedule;
        this.registrationInfo = registrationInfo;
        this.competitorLimit = competitorLimit;
        this.extensions = extensions;
    }

    @JsonProperty("formatVersion")
    public String getFormatVersion() {
        return formatVersion;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("shortName")
    public String getShortName() {
        return shortName;
    }

    @JsonProperty("series")
    public Series getSeries() {
        return series;
    }

    @JsonProperty("persons")
    public List<Person> getPersons() {
        return persons;
    }

    @JsonProperty("events")
    public List<Event> getEvents() {
        return events;
    }

    @JsonProperty("schedule")
    public Schedule getSchedule() {
        return schedule;
    }

    @JsonProperty("registrationInfo")
    public RegistrationInfo getRegistrationInfo() {
        return registrationInfo;
    }

    @JsonProperty("competitorLimit")
    public Integer getCompetitorLimit() {
        return competitorLimit;
    }

    @JsonProperty("extensions")
    public List<Extension> getExtensions() {
        return extensions;
    }

    public void setFormatVersion(final String formatVersion) {
        this.formatVersion = formatVersion;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setShortName(final String shortName) {
        this.shortName = shortName;
    }

    public void setSeries(final Series series) {
        this.series = series;
    }

    public void setPersons(final List<Person> persons) {
        this.persons = persons;
    }

    public void setEvents(final List<Event> events) {
        this.events = events;
    }

    public void setSchedule(final Schedule schedule) {
        this.schedule = schedule;
    }

    public void setRegistrationInfo(final RegistrationInfo registrationInfo) {
        this.registrationInfo = registrationInfo;
    }

    public void setCompetitorLimit(final Integer competitorLimit) {
        this.competitorLimit = competitorLimit;
    }

    public void setExtensions(final List<Extension> extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final Competition that = (Competition) obj;
        return Objects.equals(this.formatVersion, that.formatVersion) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.shortName, that.shortName) &&
                Objects.equals(this.series, that.series) &&
                Objects.equals(this.persons, that.persons) &&
                Objects.equals(this.events, that.events) &&
                Objects.equals(this.schedule, that.schedule) &&
                Objects.equals(this.registrationInfo, that.registrationInfo) &&
                Objects.equals(this.competitorLimit, that.competitorLimit) &&
                Objects.equals(this.extensions, that.extensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatVersion, id, name, shortName, series, persons, events, schedule, registrationInfo, competitorLimit, extensions);
    }

    @Override
    public String toString() {
        return "Competition[" +
                "formatVersion=" + formatVersion + ", " +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "shortName=" + shortName + ", " +
                "series=" + series + ", " +
                "persons=" + persons + ", " +
                "events=" + events + ", " +
                "schedule=" + schedule + ", " +
                "registrationInfo=" + registrationInfo + ", " +
                "competitorLimit=" + competitorLimit + ", " +
                "extensions=" + extensions + ']';
    }

    public Activity getActivityById(final int id) {
        for (final Venue venue : schedule.getVenues()) {
            for (final Room room : venue.getRooms()) {
                final Activity activity = getActivityById(id, room.activities());
                if (activity != null) {
                    return activity;
                }
            }
        }
        return null;
    }

    private Activity getActivityById(final int id, final List<Activity> activities) {
        for (Activity activity : activities) {
            if (activity.getId() == id) {
                return activity;
            } else {
                final Activity foundActivity = getActivityById(id, activity.getChildActivities());
                if (foundActivity != null) {
                    return foundActivity;
                }
            }
        }
        return null;
    }

    public rip.ada.wcif.Person getPersonById(final int id) {
        if (personCache == null) {
            synchronized (this) {
                if (personCache == null) {
                    final Map<Integer, rip.ada.wcif.Person> personCache = new HashMap<>();
                    for (final rip.ada.wcif.Person person : persons) {
                        if (person.registrantId() != null) {
                            personCache.put(person.registrantId(), person);
                        }
                    }
                    this.personCache = personCache;
                }
            }
        }
        return personCache.get(id);
    }

    public List<Person> getPersonsRegisteredForEvent(final EventType eventType) {
        final List<Person> persons = new ArrayList<>();
        for (final Person person : this.getPersons()) {
            if (person.registration() == null || person.registration().registrationStatus() != RegistrationStatus.ACCEPTED) {
                continue;
            }

            for (final EventType event : person.registration().events()) {
                if (event.equals(eventType)) {
                    persons.add(person);
                }
            }
        }

        return persons;
    }

    @JsonIgnore
    public List<Person> getCompetingPersons() {
        final List<Person> persons = new ArrayList<>();
        for (final Person person : this.getPersons()) {
            if (person.registration() == null || person.registration().registrationStatus() != RegistrationStatus.ACCEPTED) {
                continue;
            }
            persons.add(person);
        }
        return persons;
    }

    @JsonIgnore
    public int getNextActivityId() {
        if (nextActivityId == -1) {
            int maxActivityId = -1;
            for (final Venue venue : getSchedule().getVenues()) {
                for (final Room room : venue.getRooms()) {
                    maxActivityId = Math.max(maxActivityId, getMaxActivityId(room.activities()));
                }
            }
            nextActivityId = maxActivityId + 1;
        }
        return nextActivityId++;
    }

    private int getMaxActivityId(final List<rip.ada.wcif.Activity> activities) {
        int maxActivityId = -1;
        for (final Activity activity : activities) {
            maxActivityId = Math.max(maxActivityId, activity.getId());
            maxActivityId = Math.max(maxActivityId, getMaxActivityId(activity.getChildActivities()));
        }
        return maxActivityId;
    }
}
