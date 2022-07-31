package nl.vu.group2.kittens;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import nl.vu.group2.kittens.engine.GameRunner;
import nl.vu.group2.kittens.model.Card;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * This test class contains small examples on how to use several libraries.
 * If you're a reviewer, feel free to have a look to better understand how we did some stuff (e.g., Lombok).
 * Still, please ignore it for comments, since this class is intended to be used only in tests.
 */
@Slf4j
class LibrariesDemoTest {

    public static final String ITEM_NAME = "item_name";

    @DisplayName("Lombok and Slf4j demo")
    @Test
    void myFirstTest() {
        final Item item = new Item(ITEM_NAME, true, false);
        Assertions.assertEquals(ITEM_NAME, item.getName());
        Assertions.assertTrue(item.isWearable());
        Assertions.assertFalse(item.isCanPickup());
        // the log methods can be used instead of System.out.println to avoid Sonarlint issues as follows
        log.info("An item printed: {}", item.toString());
    }

    /**
     * lombok's @Value annotation generates the @AllArgsConstructor, all @Getter methods for the fields, and overrides
     * Java standard methods such as {@link Object#toString}, {@link Object#equals(Object)}, {@link Object#hashCode()}.
     * <p>
     * With IntelliJ, after building, you can go at `build/classes/java/test/nl/vu/group2/kittens/LombokDemoTest.class`
     * to see the generated code (decompiled from bytecode).
     */
    @Value
    public static class Item {
        String name;
        boolean wearable;
        boolean canPickup;
    }


    @DisplayName("StringUtils demo")
    @Test
    void whenCalledisBlank_thenCorrect() {
        Assertions.assertTrue(StringUtils.isBlank(" "));
    }


    @DisplayName("FastJSON demo")
    @Test
    void marshal_unmarshal() {
        final var item = new Item(ITEM_NAME, true, false);
        final String marshalledItem = JSON.toJSONString(item);
        log.info("Produced JSON '{}'", marshalledItem);
        Assertions.assertTrue(marshalledItem.startsWith("{"));
        Assertions.assertTrue(marshalledItem.contains("\"name\":\"item_name\""));
        Assertions.assertTrue(marshalledItem.contains("\"wearable\":true"));
        Assertions.assertTrue(marshalledItem.contains("\"canPickup\":false"));
        Assertions.assertEquals(2, StringUtils.countMatches(marshalledItem, ','));
        Assertions.assertTrue(marshalledItem.endsWith("}"));

        final String jsonItem = "{\"canPickup\":true,\"name\":\"another_name\",\"wearable\":false}";
        final Item anotherItem = JSON.parseObject(jsonItem, Item.class);
        log.info("Parsed item from JSON '{}'", anotherItem);
        Assertions.assertEquals("another_name", anotherItem.getName());
        Assertions.assertTrue(anotherItem.isCanPickup());
        Assertions.assertFalse(anotherItem.isWearable());
    }

    /**
     * Showcases several scenarios for {@link GameRunner}'s getCardsForCustomDeck method.
     */
    @DisplayName("Test FastJSON against garbage")
    @Test
    void fastjson_and_garbage() {
        final String validJson = "{\"EXPLODING_KITTEN\": 4}";
        final Map<Card, Integer> validMap = parseToMap(validJson, Card.class, Integer.class);
        log.info("Parsed valid map from JSON '{}'", validMap);
        Assertions.assertNotNull(validMap);

        final String invalidJson = "{\"EXPLODING_KITTEN\": 4}}}}}}";
        Assertions.assertThrows(
                JSONException.class,
                () -> parseToMap(invalidJson, Card.class, Integer.class)
        );

        final String invalidCardJson = "{\"NON_EXISTING_CARD\": 8}";
        final Map<Card, Integer> something = parseToMap(invalidCardJson, Card.class, Integer.class);
        log.info("Parsed something from JSON '{}'", something);
        Assertions.assertTrue(something.containsKey(null));

        final String invalidNumberJson = "{\"DEFUSE\": noooo}";
        Assertions.assertThrows(
                JSONException.class,
                () -> parseToMap(invalidNumberJson, Card.class, Integer.class)
        );
    }

    public static <K, V> Map<K, V> parseToMap(String json, Class<K> keyType, Class<V> valueType) {
        return JSON.parseObject(
                json,
                new TypeReference<>(keyType, valueType) {
                }
        );
    }
}
