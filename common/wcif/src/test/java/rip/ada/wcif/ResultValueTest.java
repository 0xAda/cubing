package rip.ada.wcif;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResultValueTest {

    @Test
    public void shouldRenderSuccessfulResult() {
        assertEquals("0.12", new ResultValue(12).toString());
        assertEquals("1.23", new ResultValue(123).toString());
        assertEquals("1:01.01", new ResultValue(6101).toString());
        assertEquals("1:23.45", new ResultValue(8345).toString());
        assertEquals("9:59.99", new ResultValue(59999).toString());
        assertEquals("10:00", new ResultValue(60000).toString());
        assertEquals("1:00:00", new ResultValue(360000).toString());
    }

    @Test
    public void shouldRenderSkippedAttempt() {
        assertEquals("Skipped", new ResultValue(0).toString());
    }

    @Test
    public void shouldRenderDNF() {
        assertEquals("DNF", new ResultValue(-1).toString());
    }

    @Test
    public void shouldRenderDNS() {
        assertEquals("DNS", new ResultValue(-2).toString());
    }

    @Test
    public void shouldRenderMultiBlindResult() {
        assertEquals("60/66 1:00:00", new ResultValue(450360006).toString());
        assertEquals("63/66 59:50", new ResultValue(390359003).toString());
        assertEquals("61/67 59:56", new ResultValue(440359606).toString());
        assertEquals("61/67 59:56", new ResultValue(440359606).toString());
        assertEquals("2/2 0:43", new ResultValue(970004300).toString());
    }

    @Test
    public void shouldRecogniseSuccessfulAttempt() {
        final ResultValue attempt = new ResultValue(100);
        assertTrue(attempt.isSuccess());
        assertFalse(attempt.isMbld());
        assertFalse(attempt.isSkipped());
        assertFalse(attempt.isDnf());
        assertFalse(attempt.isDns());
    }

    @Test
    public void shouldRecogniseSkippedAttempt() {
        final ResultValue attempt = new ResultValue(0);
        assertTrue(attempt.isSkipped());
        assertFalse(attempt.isMbld());
        assertFalse(attempt.isDnf());
        assertFalse(attempt.isDns());
        assertFalse(attempt.isSuccess());
    }

    @Test
    public void shouldRecogniseDnf() {
        final ResultValue attempt = new ResultValue(-1);
        assertTrue(attempt.isDnf());
        assertFalse(attempt.isSkipped());
        assertFalse(attempt.isMbld());
        assertFalse(attempt.isDns());
        assertFalse(attempt.isSuccess());
    }

    @Test
    public void shouldRecogniseDns() {
        final ResultValue attempt = new ResultValue(-2);
        assertTrue(attempt.isDns());
        assertFalse(attempt.isSkipped());
        assertFalse(attempt.isMbld());
        assertFalse(attempt.isDnf());
        assertFalse(attempt.isSuccess());
    }

}
