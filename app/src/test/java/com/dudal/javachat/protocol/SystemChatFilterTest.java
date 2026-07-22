package com.dudal.javachat.protocol;

import net.kyori.adventure.text.Component;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SystemChatFilterTest {
    @Test
    public void hidesPlayerJoinAndLeaveTranslationMessages() {
        assertFalse(SystemChatFilter.shouldDisplay(Component.translatable(
                "multiplayer.player.joined", Component.text("PueSeul"))));
        assertFalse(SystemChatFilter.shouldDisplay(Component.translatable(
                "multiplayer.player.joined.renamed",
                Component.text("PueSeul"), Component.text("OldName"))));
        assertFalse(SystemChatFilter.shouldDisplay(Component.translatable(
                "multiplayer.player.left", Component.text("PueSeul"))));
    }

    @Test
    public void hidesRawJoinKeyFromTranslatedLegacyPackets() {
        assertFalse(SystemChatFilter.shouldDisplay(
                Component.text("multiplayer.player.joined")));
    }

    @Test
    public void hidesJoinTranslationNestedInsideAnotherComponent() {
        Component nested = Component.text("").append(Component.translatable(
                "multiplayer.player.joined", Component.text("PueSeul")));
        assertFalse(SystemChatFilter.shouldDisplay(nested));
    }

    @Test
    public void keepsNormalServerMessages() {
        assertTrue(SystemChatFilter.shouldDisplay(
                Component.text("로그인이 필요합니다. /login <비밀번호>")));
    }
}
