package com.dudal.javachat.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChatInputTest {
    @Test
    public void replacesEveryLineBreakWithSafeSpacing() {
        assertEquals("첫 줄 둘째 줄 셋째 줄 넷째 줄",
                ChatInput.normalize("첫 줄\r\n둘째 줄\n셋째 줄\u2028넷째 줄"));
    }

    @Test
    public void multilineCommandRemainsOneCommand() {
        assertEquals("/say hello world", ChatInput.normalize("/say hello\nworld"));
    }

    @Test
    public void removesOtherProtocolUnsafeControls() {
        assertEquals("hello world", ChatInput.normalize("\u0000hello\tworld\u0007"));
    }

    @Test
    public void handlesNullAndBlankValues() {
        assertEquals("", ChatInput.normalize(null));
        assertEquals("", ChatInput.normalize("\r\n"));
    }
}
