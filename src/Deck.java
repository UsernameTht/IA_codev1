import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Deck {
    private LinkedList<YuGiOhSwingGame.Monster> cards;

    public Deck() {
        this.cards = new LinkedList<>(YuGiOhSwingGame.MonsterStats.getStarterMonsters());
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

    public YuGiOhSwingGame.Monster drawCard() {
        if (cards.isEmpty()) return null;
        return cards.poll();
    }

    public void addCard(YuGiOhSwingGame.Monster monster) {
        cards.add(monster);
    }

    public List<YuGiOhSwingGame.Monster> getCards() {
        return Collections.unmodifiableList(cards);
    }
}
