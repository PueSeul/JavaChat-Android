package com.dudal.javachat.protocol;

final class ChatInput {
    private ChatInput() {
    }

    /**
     * Minecraft chat packets do not accept line breaks or control characters.
     * IMEs and clipboard paste can still put them into a visually single-line
     * Android input, so normalize them immediately before packet creation.
     */
    static String normalize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder normalized = new StringBuilder(input.length());
        boolean lastWasReplacement = false;
        for (int index = 0; index < input.length(); index++) {
            char value = input.charAt(index);
            boolean unsafe = Character.isISOControl(value)
                    || value == '\u0085'
                    || value == '\u2028'
                    || value == '\u2029';
            if (unsafe) {
                if (!lastWasReplacement && normalized.length() > 0
                        && normalized.charAt(normalized.length() - 1) != ' ') {
                    normalized.append(' ');
                }
                lastWasReplacement = true;
            } else {
                normalized.append(value);
                lastWasReplacement = false;
            }
        }
        return normalized.toString().trim();
    }
}
