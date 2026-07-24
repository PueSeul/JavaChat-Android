package com.dudal.javachat.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.dudal.javachat.protocol.ChatLine;
import com.dudal.javachat.protocol.LegacyText;

import org.junit.Test;

public final class ChatLineTextTest {
    @Test
    public void playerChatUsesMinecraftLayoutWithoutTimestamp() {
        ChatLine line = new ChatLine(
                1L, ChatLine.Kind.PLAYER, "PueSeul", "안녕하세요");

        assertEquals("<PueSeul> 안녕하세요",
                LegacyText.strip(ChatLineText.compose(line)));
    }

    @Test
    public void alreadyDecoratedPlayerNameIsNotWrappedAgain() {
        ChatLine line = new ChatLine(
                1L, ChatLine.Kind.PLAYER, "<Steve>", "반갑습니다");

        assertEquals("<Steve> 반갑습니다",
                LegacyText.strip(ChatLineText.compose(line)));
    }

    @Test
    public void presenceMessageHasNoMetadataPrefix() {
        ChatLine line = new ChatLine(
                1L, ChatLine.Kind.PRESENCE, "서버", "Alex님이 서버에 접속했습니다.");

        assertEquals("Alex님이 서버에 접속했습니다.",
                LegacyText.strip(ChatLineText.compose(line)));
    }

    @Test
    public void systemMessageDoesNotShowServerLabel() {
        ChatLine plain = new ChatLine(
                1L, ChatLine.Kind.SYSTEM, "서버", "잠시 후 서버가 재시작됩니다.");
        ChatLine labeled = new ChatLine(
                1L, ChatLine.Kind.SYSTEM, "서버", "[서버] 이미 표시된 메시지");

        assertEquals("잠시 후 서버가 재시작됩니다.",
                LegacyText.strip(ChatLineText.compose(plain)));
        assertEquals("이미 표시된 메시지",
                LegacyText.strip(ChatLineText.compose(labeled)));
    }

    @Test
    public void minecraftFormattingIsPreserved() {
        ChatLine line = new ChatLine(
                1L, ChatLine.Kind.PLAYER, "\u00A7bPueSeul", "\u00A7a안녕하세요");

        String composed = ChatLineText.compose(line);

        assertTrue(composed.contains("\u00A7bPueSeul"));
        assertTrue(composed.contains("\u00A7a안녕하세요"));
    }
}
