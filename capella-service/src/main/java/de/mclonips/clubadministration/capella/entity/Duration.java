package de.mclonips.clubadministration.capella.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Duration {

    FULL("1/1", "L"),
    HALF("1/2", "L"),
    QUARTER("1/4", ""),
    QUAVER("1/8", "s"),
    SEMIQUAVER("1/16", "ss"),
    DEMISEMIQUAVER("1/32", "ss");

    private final String length;
    private final String value;

    public static Duration getByLength(String length) {
        for (final Duration duration : Duration.values()) {
            if (length.split("/").length != 2) {
                length += "/1";
            }

            if (duration.length.equals(length)) {
                return duration;
            }
        }

        throw new IllegalArgumentException(String.format("No Duration with length %s available!", length));
    }
}
