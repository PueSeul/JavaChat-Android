package com.dudal.javachat.ui;

import com.dudal.javachat.protocol.ChatLine;

/** Builds the compact, Minecraft-style text shown for a chat entry. */
public final class ChatLineText {
    private static final String RESET = "\u00A7r";

    private ChatLineText() {}

    public static String compose(ChatLine line) {
        return switch (line.getKind()) {
            case PLAYER -> playerSender(line) + " " + line.getFormattedMessage();
            case SYSTEM -> systemMessage(line);
            case PRESENCE -> line.getFormattedMessage();
            case LOCAL_ERROR -> labeledMessage(line, "앱");
        };
    }

    private static String playerSender(ChatLine line) {
        String plain = line.getSender().trim();
        String formatted = line.getFormattedSender();
        if (plain.startsWith("<") && plain.endsWith(">")) {
            return formatted + RESET;
        }
        return "<" + formatted + RESET + ">";
    }

    private static String systemMessage(ChatLine line) {
        String sender = line.getSender().trim();
        String prefix = "[" + (sender.isEmpty() ? "서버" : sender) + "]";
        if (!line.getMessage().startsWith(prefix)) {
            return line.getFormattedMessage();
        }
        int formattedPrefix = line.getFormattedMessage().indexOf(prefix);
        if (formattedPrefix < 0) {
            return line.getFormattedMessage();
        }
        return line.getFormattedMessage()
                .substring(formattedPrefix + prefix.length())
                .stripLeading();
    }

    private static String labeledMessage(ChatLine line, String fallbackLabel) {
        String sender = line.getSender().trim();
        String label = sender.isEmpty() ? fallbackLabel : sender;
        String existingPrefix = "[" + label + "]";
        if (line.getMessage().startsWith(existingPrefix)) {
            return line.getFormattedMessage();
        }
        return existingPrefix + " " + line.getFormattedMessage();
    }
}
