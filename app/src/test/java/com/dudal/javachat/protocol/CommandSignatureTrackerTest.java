package com.dudal.javachat.protocol;

import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandParser;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;
import org.junit.Test;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandSignatureTrackerTest {
    @Test
    public void extractsMessageArgumentUsingServerCommandTree() {
        CommandNode[] nodes = new CommandNode[] {
                node(CommandType.ROOT, null, null, new int[] {1}),
                node(CommandType.LITERAL, "msg", null, new int[] {2}),
                node(CommandType.ARGUMENT, "targets", CommandParser.ENTITY, new int[] {3}),
                node(CommandType.ARGUMENT, "message", CommandParser.MESSAGE, new int[0])
        };
        CommandSignatureTracker tracker = new CommandSignatureTracker();
        tracker.update(new ClientboundCommandsPacket(nodes, 0));

        List<CommandArgument> arguments = tracker.signableArguments("msg Steve hello there");

        assertEquals(1, arguments.size());
        assertEquals("message", arguments.get(0).getName());
        assertEquals("hello there", arguments.get(0).getValue());
        assertTrue(tracker.signableArguments("help").isEmpty());
    }

    @Test
    public void followsRedirectedAlias() {
        CommandNode[] nodes = new CommandNode[] {
                node(CommandType.ROOT, null, null, new int[] {1, 4}),
                node(CommandType.LITERAL, "msg", null, new int[] {2}),
                node(CommandType.ARGUMENT, "targets", CommandParser.ENTITY, new int[] {3}),
                node(CommandType.ARGUMENT, "message", CommandParser.MESSAGE, new int[0]),
                new CommandNode(CommandType.LITERAL, false, false, new int[0],
                        OptionalInt.of(1), "tell", null, null, null)
        };
        CommandSignatureTracker tracker = new CommandSignatureTracker();
        tracker.update(new ClientboundCommandsPacket(nodes, 0));

        List<CommandArgument> arguments = tracker.signableArguments("tell Alex quoted message");

        assertEquals(1, arguments.size());
        assertEquals("quoted message", arguments.get(0).getValue());
    }

    private static CommandNode node(CommandType type, String name, CommandParser parser,
                                    int[] children) {
        return new CommandNode(type, false, false, children, OptionalInt.empty(),
                name, parser, null, null);
    }
}
