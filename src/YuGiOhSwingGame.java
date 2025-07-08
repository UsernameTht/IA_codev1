// imports remain unchanged
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.nio.file.*;

public class YuGiOhSwingGame extends JFrame {

    private enum Phase { MAIN, BATTLE, END }

    private Player player1 = new Player("Player 1", 4000);
    private Player player2 = new Player("Player 2", 4000);
    private boolean player1Turn = true;
    private Phase currentPhase = Phase.MAIN;
    private boolean attackUsed = false;
    private boolean summonUsed = false;
    private boolean duelStarted = false;

    private JTextArea logArea = new JTextArea(12, 30);
    private JLabel lpLabel1 = new JLabel();
    private JLabel lpLabel2 = new JLabel();
    private JLabel phaseLabel = new JLabel("Phase: MAIN");
    private JLabel turnLabel = new JLabel("Turn: Player 1");

    private JButton nextPhaseButton = new JButton("Next Phase");
    private JButton nextTurnButton = new JButton("Next Turn");

    private JComboBox<String> summonBox;
    private JButton summonButton = new JButton("Summon");
    private JButton setButton = new JButton("Set");

    private JLabel player1CardImage = new JLabel();
    private JLabel player2CardImage = new JLabel();

    private final java.util.List<Monster> monsterList = MonsterFactory.getStarterMonsters();

