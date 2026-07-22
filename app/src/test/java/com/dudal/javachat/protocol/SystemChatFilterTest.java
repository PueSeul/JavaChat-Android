package com.dudal.javachat.protocol;

import net.kyori.adventure.text.Component;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class SystemChatFilterTest {
    @Test
    public void translatesPlayerJoinAndLeaveMessages() {
        assertEquals("PueSeul님이 접속했습니다.",
                SystemChatFilter.displayText(Component.translatable(
                        "multiplayer.player.joined", Component.text("PueSeul"))));
        assertEquals("PueSeul님이 접속했습니다. (이전 닉네임: OldName)",
                SystemChatFilter.displayText(Component.translatable(
                "multiplayer.player.joined.renamed",
                        Component.text("PueSeul"), Component.text("OldName"))));
        assertEquals("PueSeul님이 나갔습니다.",
                SystemChatFilter.displayText(Component.translatable(
                        "multiplayer.player.left", Component.text("PueSeul"))));
    }

    @Test
    public void hidesRawJoinKeyFromTranslatedLegacyPackets() {
        assertNull(SystemChatFilter.displayText(
                Component.text("multiplayer.player.joined")));
    }

    @Test
    public void translatesJoinMessageNestedInsideAnotherComponent() {
        Component nested = Component.text("").append(Component.translatable(
                "multiplayer.player.joined", Component.text("PueSeul")));
        assertEquals("PueSeul님이 접속했습니다.",
                SystemChatFilter.displayText(nested));
    }

    @Test
    public void keepsNormalServerMessages() {
        assertEquals("로그인이 필요합니다. /login <비밀번호>",
                SystemChatFilter.displayText(
                        Component.text("로그인이 필요합니다. /login <비밀번호>")));
    }
}
