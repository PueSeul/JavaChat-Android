package com.dudal.javachat.util;

import org.junit.Test;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ErrorTextTest {
    @Test
    public void testCommonNetworkErrorsAreReadable() {
        assertEquals("서버 주소를 찾을 수 없습니다.",
                ErrorText.from(new RuntimeException(new UnknownHostException("secret.host"))));
        assertEquals("서버가 닫혀 있거나 포트가 올바르지 않습니다.",
                ErrorText.from(new ConnectException("Connection refused")));
        assertEquals("서버 응답 시간이 초과되었습니다.",
                ErrorText.from(new SocketTimeoutException("timed out")));
    }

    @Test
    public void testRawErrorsAreCollapsedAndBounded() {
        String message = ErrorText.from(new IllegalStateException("line one\n" + "x".repeat(300)));
        assertTrue(message.length() <= 180);
        assertTrue(!message.contains("\n"));
    }
}
