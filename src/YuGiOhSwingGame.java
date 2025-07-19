import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class YuGiOhSwingGame extends JFrame {

    private enum Phase { MAIN, BATTLE, END }
    private boolean hasFirstBattlePhaseHappened = false;
    private boolean isFirstTurn = true;
    private int selectedAttackerIndex = -1; // -1 = none selected
    private Player player1 = new Player("Player 1", 4000);
    private Player player2 = new Player("Player 2", 4000);
    private boolean player1Turn = true;
    private Phase currentPhase = Phase.MAIN;
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

    private java.util.List<JLabel> player1MonsterLabels = new ArrayList<>();
    private java.util.List<JLabel> player2MonsterLabels = new ArrayList<>();

    private final java.util.List<Monster> monsterList = MonsterStats.getStarterMonsters();

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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(nextPhaseButton);
        buttonPanel.add(nextTurnButton);
        nextPhaseButton.setEnabled(false);
        nextTurnButton.setEnabled(false);
        summonButton.setEnabled(false);
        setButton.setEnabled(false);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 2));

        lpLabel1.setFont(new Font("Arial", Font.BOLD, 18));
        lpLabel1.setForeground(Color.RED);
        lpLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        lpLabel1.setBorder(BorderFactory.createTitledBorder("Player 1"));

        lpLabel2.setFont(new Font("Arial", Font.BOLD, 18));
        lpLabel2.setForeground(Color.BLUE);
        lpLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        lpLabel2.setBorder(BorderFactory.createTitledBorder("Player 2"));

        phaseLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        turnLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        infoPanel.add(lpLabel1);
        infoPanel.add(lpLabel2);
        infoPanel.add(phaseLabel);
        infoPanel.add(turnLabel);
        updateLPLabels();

        JPanel cardPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel player1Zone = new JPanel(new GridLayout(1, 5, 10, 10));
        JPanel player2Zone = new JPanel(new GridLayout(1, 5, 10, 10));
        player1Zone.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        player2Zone.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        for (int i = 0; i < 5; i++) {
            JLabel p1Slot = new JLabel();
            JLabel p2Slot = new JLabel();
            p1Slot.setPreferredSize(new Dimension(180, 220));
            p1Slot.setHorizontalAlignment(SwingConstants.CENTER);
            p1Slot.setVerticalAlignment(SwingConstants.CENTER);

            p2Slot.setPreferredSize(new Dimension(180, 220));
            p2Slot.setHorizontalAlignment(SwingConstants.CENTER);
            p2Slot.setVerticalAlignment(SwingConstants.CENTER);

            player1MonsterLabels.add(p1Slot);
            player2MonsterLabels.add(p2Slot);
            player1Zone.add(p1Slot);
            player2Zone.add(p2Slot);

            final int index = i;

// Player 1 monster slot
            p1Slot.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (!duelStarted || currentPhase != Phase.BATTLE) return;

                    if (player1Turn) {
                        if (index >= player1.getField().size()) return;
                        Player.MonsterSlot attacker = player1.getField().get(index);
                        if (attacker.isSet() || attacker.hasAttacked()) {
                            log("You can't select this monster for attack.");
                            return;
                        }
                        selectedAttackerIndex = index;
                        log("Selected " + attacker.getMonster().getName() + " to attack.");
                    } else {
                        if (selectedAttackerIndex == -1) return;
                        Player.MonsterSlot attacker = player2.getField().get(selectedAttackerIndex);
                        Player.MonsterSlot defender = index < player1.getField().size()
                                ? player1.getField().get(index)
                                : null;
                        performAttack(attacker, defender, player2, player1);
                        selectedAttackerIndex = -1;
                        refreshField();
                    }
                }
            });