    public YuGiOhSwingGame() {
        setTitle("Yu-Gi-Oh! Duel Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLayout(new BorderLayout());

        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JPanel selectPanel = new JPanel();
        summonBox = new JComboBox<>();
        for (Monster m : monsterList) {
            summonBox.addItem(m.getName());
        }
        JButton startButton = new JButton("Start Duel");
        selectPanel.add(new JLabel("Choose Monster:"));
        selectPanel.add(summonBox);
        selectPanel.add(summonButton);
        selectPanel.add(setButton);
        selectPanel.add(startButton);

        JPanel infoPanel = new JPanel();
        updateLPLabels();
        infoPanel.add(lpLabel1);
        infoPanel.add(lpLabel2);
        infoPanel.add(phaseLabel);
        infoPanel.add(turnLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(nextPhaseButton);
        buttonPanel.add(nextTurnButton);
        nextPhaseButton.setEnabled(false);
        nextTurnButton.setEnabled(false);
        summonButton.setEnabled(false);
        setButton.setEnabled(false);

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new GridLayout(1, 2, 40, 10));
        cardPanel.add(player1CardImage);
        cardPanel.add(player2CardImage);

        add(selectPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.WEST);
        add(infoPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.PAGE_END);

        startButton.addActionListener(e -> {
            log("The duel begins!");
            duelStarted = true;
            nextPhaseButton.setEnabled(true);
            nextTurnButton.setEnabled(true);
            summonButton.setEnabled(true);
            setButton.setEnabled(true);
            startButton.setEnabled(false);
        });

        summonButton.addActionListener(e -> {
            if (!duelStarted) {
                log("Start the duel first.");
                return;
            }
            if (currentPhase != Phase.MAIN) {
                log("You can only summon during the Main Phase.");
                return;
            }
            if (summonUsed) {
                log("You can only summon/set one monster per turn.");
                return;
            }
            Monster m = monsterList.get(summonBox.getSelectedIndex());
            Player currentPlayer = player1Turn ? player1 : player2;
            currentPlayer.setMonster(m);
            log(currentPlayer.getName() + " summons " + m.getName());
            setCardImage(player1Turn ? player1CardImage : player2CardImage, m);
            summonUsed = true;
        });

        setButton.addActionListener(e -> {
            if (!duelStarted) {
                log("Start the duel first.");
                return;
            }
            if (currentPhase != Phase.MAIN) {
                log("You can only set during the Main Phase.");
                return;
            }
            if (summonUsed) {
                log("You can only summon/set one monster per turn.");
                return;
            }
            Monster m = monsterList.get(summonBox.getSelectedIndex());
            Player currentPlayer = player1Turn ? player1 : player2;
            currentPlayer.setMonster(m);
            log(currentPlayer.getName() + " sets a card.");
            JLabel label = player1Turn ? player1CardImage : player2CardImage;
            label.setIcon(new ImageIcon(new BufferedImage(150, 220, BufferedImage.TYPE_INT_ARGB)));
            label.setToolTipText("Set Monster");
            summonUsed = true;
        });

        nextPhaseButton.addActionListener(e -> switchPhase());
        nextTurnButton.addActionListener(e -> switchTurn());

        player1CardImage.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (player1Turn && duelStarted && currentPhase == Phase.BATTLE && !attackUsed && player1.getMonster() != null && player2.getMonster() != null) {
                    performAttack(player1, player2);
                }
            }
        });

        player2CardImage.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!player1Turn && duelStarted && currentPhase == Phase.BATTLE && !attackUsed && player2.getMonster() != null && player1.getMonster() != null) {
                    performAttack(player2, player1);
                }
            }
        });

        setVisible(true);
    }

    private void switchTurn() {
        player1Turn = !player1Turn;
        currentPhase = Phase.MAIN;
        attackUsed = false;
        summonUsed = false;
        log("It's now " + (player1Turn ? player1.getName() : player2.getName()) + "'s turn.");
        turnLabel.setText("Turn: " + (player1Turn ? player1.getName() : player2.getName()));
        phaseLabel.setText("Phase: MAIN");
    }


    private void performAttack(Player attacker, Player defender) {
        Monster attackingMonster = attacker.getMonster();
        Monster defendingMonster = defender.getMonster();

        log(attacker.getName() + "'s " + attackingMonster.getName() + " attacks!");

        if (attackingMonster.getAttackPoints() > defendingMonster.getDefensePoints()) {
            int damage = attackingMonster.getAttackPoints() - defendingMonster.getDefensePoints();
            defender.takeDamage(damage);
            log(defender.getName() + " takes " + damage + " damage!");
        } else {
            log(defendingMonster.getName() + " defends successfully!");
        }

        updateLPLabels();
        attackUsed = true;

        if (defender.isDefeated()) {
            log(defender.getName() + " has lost the duel!");
            nextPhaseButton.setEnabled(false);
            nextTurnButton.setEnabled(false);
            summonButton.setEnabled(false);
            setButton.setEnabled(false);
        }
    }

    private void switchPhase() {
        switch (currentPhase) {
            case MAIN -> {
                currentPhase = Phase.BATTLE;
                log("Entered Battle Phase");
            }
            case BATTLE -> {
                currentPhase = Phase.END;
                log("Entered End Phase");
            }
            case END -> {
                log("End Phase complete. Use Next Turn.");
            }
        }
        phaseLabel.setText("Phase: " + currentPhase);
    }


    private void updateLPLabels() {
        lpLabel1.setText(player1.getName() + " LP: " + player1.getLifePoints());
        lpLabel2.setText(player2.getName() + " LP: " + player2.getLifePoints());
    }

    private void log(String text) {
        logArea.append(text + "\n");
    }

    private void setCardImage(JLabel label, Monster monster) {
        ImageIcon icon = new ImageIcon(monster.getImageFullPath());
        Image scaledImage = icon.getImage().getScaledInstance(150, 220, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(scaledImage));
        label.setToolTipText("ATK: " + monster.getAttackPoints() + " / DEF: " + monster.getDefensePoints());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(YuGiOhSwingGame::new);
    }

    static class Player {
        private String name;
        private int lifePoints;
        private Monster monster;

        public Player(String name, int lifePoints) {
            this.name = name;
            this.lifePoints = lifePoints;
        }

        public String getName() {
            return name;
        }

        public int getLifePoints() {
            return lifePoints;
        }

        public void takeDamage(int damage) {
            this.lifePoints = Math.max(0, lifePoints - damage);
        }

        public boolean isDefeated() {
            return lifePoints <= 0;
        }

        public void setMonster(Monster monster) {
            this.monster = monster;
        }

        public Monster getMonster() {
            return monster;
        }
    }

    static class Monster {
        private String name;
        private int attackPoints;
        private int defensePoints;

        public Monster(String name, int attackPoints, int defensePoints) {
            this.name = name;
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

        public String getImageFullPath() {
            return "C:\\Users\\ryans\\IdeaProjects\\IA_codev1\\Resources\\" + name.toLowerCase().replace(" ", "_") + ".png";
        }
    }

    public static class MonsterFactory {
        public static java.util.List<Monster> getStarterMonsters() {
            java.util.List<Monster> list = new java.util.ArrayList<>();
            list.add(new Monster("Dark Magician", 2500, 2100));
            list.add(new Monster("Blue-Eyes White Dragon", 3000, 2500));
            list.add(new Monster("Red-Eyes Black Dragon", 2400, 2000));
            list.add(new Monster("Summoned Skull", 2500, 1200));
            return list;
        }
    }
}




