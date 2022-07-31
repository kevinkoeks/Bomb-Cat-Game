package nl.vu.group2.kittens.engine.effect;

import lombok.Value;
import nl.vu.group2.kittens.model.Card;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Value
public class CardCombo {

    List<Card> cards;

    public Optional<ComboEffect> getEffect() {
        switch (cards.size()) {
            case 1:
                return getEffectForSingleCard();
            case 2:
                return getEffectForTwoCards();
            case 3:
                return getEffectForThreeCards();
            case 5:
                return getEffectForFiveCards();
            default:
                return Optional.empty();
        }
    }

    private Optional<ComboEffect> getEffectForSingleCard() {
        final ComboEffect effect;
        switch (cards.get(0)) {
            case ATTACK:
                effect = new AttackEffect();
                break;
            case FAVOR:
                effect = new FavorEffect();
                break;
            case SEE_THE_FUTURE:
                effect = new SeeTheFutureEffect();
                break;
            case SKIP:
                effect = new SkipEffect();
                break;
            case SHUFFLE:
                effect = new ShuffleEffect();
                break;
            default:
                effect = null;
        }
        return Optional.ofNullable(effect);
    }

    private Optional<ComboEffect> getEffectForTwoCards() {
        return createIfHolds(countDistinctCards() == 1, TwoCardsEffect::new);
    }

    private Optional<ComboEffect> getEffectForThreeCards() {
        return createIfHolds(countDistinctCards() == 1, ThreeCardsEffect::new);
    }

    private Optional<ComboEffect> getEffectForFiveCards() {
        return createIfHolds(countDistinctCards() == 5, FiveCardsEffect::new);
    }

    // this method might not be the nicest among all methods, but it's here to have readable code and less repetition above
    private static Optional<ComboEffect> createIfHolds(boolean condition, Supplier<ComboEffect> effectSupplier) {
        if (condition) {
            return Optional.ofNullable(effectSupplier.get());
        }
        return Optional.empty();
    }

    private long countDistinctCards() {
        return cards.stream().distinct().count();
    }
}
