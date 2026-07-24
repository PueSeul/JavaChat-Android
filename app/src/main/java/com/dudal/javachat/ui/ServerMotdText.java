package com.dudal.javachat.ui;

/** Normalizes server-provided MOTD spacing without discarding Minecraft formatting. */
public final class ServerMotdText {
    private ServerMotdText() {}

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String[] lines = raw.replace("\r\n", "\n")
                .replace('\r', '\n')
                .split("\n", -1);
        StringBuilder result = new StringBuilder(raw.length());
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            if (lineIndex > 0) {
                result.append('\n');
            }
            String line = lines[lineIndex];
            StringBuilder leadingFormatting = new StringBuilder();
            int index = 0;
            while (index < line.length()) {
                char current = line.charAt(index);
                if (Character.isWhitespace(current)) {
                    index++;
                } else if (current == '\u00A7' && index + 1 < line.length()) {
                    leadingFormatting.append(current).append(line.charAt(index + 1));
                    index += 2;
                } else {
                    break;
                }
            }
            result.append(leadingFormatting)
                    .append(line.substring(index).stripTrailing());
        }
        return result.toString().strip();
    }
}
