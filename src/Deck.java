import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Deck {
    private final LinkedList<Monster> cards;

    public Deck() {
        this.cards = new LinkedList<>(MonsterStats.getStarterMonsters());
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public Monster drawCard() {
        if (cards.isEmpty()) return null;
        return cards.poll();
    }

    public void addCard(Monster monster) {
        cards.add(monster);
    }

    public List<Monster> getCards() {
        return Collections.unmodifiableList(cards);
    }
}
