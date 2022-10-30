package transformer;

//import jdk.jfr.Unsigned;

import java.util.Collections;

public class Transformer {
    /**
     * Integer to binaryString
     *
     * @param numStr to be converted
     * @return result
     */
    public String intToBinary(String numStr) {
        byte[] buf = new byte[32];
        for (int i = 0; i < 32; i++) buf[i] = 1;
        try {//不越界
            int num = Integer.valueOf(numStr);
            if (num < 0) {
                return Integer.toBinaryString(num);
            } else {
                return String.join("", Collections.nCopies(32 - Integer.toBinaryString(num).length(), "0")) + Integer.toBinaryString(num);
            }
        } catch (NumberFormatException e) {//越界
            long num = Long.valueOf(numStr);
            long Mod = ((long) Integer.MAX_VALUE + 1) * 2;
            long newNum = (num - Integer.MIN_VALUE) % Mod + Integer.MIN_VALUE;
            return intToBinary(Long.toString(newNum));
        }
    }

    /**
     * BinaryString to Integer
     *
     * @param binStr : Binary string in 2's complement
     * @return :result
     */
    public String binaryToInt(String binStr) {
        //TODO:
        if (binStr.charAt(0) == '0') {
            return Integer.valueOf(binStr.substring(1), 2).toString();
        } else {
            long tmp = Long.valueOf(binStr, 2);
            long Mod = ((long) Integer.MAX_VALUE + 1) * 2;
            int ans = (int) ((tmp - Integer.MIN_VALUE) % Mod + Integer.MIN_VALUE);
            return Integer.toString(ans);
        }
    }

    /**
     * The decimal number to its NBCD code
     */
    public String decimalToNBCD(String decimal) {
        return getBCDString(Integer.parseInt(decimal));
    }

    private String getBCDString(int val) {
        String sign = val < 0 ? "1101" : "1100"; //得到符号位
        String result = "";
        val = Math.abs(val);
        int i = 7;
        while (i > 0) {
            int tmpVal = val % 10;
            result = getBCDString_4(tmpVal).concat(result);
            val = val / 10;
            i--;
        }
        return sign.concat(result);
    }

    private String getBCDString_4(int n) {
        switch (n) {
            case 0:
                return "0000";
            case 1:
                return "0001";
            case 2:
                return "0010";
            case 3:
                return "0011";
            case 4:
                return "0100";
            case 5:
                return "0101";
            case 6:
                return "0110";
            case 7:
                return "0111";
            case 8:
                return "1000";
            case 9:
                return "1001";
        }
        return null;
    }

    /**
     * NBCD code to its decimal number
     */
    public String NBCDToDecimal(String NBCDStr) {
        return String.valueOf(NBCDTrueValue(NBCDStr));
    }

    private int NBCDTrueValue(String operand) {
        StringBuilder ans = new StringBuilder();
        if (operand.startsWith("1101")) ans.append('-');
        operand = operand.substring(4);
        for (int i = 0; i < operand.length() && i < 28; i += 4) {
            ans.append(Integer.valueOf(operand.substring(i, i + 4), 2));
        }
        return Integer.parseInt(ans.toString());
    }

    /**
     * Float true value to binaryString
     *
     * @param floatStr : The string of the float true value
     */
    public String floatToBinary(String floatStr) {
        int eLength = 8;//指数位
        int sLength = 23;//浮点数
        double d = Double.valueOf(floatStr);
        boolean isNeg = d < 0;
        if (Double.isNaN(d)) {
            return "Nan";
        }
        if (!isFinite(d, eLength, sLength)) {
            return isNeg ? "-Inf" : "+Inf";
        }
        StringBuilder answer = new StringBuilder(1 + eLength + sLength);
        if (isNeg) answer.append("1");
        else answer.append("0");
        if (d == 0.0) {
            for (int i = 0; i < eLength + sLength; i++) {
                answer.append("0");
            }
            return answer.toString();
        } else {
            d = Math.abs(d);
            int bias = (int) ((maxValue(eLength) + 1) / 2 - 1);  // bias
            boolean subnormal = (d < minNormal(eLength, sLength));
            if (subnormal) {
                for (int i = 0; i < eLength; i++) {
                    answer.append("0");
                }
                d = d * Math.pow(2, bias - 1);  //将指数消去
                answer.append(fixPoint(d, sLength));
            } else {
                int exponent = getExponent(d);
                answer.append(integerRepresentation(String.valueOf((int) (exponent + bias)), eLength));  // 加上 bias
                d = d / Math.pow(2, exponent);
                answer.append(fixPoint(d - 1, sLength));  // fixPoint传入的参数要求小于1，自动忽略了隐藏位            }
            }
            return answer.toString();
        }
    }

