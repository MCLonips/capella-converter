package de.mclonips.clubadministration.capella.entity.type;

public enum ElementValue {

    D5("1 b 7"),
    D5C("1 b 6"),
    E5("1 b 5"),
    F5("1 2 3 4 6 7"),
    F5C("1 2 3 5 6 7"),
    G5("1 2 3"),
    G5C("1 2 4 5 6 7"),
    A5("1 2"),
    A5C("1 3 4 5 6"),
    B5("1"),
    B5C("2 3 "),
    C6("2 3"),
    C6C("4 5 6"),
    D6("2 b 7"),
    D6C("2 b 6"),
    E6("1 b 5"),
    F6("1 2 3 4 6"),
    F6C("1 2 3 5 6"),
    G6("1 2 3"),
    G6C("1 2 4"),
    A6("1 2"),
    A6C("1 3"),
    B6("1"),
    B6C("2 4 5 6"),
    C7("2 4 5 6"),
    C7C("2 3 4"),
    D7("2 3"),
    D7C("2 3 5 6"),
    E7("1 2 5 6"),
    F7("1 2 4"),
    F7C("1 2 4 5 6 7"),
    G7("1 3 5 6 7"),
    G7C("3 6"),
    A7("2 b 7"),
    A7C("1 2 4 7"),
    B7("1 2"),
    REPETITION(":"),
    NONE("");

    private final String value;

    ElementValue(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
