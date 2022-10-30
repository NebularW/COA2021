package util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CRCTest {

    @Test
    public void CRCTruePrivateTest1() {
        char[] data = "11100110".toCharArray();
        String p = "1011";
        char[] originCRC = CRC.Calculate(data, p);
        assertArrayEquals(originCRC, "100".toCharArray());
        assertArrayEquals(CRC.Check(data, p, originCRC), "000".toCharArray());
    }

    @Test
    public void CRCTruePrivateTest2() {
        char[] data = "100011".toCharArray();
        String p = "1011";
        char[] originCRC = CRC.Calculate(data, p);
        assertArrayEquals(originCRC, "111".toCharArray());
        assertArrayEquals(CRC.Check(data, p, originCRC), "000".toCharArray());
    }

    @Test
    public void CRCTruePrivateTest3() {
        char[] data = "11010010".toCharArray();
        String p = "10011";
        char[] originCRC = CRC.Calculate(data, p);
        assertArrayEquals(originCRC, "1010".toCharArray());
        assertArrayEquals(CRC.Check(data, p, originCRC), "0000".toCharArray());
    }

    @Test
    public void CRCFalsePrivateTest3() {
        char[] data = "11100110".toCharArray();
        String p = "1001";
        char[] originCRC = CRC.Calculate(data, p);
        assertArrayEquals(originCRC, "001".toCharArray());
        assert originCRC != null;
        originCRC[1] = '1';
        assertArrayEquals(CRC.Check(data, p, originCRC), "010".toCharArray());
    }

}