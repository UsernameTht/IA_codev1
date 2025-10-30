import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MonsterStats {
    private MonsterStats() {
    }

    public static List<Monster> getStarterMonsters() {
        List<Monster> list = new ArrayList<>();
        list.add(new Monster("Dark Magician", 2500, 2100));
        list.add(new Monster("Blue-Eyes White Dragon", 3000, 2500));
        list.add(new Monster("Red-Eyes Black Dragon", 2400, 2000));
        list.add(new Monster("Summoned Skull", 2500, 1200));
        list.add(new Monster("Celtic Guardian", 1400, 1200));
        list.add(new Monster("Kuriboh", 300, 200));
        list.add(new Monster("Mystical Elf", 800, 2000));
        list.add(new Monster("La Jinn", 1800, 1000));
        list.add(new Monster("Battle Ox", 1700, 1000));
        list.add(new Monster("Harpie Lady", 1300, 1400));
        list.add(new Monster("Axe Raider", 1700, 1150));
        list.add(new Monster("Vorse Raider", 1900, 1200));
        return Collections.unmodifiableList(list);
    }
}
