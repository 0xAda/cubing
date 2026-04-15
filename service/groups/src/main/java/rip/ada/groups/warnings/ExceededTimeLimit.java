package rip.ada.groups.warnings;

import rip.ada.wcif.ActivityCode;

import java.util.List;

public record ExceededTimeLimit(int personId, int totalTime, int timeLimit,
                                List<ActivityCode> rounds) {
}
