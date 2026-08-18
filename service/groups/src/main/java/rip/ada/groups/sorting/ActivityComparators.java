package rip.ada.groups.sorting;

import rip.ada.wcif.Activity;

import java.util.Comparator;

public class ActivityComparators {

    public static Comparator<Activity> START_TIME = Comparator.comparingLong(a -> a.getStartTime().toEpochMilli());

}
