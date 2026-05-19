import game.Game;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        int w = askSize("Insert world width  (20-40):", 20);
        int h = askSize("Insert world height (20-40):", 20);
        if (w < 20 || w > 40) w = 20;
        if (h < 20 || h > 40) h = 20;

        // Choose world type
        String[] types  = {"Grid World (square)", "Hex World (hexagonal)"};
        int typeChoice  = JOptionPane.showOptionDialog(
                null, "Choose world type:", "World Type",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, types, types[0]);
        boolean useHex = (typeChoice == 1);

        final int width = w, height = h;
        SwingUtilities.invokeLater(() -> new Game(width, height, useHex).setVisible(true));
    }

    private static int askSize(String prompt, int def) {
        String s = JOptionPane.showInputDialog(null, prompt, String.valueOf(def));
        if (s == null) return def;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}