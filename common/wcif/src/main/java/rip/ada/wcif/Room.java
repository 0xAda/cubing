package rip.ada.wcif;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public record Room(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("color") String color,
        @JsonProperty("activities") List<Activity> activities,
        @JsonProperty("extensions") List<Extension> extensions
) {
    public Activity getTopLevelActivityByCode(final ActivityCode activityCode) {
        for (final Activity activity : activities) {
            if (activity.getActivityCode().equals(activityCode)) {
                return activity;
            }
        }
        return null;
    }

    public List<Activity> getGroups(final ActivityCode round) {
        final List<Activity> groups = new ArrayList<>();
        getGroupsRecursively(round, groups, activities);
        return groups;
    }

    private void getGroupsRecursively(final ActivityCode round, final List<Activity> groupAccumulator, final List<Activity> activities) {
        for (final Activity activity : activities) {
            final ActivityCode activityCode = activity.getActivityCode();
            final boolean matchesRound = round.attempt() == null
                    ? activityCode.roundOnly().equals(round.roundOnly())
                    : activityCode.withoutGroup().equals(round.withoutGroup());
            if (activityCode.group() != null && matchesRound) {
                groupAccumulator.add(activity);
            }

            getGroupsRecursively(round, groupAccumulator, activity.getChildActivities());
        }
    }
}
