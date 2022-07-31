package nl.vu.group2.kittens.model;

public enum Card {

    ATTACK,
    BEARD_CAT,
    CATTERMELON,
    DEFUSE,
    EXPLODING_KITTEN,
    FAVOR,
    HAIRY_POTATO_CAT,
    NOPE,
    RAINBOW_RALPHING_CAT,
    SEE_THE_FUTURE,
    SHUFFLE,
    SKIP,
    TACOCAT;

    public static Card of(String name) {
        final String enumName = name.replace(" ", "_").toUpperCase();
        return Card.valueOf(enumName);
    }
}
