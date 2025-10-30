import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class Player {
    private final String name;
    private int lifePoints;
    private final List<MonsterSlot> field = new ArrayList<>();
    private final List<Monster> hand = new ArrayList<>();
    private final Deque<Monster> deck = new ArrayDeque<>();

    public Player(String name, int lifePoints) {
        this(name, lifePoints, MonsterStats.getStarterMonsters());
    }

    public Player(String name, int lifePoints, List<Monster> deckList) {
        this.name = name;
        this.lifePoints = lifePoints;
        List<Monster> copy = new ArrayList<>(deckList);
        Collections.shuffle(copy);
        copy.forEach(deck::add);
        drawStartingHand(5);
    }

    private void drawStartingHand(int count) {
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            hand.add(deck.poll());
        }
    }

    public String getName() {
        return name;
    }

    public int getLifePoints() {
        return lifePoints;
    }

    public void takeDamage(int damage) {
        lifePoints = Math.max(0, lifePoints - Math.max(0, damage));
    }

    public boolean isDefeated() {
        return lifePoints <= 0;
    }

    public Monster drawCard() {
        Monster drawn = deck.poll();
        if (drawn != null) {
            hand.add(drawn);
        }
        return drawn;
    }

    public boolean hasCardsInDeck() {
        return !deck.isEmpty();
    }

    public MonsterSlot summonFromHand(int handIndex, boolean set) {
        Monster monster = hand.remove(handIndex);
        MonsterSlot slot = new MonsterSlot(monster, set);
        field.add(slot);
        return slot;
    }

    public void destroyMonster(MonsterSlot slot) {
        field.remove(slot);
    }

    public void resetMonstersForNewTurn() {
        field.forEach(slot -> slot.setHasAttacked(false));
    }

    public List<MonsterSlot> getField() {
        return Collections.unmodifiableList(field);
    }

    public List<Monster> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public boolean hasCardsInHand() {
        return !hand.isEmpty();
    }

    public static class MonsterSlot {
        private final Monster monster;
        private boolean set;
        private boolean hasAttacked;

        private MonsterSlot(Monster monster, boolean set) {
            this.monster = monster;
            this.set = set;
        }

        public Monster getMonster() {
            return monster;
        }

        public boolean isSet() {
            return set;
        }

        public void reveal() {
            set = false;
        }

        public boolean hasAttacked() {
            return hasAttacked;
        }

        public void markAttacked() {
            hasAttacked = true;
        }

        public void setHasAttacked(boolean value) {
            hasAttacked = value;
        }
    }
}
