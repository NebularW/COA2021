package util;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public class CRC {

    /**
     * CRC计算器
     *
     * @param data       数据流
     * @param polynomial 多项式
     * @return CheckCode
     */

    public static char[] Calculate(char[] data, String polynomial) {
        int dataLen = data.length;
        int R = polynomial.length() - 1;
        //左移R位
        char[] tmpData = new char[dataLen + R];
        for (int i = 0; i < dataLen; i++) {
            tmpData[i] = data[i];
        }
        for (int i = 0; i < R; i++) {
            tmpData[dataLen + i] = '0';
        }
        return m2Div(polynomial, tmpData);
    }

    private static int getFirstOne(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 1) return i;
        }
        return -1;
    }

    /**
     * CRC校验器
     *
     * @param data       接收方接受的数据流
     * @param polynomial 多项式
     * @param CheckCode  CheckCode
     * @return 余数
     */
    public static char[] Check(char[] data, String polynomial, char[] CheckCode) {
        int dataLen = data.length;
        int R = polynomial.length() - 1;
        //左移，加上校验码
        char[] tmpData = new char[dataLen + R];
        for (int i = 0; i < dataLen; i++) {
            tmpData[i] = data[i];
        }
        for (int i = 0; i < R; i++) {
            tmpData[dataLen + i] = CheckCode[i];
        }
        return m2Div(polynomial, tmpData);
    }

    private static char[] m2Div(String polynomial, char[] tmpData) {
        //模二除法求余

        int R = polynomial.length() - 1;
        int res = R + 1;
        int[] sub = new int[polynomial.length()];
        char[] ans = new char[R];
        for (int i = 0; i < sub.length; i++) {
            sub[i] = Character.getNumericValue(tmpData[i]);
        }
        int cnt = sub.length;//位数
        OUTER:
        while (cnt <= tmpData.length && res == R + 1) {
            int firstOne = getFirstOne(sub);//1
            if (firstOne == 0) {
                for (int i = 0; i < sub.length; i++) {
                    sub[i] ^= polynomial.charAt(i) - '0';
                }
            }
            firstOne = getFirstOne(sub);
            for (res = 0; res < sub.length; res++) {
                if (firstOne != -1 && res + firstOne < sub.length) sub[res] = sub[res + firstOne];
                else {
                    if (cnt < tmpData.length) {
                        sub[res] = tmpData[cnt] - '0';
                        cnt++;
                    } else break OUTER;
                }
            }//res为余数位数
        }
        if (res == R + 1) {
            for (int i = 0; i < R; i++) {
                ans[i] = (char) (sub[i + 1] + '0');
            }
        } else {
            for (int i = 0; i < R; i++) {
                if (i < R - res) ans[i] = '0';
                else ans[i] = (char) (sub[i + res - R] + '0');
            }
        }
        return ans;
    }
    /*private static char[] m2Div(char[] tmpData, String polynomial) {
        int a = Integer.parseInt(String.copyValueOf(tmpData), 2);
        int b = Integer.parseInt(polynomial, 2);
        int lengtha = intLength(a, 2);
        int lengthb = intLength(b, 2);
        //存储余数
        int d = 0;
        //存储商
        int e = 0;
        for (int i = lengtha; i > 0; i--) {
            int c = getIndex(a, i);
            d = (d << 1) + c;
            e = e << 1;
            if (intLength(d, 2) == lengthb) {
                e = e + 1;
                d = d ^ b;
            }
        }
        char[] res = new char[polynomial.length() - 1];
        char[] tmp = Integer.toBinaryString(d).toCharArray();
        for (int i = 0; i < polynomial.length() - 1 - tmp.length; i++) res[i] = '0';
        for (int i = 0; i < tmp.length; i++) {
            res[i + polynomial.length() - 1 - tmp.length] = tmp[i];
        }
        return res;
    }*/

    private static int intLength(int num, int radix) {
        for (int i = 0; i < 32; i++) {
            int c = (int) Math.pow(radix, i);
            if (num % c == num) {
                return i;
            }
        }
        return 0;
    }

    private static int getIndex(int a, int index) {
        return a >> (index - 1) & 1;
    }
}
/*
 */
