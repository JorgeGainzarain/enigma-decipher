package es.usj.crypto.enigma;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class AlphabetCircle extends JPanel {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Map<Character, Set<Character>> connections;
    private final Set<Set<Character>> closedLoops;

    public AlphabetCircle(Map<Character, Set<Character>> connections) {
        this.connections = connections;
        this.closedLoops = findClosedLoops();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - 40;
        int centerX = width / 2;
        int centerY = height / 2;

        Map<Character, Point> positions = new HashMap<>();
        double angleStep = 2 * Math.PI / ALPHABET.length();

        for (int i = 0; i < ALPHABET.length(); i++) {
            char letter = ALPHABET.charAt(i);
            double angle = i * angleStep;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            positions.put(letter, new Point(x, y));
            g2d.drawString(String.valueOf(letter), x - 5, y + 5);
        }

        for (Map.Entry<Character, Set<Character>> entry : connections.entrySet()) {
            char letter = entry.getKey();
            Point p1 = positions.get(letter);
            for (char connectedLetter : entry.getValue()) {
                Point p2 = positions.get(connectedLetter);
                if (isPartOfClosedLoop(letter, connectedLetter)) {
                    g2d.setColor(Color.RED);
                } else {
                    g2d.setColor(Color.BLACK);
                }
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private Set<Set<Character>> findClosedLoops() {
        Set<Set<Character>> closedLoops = new HashSet<>();
        for (char start : connections.keySet()) {
            findClosedLoop(start, start, new HashSet<>(), new LinkedHashSet<>(), closedLoops);
        }
        return closedLoops;
    }

    private void findClosedLoop(char start, char current, Set<Character> visited, Set<Character> path, Set<Set<Character>> closedLoops) {
        if (visited.contains(current)) {
            if (current == start && path.size() >= 3) {
                closedLoops.add(new HashSet<>(path));
            }
            return;
        }
        visited.add(current);
        path.add(current);
        for (char next : connections.get(current)) {
            findClosedLoop(start, next, visited, path, closedLoops);
        }
        visited.remove(current);
        path.remove(current);
    }

    private boolean isPartOfClosedLoop(char letter1, char letter2) {
        for (Set<Character> loop : closedLoops) {
            if (loop.contains(letter1) && loop.contains(letter2)) {
                return true;
            }
        }
        return false;
    }
}