    private String integerRepresentation(String number, int length) {
        String result = intToBinary(number);
        return result.substring(32 - length);
    }

    private boolean isFinite(double d, int eLength, int sLength) {
        int bias = (int) ((maxValue(eLength) + 1) / 2 - 1);  // bias
        int exponent = (int) (maxValue(eLength) - 1 - bias - sLength);  // 指数全1和全0是特殊情况，这里只要计算可以被正常表示的最大值，因此-1，且直接将significand转化的位数减去
        double significand = maxValue(sLength + 1);  // 加上隐藏位
        double result = significand * Math.pow(2, exponent);
        return d >= -result && d <= result;
    }

    private double maxValue(int length) {
        //不能使用移位操作
        return Math.pow(2, length) - 1;
    }

    private double minNormal(int eLength, int sLength) {
        int bias = (int) ((maxValue(eLength) + 1) / 2 - 1);  // bias
        return Math.pow(2, 1 - bias);  // 指数为1，阶码全0
    }

    private int getExponent(double d) {
        if (d >= 1 && d < 2) return 0;
        int cnt = 0;
        if (d >= 2) {
            while (d >= 2) {
                d /= 2;
                cnt++;
            }
        } else {
            while (d < 1) {
                d *= 2;
                cnt--;
            }
        }
        return cnt;
    }

    private String fixPoint(double d, int sLength) {
        d = d < 1 ? d : d - (int) d;  // d = 0.xxxxx
        StringBuilder res = new StringBuilder();
        int count = 0;
        while (d != 0 && count < sLength) {
            d *= 2;
            if (d < 1) {
                res.append("0");
            } else {
                d -= 1;
                res.append("1");
            }
            count++;  // 最长为sLength的长度
        }
        int len = res.length();  // 不能直接用res.length()
        for (int i = 0; i < sLength - len; i++) res.append(0);
        return res.toString();
    }

    /**
     * Binary code to its float true value
     */
    public String binaryToFloat(String binStr) {
        boolean isNeg = (binStr.charAt(0) == '1');
        String exp = binStr.substring(1, 9);
        String frag = binStr.substring(9);
        if (exp.equals("11111111")) {
            if (frag.contains("1")) {
                return "NaN";
            } else {
                return isNeg ? "-Inf" : "+Inf";
            }
        } else if (exp.equals("00000000")) {
            if (frag.contains("1")) {
                double f = 0.0;
                int fe = 1;
                for (char fc : frag.toCharArray()) {
                    f += Integer.parseInt(String.valueOf(fc)) / Math.pow(2, fe);
                    fe++;
                }
                f = (f) * Math.pow(2, -126);
                f = isNeg ? -f : f;
                return String.valueOf(f);
            } else {
                return "0.0";
            }
        }
        double f = 0.0;
        int fe = 1;
        for (char fc : frag.toCharArray()) {
            f += Integer.parseInt(String.valueOf(fc)) / Math.pow(2, fe);
            fe++;
        }
        int e = Integer.parseInt(exp, 2) - 127;
        f = (1 + f) * Math.pow(2, e);
        f = isNeg ? -f : f;
        return String.valueOf(f);
    }


}
