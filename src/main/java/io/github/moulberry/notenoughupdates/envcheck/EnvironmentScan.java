package io.github.moulberry.notenoughupdates.envcheck;

import javax.swing.*;

public class EnvironmentScan {

    static boolean shouldCheckOnce = true;

    public static void checkEnvironmentOnce() {
        if (shouldCheckOnce) checkEnvironment();
    }

    static void checkEnvironment() {
        shouldCheckOnce = false;
        // Skip Forge checks for Fabric
    }

    public static void checkForgeEnvironment() {
        // Legacy
    }

    public static void showErrorMessage(String... messages) {
        String message = String.join("\n", messages);
        System.setProperty("java.awt.headless", "false");
        JOptionPane.showMessageDialog(
                null, message, "NotEnoughUpdates - Problematic System Configuration", JOptionPane.ERROR_MESSAGE
        );
    }
}
