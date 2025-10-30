import java.util.Objects;

public class Monster {
    private final String name;
    private final int attackPoints;
    private final int defensePoints;

    public Monster(String name, int attackPoints, int defensePoints) {
        this.name = Objects.requireNonNull(name);
        this.attackPoints = attackPoints;
        this.defensePoints = defensePoints;
    }

    public String getName() {
        return name;
    }

    public int getAttackPoints() {
        return attackPoints;
    }

    public int getDefensePoints() {
        return defensePoints;
    }

    public String getImageFileName() {
        return name.toLowerCase().replace(" ", "_") + ".png";
    }
}
