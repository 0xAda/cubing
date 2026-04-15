package rip.ada.groups.templates;

import java.util.StringJoiner;

public class Joiner {

    public static String join(final String... strings) {
        if (strings.length == 0) {
            return "";
        }
        if (strings.length == 1) {
            return strings[0];
        }
        final StringJoiner stringJoiner = new StringJoiner(", ");
        for (int i = 0; i < strings.length - 1; i++) {
            stringJoiner.add(strings[i]);
        }
        return stringJoiner + " and " + strings[strings.length - 1];
    }

}