// Player 2 monster slot
            p2Slot.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (!duelStarted || currentPhase != Phase.BATTLE) return;

                    if (!player1Turn) {
                        if (index >= player2.getField().size()) return;
                        Player.MonsterSlot attacker = player2.getField().get(index);
                        if (attacker.isSet() || attacker.hasAttacked()) {
                            log("You can't select this monster for attack.");
                            return;
                        }
                        selectedAttackerIndex = index;
                        log("Selected " + attacker.getMonster().getName() + " to attack.");
                    } else {
                        if (selectedAttackerIndex == -1) return;
                        Player.MonsterSlot attacker = player1.getField().get(selectedAttackerIndex);
                        Player.MonsterSlot defender = index < player2.getField().size()
                                ? player2.getField().get(index)
                                : null;
                        performAttack(attacker, defender, player1, player2);
                        selectedAttackerIndex = -1;
                        refreshField();
                    }
                }
            });

        }


        cardPanel.add(player2Zone);
        cardPanel.add(player1Zone);


        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(infoPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(selectPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.WEST);
        add(southPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> {
            log("The duel begins!");
            duelStarted = true;
            nextPhaseButton.setEnabled(true);
            nextTurnButton.setEnabled(true);
            summonButton.setEnabled(true);
            setButton.setEnabled(true);
            startButton.setEnabled(false);
            refreshHandComboBox(); //  Populate hand combo at game start
        });


        summonButton.addActionListener(e -> {
            Player currentPlayer = player1Turn ? player1 : player2;
            if (!duelStarted || currentPhase != Phase.MAIN || summonUsed || currentPlayer.getHand().isEmpty()) {
                log(!duelStarted ? "Start the duel first." : summonUsed ? "You can only summon/set once per turn." : "No monsters in hand.");
                return;
            }

            Monster m = currentPlayer.getHand().remove(summonBox.getSelectedIndex());
            currentPlayer.summon(m, false);
            log(currentPlayer.getName() + " summons " + m.getName());
            summonUsed = true;
            refreshField();
            refreshHandComboBox();
        });


        setButton.addActionListener(e -> {
            Player currentPlayer = player1Turn ? player1 : player2;
            if (!duelStarted || currentPhase != Phase.MAIN || summonUsed || currentPlayer.getHand().isEmpty()) {
                log(!duelStarted ? "Start the duel first." : summonUsed ? "You can only summon/set once per turn." : "No monsters in hand.");
                return;
            }

            Monster m = currentPlayer.getHand().remove(summonBox.getSelectedIndex());
            currentPlayer.summon(m, true);
            log(currentPlayer.getName() + " sets a card.");
            summonUsed = true;
            refreshField();
            refreshHandComboBox();
        });


        nextPhaseButton.addActionListener(e -> switchPhase());
        nextTurnButton.addActionListener(e -> switchTurn());


        setVisible(true);
    }

    private void setCardFacedown(JLabel label) {
        try {
            BufferedImage original = ImageIO.read(getClass().getResource("/facedown_set.png"));
            if (original == null) throw new IOException("facedown_set.png not found in resources.");

            int w = original.getWidth();
            int h = original.getHeight();

            // Rotate the image 90° clockwise (to landscape)
            BufferedImage rotated = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rotated.createGraphics();

            // Transparent background
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, h, w);
            g2.setComposite(AlphaComposite.SrcOver);

            // Rotate
            g2.translate(h / 2.0, w / 2.0);
            g2.rotate(Math.toRadians(90)); // Clockwise
            g2.drawImage(original, -w / 2, -h / 2, null);
            g2.dispose();

            // Now scale the rotated image
            Image scaled = rotated.getScaledInstance(220, 150, Image.SCALE_SMOOTH); // Landscape
            label.setIcon(new ImageIcon(scaled));
            label.setToolTipText("Set Monster");

        } catch (IOException e) {
            e.printStackTrace();
            label.setIcon(null);
        }
    }




    private void switchTurn() {
        player1Turn = !player1Turn;
        currentPhase = Phase.MAIN;
        selectedAttackerIndex = -1;
        summonUsed = false;

        Player currentPlayer = player1Turn ? player1 : player2;

        // Draw logic with deck-out loss
        if (isFirstTurn && !player1Turn) {
            if (currentPlayer.getDeck().isEmpty()) {
                log(currentPlayer.getName() + " has no cards to draw and loses the duel!");
                currentPlayer.takeDamage(currentPlayer.getLifePoints());
                updateLPLabels();
                disableAllButtons();
                return;
            }
            currentPlayer.getHand().add(currentPlayer.getDeck().remove(0));
            log(currentPlayer.getName() + " draws an additional card.");
            isFirstTurn = false;
        } else {
            if (currentPlayer.getDeck().isEmpty()) {
                log(currentPlayer.getName() + " has no cards to draw and loses the duel!");
                currentPlayer.takeDamage(currentPlayer.getLifePoints());
                updateLPLabels();
                disableAllButtons();
                return;
            }
            Monster drawn = currentPlayer.getDeck().remove(0);
            currentPlayer.getHand().add(drawn);
            log(currentPlayer.getName() + " draws " + drawn.getName());
        }

        nextPhaseButton.setEnabled(true);
        nextTurnButton.setEnabled(true);
        summonButton.setEnabled(true);
        setButton.setEnabled(true);

        List<Player.MonsterSlot> field = currentPlayer.getField();
        for (Player.MonsterSlot slot : field) {
            slot.markAttacked(false);
        }

        log("It's now " + currentPlayer.getName() + "'s turn.");
        turnLabel.setText("Turn: " + currentPlayer.getName());
        phaseLabel.setText("Phase: MAIN");
        refreshField();
        refreshHandComboBox();
    }



    private void performAttack(Player.MonsterSlot attackerSlot, Player.MonsterSlot defenderSlot, Player attacker, Player defender) {
        if (attackerSlot == null || attackerSlot.hasAttacked()) return;

        Monster atkMonster = attackerSlot.getMonster();
        log(attacker.getName() + "'s " + atkMonster.getName() + " attacks!");

        if (defenderSlot == null) {
            // Direct attack
            defender.takeDamage(atkMonster.getAttackPoints());
            log("Direct attack! " + defender.getName() + " takes " + atkMonster.getAttackPoints() + " damage!");
        } else {
            Monster defMonster = defenderSlot.getMonster();

            if (defenderSlot.isSet()) {
                defenderSlot.reveal();
                log(defender.getName() + "'s set monster is attacked!");

                int atk = atkMonster.getAttackPoints();
                int def = defMonster.getDefensePoints();

                if (atk > def) {
                    defender.getField().remove(defenderSlot);
                    log("Set monster was destroyed! No damage to player.");
                } else if (atk < def) {
                    int dmg = def - atk;
                    attacker.takeDamage(dmg);
                    log("Set monster defends successfully! " + attacker.getName() + " takes " + dmg + " damage!");
                } else {
                    log("Attack equals defense. No damage, no destruction.");
                }
            } else {
                // Face-up defense: use ATK vs ATK
                int atk = atkMonster.getAttackPoints();
                int def = defMonster.getAttackPoints();

                if (atk > def) {
                    int dmg = atk - def;
                    defender.takeDamage(dmg);
                    defender.getField().remove(defenderSlot);
                    log(defender.getName() + "'s " + defMonster.getName() + " was destroyed and takes " + dmg + " damage!");
                } else if (atk < def) {
                    int dmg = def - atk;
                    attacker.takeDamage(dmg);
                    attacker.getField().remove(attackerSlot);
                    log(attacker.getName() + "'s monster was destroyed and takes " + dmg + " damage!");
                } else {
                    // Equal stats: both destroyed
                    attacker.getField().remove(attackerSlot);
                    defender.getField().remove(defenderSlot);
                    log("Both monsters destroyed!");
                }
            }
        }

        attackerSlot.markAttacked();
        updateLPLabels();
        refreshField();

        if (defender.isDefeated()) {
            log(defender.getName() + " has lost the duel!");
            disableAllButtons();
        }
    }


    private void disableAllButtons() {
        nextPhaseButton.setEnabled(false);
        nextTurnButton.setEnabled(false);
        summonButton.setEnabled(false);
        setButton.setEnabled(false);
    }


    private void switchPhase() {
        switch (currentPhase) {
            case MAIN -> {
                if (isFirstTurn) {
                    currentPhase = Phase.END; // skip battle phase on first turn
                    log("No Battle Phase on the first turn.");
                    nextPhaseButton.setEnabled(false);
                    break;
                }
                currentPhase = Phase.BATTLE;
                log("Entered Battle Phase");
                nextPhaseButton.setEnabled(true);
            }
            case BATTLE -> {
                currentPhase = Phase.END;
                log("Entered End Phase");
                nextPhaseButton.setEnabled(false);
            }
            case END -> {
                log("End Phase complete. Use Next Turn.");
            }
        }
        phaseLabel.setText("Phase: " + currentPhase);
    }

    private void refreshHandComboBox() {
        summonBox.removeAllItems();
        Player current = player1Turn ? player1 : player2;
        for (Monster m : current.getHand()) {
            summonBox.addItem(m.getName());
        }
    }

    private void updateLPLabels() {
        lpLabel1.setText(player1.getName() + " LP: " + player1.getLifePoints());
        lpLabel2.setText(player2.getName() + " LP: " + player2.getLifePoints());
    }

    private void log(String text) {
        logArea.append(text + "\n");
    }

    private void setCardImage(JLabel label, Monster monster) {
        try {
            BufferedImage original = ImageIO.read(getClass().getResource("/" + monster.getImageFileName()));
            Image scaledImage = original.getScaledInstance(150, 220, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(150, 220, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(scaledImage, 0, 0, null);
            g2.dispose();
            label.setIcon(new ImageIcon(resized));
            label.setToolTipText("ATK: " + monster.getAttackPoints() + " / DEF: " + monster.getDefensePoints());
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            label.setIcon(null);
        }
    }






    public static void main(String[] args) {
        SwingUtilities.invokeLater(YuGiOhSwingGame::new);
    }

    public static class Player {
        private String name;
        private int lifePoints;
        private List<MonsterSlot> field = new ArrayList<>();
        private List<Monster> hand = new ArrayList<>();
        private List<Monster> deck = new ArrayList<>();

        public Player(String name, int lifePoints) {
            this.name = name;
            this.lifePoints = lifePoints;
            deck = new ArrayList<>(MonsterStats.getStarterMonsters());
            java.util.Collections.shuffle(deck);
            for (int i = 0; i < 5 && !deck.isEmpty(); i++) {
                hand.add(deck.remove(0));
            }
        }

        public String getName() { return name; }
        public int getLifePoints() { return lifePoints; }
        public void takeDamage(int damage) { this.lifePoints = Math.max(0, lifePoints - damage); }
        public boolean isDefeated() { return lifePoints <= 0; }
        public List<MonsterSlot> getField() { return field; }
        public List<Monster> getHand() { return hand; }
        public List<Monster> getDeck() { return deck; }
        public void summon(Monster m, boolean isSet) { field.add(new MonsterSlot(m, isSet)); }
        public void clearField() { field.clear(); }

        public static class MonsterSlot {
            private Monster monster;
            private boolean isSet;
            private boolean hasAttacked;
            public MonsterSlot(Monster monster, boolean isSet) {
                this.monster = monster;
                this.isSet = isSet;
                this.hasAttacked = false;
            }
            public Monster getMonster() { return monster; }
            public boolean isSet() { return isSet; }
            public void reveal() { isSet = false; }
            public boolean hasAttacked() { return hasAttacked; }
            public void markAttacked() { hasAttacked = true; }
            public void markAttacked(boolean value) { hasAttacked = value; }
        }
    }




    public static class Monster {
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
        //where the image files are located
        public String getImageFullPath() {
            return "\\Resources\\" + name.toLowerCase().replace(" ", "_") + ".png";
        }

        public String getImageFileName() {
            return name.toLowerCase().replace(" ", "_") + ".png";
        }

    }

    //Monster Stats for the cards
    public static class MonsterStats {
        public static java.util.List<Monster> getStarterMonsters() {
            java.util.List<Monster> list = new java.util.ArrayList<>();
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
            return list;
        }
    }

//    private void promptFlipSummon(Player.MonsterSlot slot) {
//        if (!duelStarted || currentPhase != Phase.MAIN || summonUsed || !slot.isSet()) return;
//
//        int choice = JOptionPane.showConfirmDialog(this,
//                "Flip Summon this monster?",
//                "Flip Summon",
//                JOptionPane.YES_NO_OPTION);
//
//        if (choice == JOptionPane.YES_OPTION) {
//            slot.reveal();
//            summonUsed = true;
//            Player current = player1Turn ? player1 : player2;
//            log(current.getName() + " flip summons " + slot.getMonster().getName());
//            refreshField();
//        }
//    }

    private void refreshField() {
        for (int i = 0; i < 5; i++) {
            updateMonsterLabel(player1MonsterLabels.get(i), i < player1.getField().size() ? player1.getField().get(i) : null);
            updateMonsterLabel(player2MonsterLabels.get(i), i < player2.getField().size() ? player2.getField().get(i) : null);
        }
    }

    private void updateMonsterLabel(JLabel label, Player.MonsterSlot slot) {
        if (slot == null) {
            label.setIcon(null);
            label.setToolTipText("");
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // empty slot border
            return;
        }

        Monster monster = slot.getMonster();
        try {
            if (slot.isSet()) {
                BufferedImage original = ImageIO.read(getClass().getResource("/facedown_set.png"));
                int w = original.getWidth(), h = original.getHeight();

                // rotate to landscape
                BufferedImage rotated = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = rotated.createGraphics();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, h, w);
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(h / 2.0, w / 2.0);
                g2.rotate(Math.toRadians(90));
                g2.drawImage(original, -w / 2, -h / 2, null);
                g2.dispose();

                Image scaled = rotated.getScaledInstance(150, 220, Image.SCALE_SMOOTH); // force portrait size
                label.setIcon(new ImageIcon(scaled));
                label.setToolTipText("Set Monster");
                label.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2)); // distinguish set monster
            } else {
                BufferedImage original = ImageIO.read(getClass().getResource("/" + monster.getImageFileName()));
                Image scaledImage = original.getScaledInstance(150, 220, Image.SCALE_SMOOTH);
                BufferedImage resized = new BufferedImage(150, 220, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = resized.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(scaledImage, 0, 0, null);
                g2.dispose();

                label.setIcon(new ImageIcon(resized));
                label.setToolTipText("ATK: " + monster.getAttackPoints() + " / DEF: " + monster.getDefensePoints());
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // border for summoned
            }
        } catch (IOException e) {
            e.printStackTrace();
            label.setIcon(null);
            label.setBorder(BorderFactory.createLineBorder(Color.RED)); // show broken image border
        }
    }


}