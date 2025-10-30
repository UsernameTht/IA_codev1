import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YuGiOhSwingGame extends JFrame {

    private enum Phase { MAIN, BATTLE, END }

    private final JTextArea logArea = new JTextArea(12, 30);
    private final JLabel lpLabel1 = new JLabel();
    private final JLabel lpLabel2 = new JLabel();
    private final JLabel phaseLabel = new JLabel("Phase: MAIN");
    private final JLabel turnLabel = new JLabel("Turn: Player 1");

    private final JButton nextPhaseButton = new JButton("Next Phase");
    private final JButton nextTurnButton = new JButton("Next Turn");
    private final JButton summonButton = new JButton("Summon");
    private final JButton setButton = new JButton("Set");
    private final JComboBox<String> summonBox = new JComboBox<>();

    private final List<JLabel> player1MonsterLabels = new ArrayList<>();
    private final List<JLabel> player2MonsterLabels = new ArrayList<>();

    private final Map<String, ImageIcon> iconCache = new HashMap<>();

    private final Player player1 = new Player("Player 1", 4000);
    private final Player player2 = new Player("Player 2", 4000);

    private boolean player1Turn = true;
    private Phase currentPhase = Phase.MAIN;
    private boolean summonUsed = false;
    private boolean duelStarted = false;
    private int selectedAttackerIndex = -1;
    private boolean firstTurnOfGame = true;

    public YuGiOhSwingGame() {
        setTitle("Yu-Gi-Oh! Duel Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLayout(new BorderLayout());

        setupLogArea();
        JPanel selectPanel = buildSelectPanel();
        JPanel infoPanel = buildInfoPanel();
        JPanel buttonPanel = buildButtonPanel();
        JPanel cardPanel = buildCardPanel();

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(infoPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(selectPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        add(new JScrollPane(logArea), BorderLayout.WEST);
        add(southPanel, BorderLayout.SOUTH);

        updateLPLabels();
        refreshField();
        refreshHandComboBox();
        updateSummonButtons();

        setVisible(true);
    }

    private void setupLogArea() {
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
    }

    private JPanel buildSelectPanel() {
        JPanel selectPanel = new JPanel();
        JButton startButton = new JButton("Start Duel");
        selectPanel.add(new JLabel("Choose Monster:"));
        summonBox.setPrototypeDisplayValue("Blue-Eyes White Dragon");
        selectPanel.add(summonBox);
        selectPanel.add(summonButton);
        selectPanel.add(setButton);
        selectPanel.add(startButton);

        startButton.addActionListener(e -> {
            log("The duel begins!");
            duelStarted = true;
            startButton.setEnabled(false);
            nextPhaseButton.setEnabled(true);
            nextTurnButton.setEnabled(true);
            updateSummonButtons();
            refreshHandComboBox();
        });

        summonButton.addActionListener(e -> handleSummonOrSet(false));
        setButton.addActionListener(e -> handleSummonOrSet(true));

        return selectPanel;
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(nextPhaseButton);
        buttonPanel.add(nextTurnButton);
        nextPhaseButton.setEnabled(false);
        nextTurnButton.setEnabled(false);

        nextPhaseButton.addActionListener(e -> switchPhase());
        nextTurnButton.addActionListener(e -> switchTurn());
        return buttonPanel;
    }

    private JPanel buildInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(2, 2));

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
        return infoPanel;
    }

    private JPanel buildCardPanel() {
        JPanel cardPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JPanel player1Zone = new JPanel(new GridLayout(1, 5, 10, 10));
        JPanel player2Zone = new JPanel(new GridLayout(1, 5, 10, 10));
        player1Zone.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        player2Zone.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 5; i++) {
            JLabel p1Slot = createMonsterSlotLabel();
            JLabel p2Slot = createMonsterSlotLabel();
            int index = i;
            registerMonsterSlotListener(p1Slot, true, index);
            registerMonsterSlotListener(p2Slot, false, index);
            player1MonsterLabels.add(p1Slot);
            player2MonsterLabels.add(p2Slot);
            player1Zone.add(p1Slot);
            player2Zone.add(p2Slot);
        }

        cardPanel.add(player2Zone);
        cardPanel.add(player1Zone);
        return cardPanel;
    }

    private JLabel createMonsterSlotLabel() {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(180, 220));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        return label;
    }

    private void registerMonsterSlotListener(JLabel label, boolean belongsToPlayer1, int index) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMonsterSlotClick(belongsToPlayer1, index);
            }
        });
    }

    private void handleMonsterSlotClick(boolean belongsToPlayer1, int index) {
        if (!duelStarted || currentPhase != Phase.BATTLE) {
            return;
        }

        Player activePlayer = player1Turn ? player1 : player2;
        Player opponent = player1Turn ? player2 : player1;
        Player zoneOwner = belongsToPlayer1 ? player1 : player2;

        if (zoneOwner == activePlayer) {
            if (index >= activePlayer.getField().size()) {
                return;
            }
            Player.MonsterSlot attacker = activePlayer.getField().get(index);
            if (attacker.isSet() || attacker.hasAttacked()) {
                log("You can't select this monster for attack.");
                return;
            }
            selectedAttackerIndex = index;
            log("Selected " + attacker.getMonster().getName() + " to attack.");
        } else if (zoneOwner == opponent) {
            if (selectedAttackerIndex == -1) {
                return;
            }
            if (selectedAttackerIndex >= activePlayer.getField().size()) {
                selectedAttackerIndex = -1;
                return;
            }
            Player.MonsterSlot attackerSlot = activePlayer.getField().get(selectedAttackerIndex);
            Player.MonsterSlot defenderSlot = index < opponent.getField().size()
                    ? opponent.getField().get(index)
                    : null;
            performAttack(attackerSlot, defenderSlot, activePlayer, opponent);
            selectedAttackerIndex = -1;
            refreshField();
        }
    }

    private void handleSummonOrSet(boolean set) {
        if (!duelStarted || currentPhase != Phase.MAIN || summonUsed) {
            log(!duelStarted ? "Start the duel first." : summonUsed ? "You can only summon/set once per turn." : "Wrong phase.");
            return;
        }

        Player currentPlayer = player1Turn ? player1 : player2;
        if (!currentPlayer.hasCardsInHand()) {
            log("No monsters in hand.");
            return;
        }

        int selectedIndex = summonBox.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentPlayer.getHand().size()) {
            log("Select a monster from your hand first.");
            return;
        }

        Player.MonsterSlot slot = currentPlayer.summonFromHand(selectedIndex, set);
        log(currentPlayer.getName() + (set ? " sets a card." : " summons " + slot.getMonster().getName() + "."));
        summonUsed = true;
        refreshField();
        refreshHandComboBox();
        updateSummonButtons();
    }

    private void switchTurn() {
        if (!duelStarted) {
            return;
        }
        boolean wasPlayer1Turn = player1Turn;
        player1Turn = !player1Turn;
        Player currentPlayer = player1Turn ? player1 : player2;
        Player opponent = player1Turn ? player2 : player1;
        currentPhase = Phase.MAIN;
        selectedAttackerIndex = -1;
        summonUsed = false;
        nextPhaseButton.setEnabled(true);

        if (!drawCardFor(currentPlayer, opponent)) {
            return;
        }

        currentPlayer.resetMonstersForNewTurn();
        log("It's now " + currentPlayer.getName() + "'s turn.");
        turnLabel.setText("Turn: " + currentPlayer.getName());
        phaseLabel.setText("Phase: MAIN");
        firstTurnOfGame = wasPlayer1Turn ? false : firstTurnOfGame;
        refreshField();
        refreshHandComboBox();
        updateSummonButtons();
    }

    private boolean drawCardFor(Player player, Player opponent) {
        if (!player.hasCardsInDeck()) {
            player.takeDamage(player.getLifePoints());
            updateLPLabels();
            log(player.getName() + " has no cards to draw and loses the duel!");
            if (opponent != null) {
                log(opponent.getName() + " wins the duel!");
            }
            disableAllButtons();
            return false;
        }
        Monster drawn = player.drawCard();
        if (drawn != null) {
            log(player.getName() + " draws " + drawn.getName() + ".");
        }
        return true;
    }

    private void performAttack(Player.MonsterSlot attackerSlot, Player.MonsterSlot defenderSlot, Player attacker, Player defender) {
        if (attackerSlot == null || attackerSlot.hasAttacked()) {
            return;
        }
        if (!attacker.getField().contains(attackerSlot)) {
            return;
        }

        Monster atkMonster = attackerSlot.getMonster();
        log(attacker.getName() + "'s " + atkMonster.getName() + " attacks!");
        attackerSlot.markAttacked();

        if (defenderSlot == null) {
            defender.takeDamage(atkMonster.getAttackPoints());
            log("Direct attack! " + defender.getName() + " takes " + atkMonster.getAttackPoints() + " damage!");
        } else {
            Monster defMonster = defenderSlot.getMonster();
            if (defenderSlot.isSet()) {
                defenderSlot.reveal();
                log(defender.getName() + "'s set monster is attacked!");
                resolveSetMonsterBattle(attacker, defender, attackerSlot, defenderSlot, atkMonster, defMonster);
            } else {
                resolveFaceUpBattle(attacker, defender, attackerSlot, defenderSlot, atkMonster, defMonster);
            }
        }

        concludeBattleStep(attacker, defender);
    }

    private void resolveSetMonsterBattle(Player attacker, Player defender, Player.MonsterSlot attackerSlot,
                                         Player.MonsterSlot defenderSlot, Monster atkMonster, Monster defMonster) {
        int atk = atkMonster.getAttackPoints();
        int def = defMonster.getDefensePoints();
        if (atk > def) {
            defender.destroyMonster(defenderSlot);
            log("Set monster was destroyed! No damage to player.");
        } else if (atk < def) {
            int dmg = def - atk;
            attacker.takeDamage(dmg);
            log("Set monster defends successfully! " + attacker.getName() + " takes " + dmg + " damage!");
        } else {
            log("Attack equals defense. No damage, no destruction.");
        }
    }

    private void resolveFaceUpBattle(Player attacker, Player defender, Player.MonsterSlot attackerSlot,
                                     Player.MonsterSlot defenderSlot, Monster atkMonster, Monster defMonster) {
        int atk = atkMonster.getAttackPoints();
        int def = defMonster.getAttackPoints();
        if (atk > def) {
            int dmg = atk - def;
            defender.takeDamage(dmg);
            defender.destroyMonster(defenderSlot);
            log(defender.getName() + "'s " + defMonster.getName() + " was destroyed and takes " + dmg + " damage!");
        } else if (atk < def) {
            int dmg = def - atk;
            attacker.takeDamage(dmg);
            attacker.destroyMonster(attackerSlot);
            log(attacker.getName() + "'s monster was destroyed and takes " + dmg + " damage!");
        } else {
            attacker.destroyMonster(attackerSlot);
            defender.destroyMonster(defenderSlot);
            log("Both monsters destroyed!");
        }
    }

    private void concludeBattleStep(Player attacker, Player defender) {
        updateLPLabels();
        refreshField();
        if (attacker.isDefeated() && defender.isDefeated()) {
            endDuel("Both players' Life Points hit zero. The duel ends in a draw!");
        } else if (defender.isDefeated()) {
            endDuel(defender.getName() + " has lost the duel!");
        } else if (attacker.isDefeated()) {
            endDuel(attacker.getName() + " has lost the duel!");
        }
    }

    private void switchPhase() {
        switch (currentPhase) {
            case MAIN -> {
                if (firstTurnOfGame && player1Turn) {
                    currentPhase = Phase.END;
                    log("No Battle Phase on the first turn.");
                    nextPhaseButton.setEnabled(false);
                } else {
                    currentPhase = Phase.BATTLE;
                    log("Entered Battle Phase");
                    nextPhaseButton.setEnabled(true);
                }
            }
            case BATTLE -> {
                currentPhase = Phase.END;
                log("Entered End Phase");
                nextPhaseButton.setEnabled(false);
            }
            case END -> log("End Phase complete. Use Next Turn.");
        }
        phaseLabel.setText("Phase: " + currentPhase);
        updateSummonButtons();
    }

    private void refreshHandComboBox() {
        summonBox.removeAllItems();
        Player current = player1Turn ? player1 : player2;
        for (Monster m : current.getHand()) {
            summonBox.addItem(m.getName());
        }
        if (summonBox.getItemCount() > 0) {
            summonBox.setSelectedIndex(0);
        }
    }

    private void updateSummonButtons() {
        Player current = player1Turn ? player1 : player2;
        boolean canSummon = duelStarted && currentPhase == Phase.MAIN && !summonUsed && current.hasCardsInHand();
        summonButton.setEnabled(canSummon);
        setButton.setEnabled(canSummon);
        summonBox.setEnabled(canSummon);
    }

    private void updateLPLabels() {
        lpLabel1.setText(player1.getName() + " LP: " + player1.getLifePoints());
        lpLabel2.setText(player2.getName() + " LP: " + player2.getLifePoints());
    }

    private void refreshField() {
        updateFieldForPlayer(player1MonsterLabels, player1.getField());
        updateFieldForPlayer(player2MonsterLabels, player2.getField());
    }

    private void updateFieldForPlayer(List<JLabel> labels, List<Player.MonsterSlot> slots) {
        for (int i = 0; i < labels.size(); i++) {
            Player.MonsterSlot slot = i < slots.size() ? slots.get(i) : null;
            updateMonsterLabel(labels.get(i), slot);
        }
    }

    private void updateMonsterLabel(JLabel label, Player.MonsterSlot slot) {
        if (slot == null) {
            label.setIcon(null);
            label.setToolTipText("");
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            return;
        }

        if (slot.isSet()) {
            ImageIcon icon = loadCardIcon("/facedown_set.png", true, 150, 220);
            label.setIcon(icon);
            label.setToolTipText("Set Monster");
            label.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            if (icon == null) {
                label.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
        } else {
            Monster monster = slot.getMonster();
            ImageIcon icon = loadCardIcon("/" + monster.getImageFileName(), false, 150, 220);
            label.setIcon(icon);
            label.setToolTipText("ATK: " + monster.getAttackPoints() + " / DEF: " + monster.getDefensePoints());
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            if (icon == null) {
                label.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
        }
    }

    private ImageIcon loadCardIcon(String resourcePath, boolean rotate90, int width, int height) {
        String cacheKey = resourcePath + "|" + rotate90 + "|" + width + "x" + height;
        if (iconCache.containsKey(cacheKey)) {
            return iconCache.get(cacheKey);
        }

        try {
            BufferedImage image = ImageIO.read(getClass().getResource(resourcePath));
            if (image == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            if (rotate90) {
                int w = image.getWidth();
                int h = image.getHeight();
                BufferedImage rotated = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = rotated.createGraphics();
                applyHighQualityRendering(g2);
                g2.translate(h / 2.0, w / 2.0);
                g2.rotate(Math.toRadians(90));
                g2.translate(-w / 2.0, -h / 2.0);
                g2.drawImage(image, 0, 0, null);
                g2.dispose();
                image = rotated;
            }

            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            applyHighQualityRendering(g2d);
            g2d.drawImage(image, 0, 0, width, height, null);
            g2d.dispose();

            ImageIcon icon = new ImageIcon(scaled);
            iconCache.put(cacheKey, icon);
            return icon;
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void applyHighQualityRendering(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void log(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void endDuel(String message) {
        log(message);
        disableAllButtons();
    }

    private void disableAllButtons() {
        nextPhaseButton.setEnabled(false);
        nextTurnButton.setEnabled(false);
        summonButton.setEnabled(false);
        setButton.setEnabled(false);
        summonBox.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(YuGiOhSwingGame::new);
    }
}
