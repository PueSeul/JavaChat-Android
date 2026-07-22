package com.dudal.javachat.protocol;

import net.kyori.adventure.text.Component;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComponentTextTest {
    @Test
    public void translatesInvalidProfileKeyReason() {
        assertEquals(
                "프로필 공개키 서명이 올바르지 않습니다. Microsoft 로그인을 다시 진행해 주세요.",
                ComponentText.plain(Component.translatable(
                        "multiplayer.disconnect.invalid_public_key_signature")));
    }
}
