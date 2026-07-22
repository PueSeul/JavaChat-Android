package com.dudal.javachat.protocol;

import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandParser;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Tracks the argument ranges that the server marks as signable chat messages. */
final class CommandSignatureTracker {
    private final Map<String, List<Rule>> rulesByCommand = new HashMap<>();

    synchronized void update(ClientboundCommandsPacket packet) {
        rulesByCommand.clear();
        CommandNode[] nodes = packet.getNodes();
        int rootIndex = packet.getFirstNodeIndex();
        if (!valid(nodes, rootIndex)) {
            return;
        }

        for (int childIndex : nodes[rootIndex].getChildIndices()) {
            if (!valid(nodes, childIndex)) {
                continue;
            }
            CommandNode command = nodes[childIndex];
            if (command.getType() != CommandType.LITERAL || command.getName() == null) {
                continue;
            }
            String name = command.getName().toLowerCase(Locale.ROOT);
            List<Rule> rules = new ArrayList<>();
            collect(nodes, childIndex, 0, new HashSet<>(), rules);
            if (!rules.isEmpty()) {
                rulesByCommand.put(name, List.copyOf(rules));
            }
        }
    }

    synchronized List<CommandArgument> signableArguments(String command) {
        List<Token> tokens = tokenize(command);
        if (tokens.isEmpty()) {
            return List.of();
        }
        List<Rule> rules = rulesByCommand.get(tokens.get(0).value.toLowerCase(Locale.ROOT));
        if (rules == null) {
            return List.of();
        }

        Rule selected = null;
        for (Rule rule : rules) {
            if (rule.tokenIndex < tokens.size()
                    && (selected == null || rule.tokenIndex > selected.tokenIndex)) {
                selected = rule;
            }
        }
        if (selected == null) {
            return List.of();
        }
        String value = command.substring(tokens.get(selected.tokenIndex).start).trim();
        return value.isEmpty()
                ? List.of()
                : List.of(new CommandArgument(selected.argumentName, value));
    }

    private static void collect(CommandNode[] nodes, int index, int tokenIndex,
                                Set<Integer> path, List<Rule> rules) {
        if (!valid(nodes, index) || tokenIndex > 64 || !path.add(index)) {
            return;
        }
        CommandNode node = nodes[index];
        if (node.getType() == CommandType.ARGUMENT
                && node.getParser() == CommandParser.MESSAGE
                && node.getName() != null) {
            rules.add(new Rule(node.getName(), tokenIndex));
        }
        for (int child : node.getChildIndices()) {
            collect(nodes, child, tokenIndex + 1, path, rules);
        }
        if (node.getRedirectIndex().isPresent()) {
            collect(nodes, node.getRedirectIndex().getAsInt(), tokenIndex, path, rules);
        }
        path.remove(index);
    }

    private static List<Token> tokenize(String command) {
        List<Token> result = new ArrayList<>();
        int index = 0;
        while (index < command.length()) {
            while (index < command.length() && Character.isWhitespace(command.charAt(index))) {
                index++;
            }
            if (index >= command.length()) {
                break;
            }
            int start = index;
            boolean quoted = command.charAt(index) == '"';
            if (quoted) {
                index++;
            }
            boolean escaped = false;
            while (index < command.length()) {
                char value = command.charAt(index);
                if (quoted) {
                    if (!escaped && value == '"') {
                        index++;
                        break;
                    }
                    escaped = !escaped && value == '\\';
                    if (value != '\\') {
                        escaped = false;
                    }
                    index++;
                } else if (Character.isWhitespace(value)) {
                    break;
                } else {
                    index++;
                }
            }
            result.add(new Token(start, command.substring(start, index)));
        }
        return result;
    }

    private static boolean valid(CommandNode[] nodes, int index) {
        return index >= 0 && index < nodes.length;
    }

    private static final class Rule {
        private final String argumentName;
        private final int tokenIndex;

        private Rule(String argumentName, int tokenIndex) {
            this.argumentName = argumentName;
            this.tokenIndex = tokenIndex;
        }
    }

    private static final class Token {
        private final int start;
        private final String value;

        private Token(int start, String value) {
            this.start = start;
            this.value = value;
        }
    }
}
