package cpu.alu;


import util.DataType;
import util.Transformer;

import java.util.Arrays;

/**
 * Arithmetic Logic Unit
 * ALU封装类*/


public class ALU {

    DataType remainderReg;
    private final int LENGTH = 32;

    private int logicALgo(char op, int a, int b) {//取反时b无意义
        switch (op) {
            case '^':
                int res = (a == b) ? 0 : 1;
                return res;
            case '&':
                if (a == b && a == 1) return 1;
                else return 0;
            case '|':
                if (a == b && a == 0) return 0;
                else return 1;
            case '!':
                return (a == 1) ? 0 : 1;
            default:
                throw new NumberFormatException("not a logical operation!");
        }
    }

    private DataType opposite(DataType s) {
        int idx = -1;
        char[] res = new char[LENGTH];
        for (int i = LENGTH - 1; i >= 0; i--) {
            if (s.toString().charAt(i) == '1') {
                res[i] = '1';
                idx = i;
                break;
            }
            res[i] = '0';
        }
        for (int i = idx - 1; i >= 0; i--) {
            int tmp = logicALgo('!', s.toString().charAt(i) - '0', 2);//b无意义
            res[i] = (char) (tmp + '0');
        }
        return new DataType(String.valueOf(res));
    }

/**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits*/


    public DataType add(DataType src, DataType dest) {
        char[] res = new char[LENGTH];
        int carry = 0;
        for (int i = LENGTH - 1; i >= 0; i--) {
            int a = src.toString().charAt(i) - '0';
            int b = dest.toString().charAt(i) - '0';
            int tmp = logicALgo('^', a, b);
            res[i] = (char) (logicALgo('^', tmp, carry) + '0');//Fi
            int t1 = logicALgo('&', a, carry);
            int t2 = logicALgo('&', b, carry);
            int t3 = logicALgo('&', a, b);
            tmp = logicALgo('|', t1, t2);
            carry = logicALgo('|', tmp, t3);//Ci
        }
        return new DataType(String.valueOf(res));
    }


/**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits*/


    public DataType sub(DataType src, DataType dest) {
        return add(opposite(src), dest);
    }


/**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits*/


    public DataType mul(DataType src, DataType dest) {
        char[] res = new char[2 * LENGTH];
        for (int i = 0; i < 2 * LENGTH; i++) {
            if (i < LENGTH) res[i] = '0';
            else res[i] = src.toString().charAt(i - LENGTH);
        }
        for (int i = LENGTH - 1; i >= 0; i--) {
            //YY
            int tmp = 0;
            if (i == LENGTH - 1) tmp = '0' - src.toString().charAt(i);
            else tmp = src.toString().charAt(i + 1) - src.toString().charAt(i);
            if (tmp == 1) {
                String s = add(new DataType(String.valueOf(Arrays.copyOf(res, LENGTH))), dest).toString();
                for (int k = 0; k < LENGTH; k++) {
                    res[k] = s.charAt(k);
                }
            } else if (tmp == -1) {
                String s = sub(dest, new DataType(String.valueOf(Arrays.copyOf(res, LENGTH)))).toString();
                for (int k = 0; k < LENGTH; k++) {
                    res[k] = s.charAt(k);
                }
            }

            //有符号移位
            for (int j = 2 * LENGTH - 1; j > 0; j--) {
                res[j] = res[j - 1];
            }
            res[0] = res[1];
        }
        return new DataType(String.valueOf(Arrays.copyOfRange(res, LENGTH, 2 * LENGTH)));

    }


/**
     * 返回两个二进制整数的除法结果
     * 请注意使用不恢复余数除法方式实现
     * dest ÷ src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits*/


    public DataType div(DataType src, DataType dest) {
        if (src.toString().equals("00000000000000000000000000000000")) throw new ArithmeticException();
        char[] res = new char[2 * LENGTH];
        for (int i = 0; i < 2 * LENGTH; i++) {
            if (i < LENGTH) res[i] = dest.toString().charAt(0);
            else res[i] = dest.toString().charAt(i - LENGTH);
        }//有符号扩展

        for (int j = 0; j < LENGTH; j++) {
            //左移
            for (int i = 0; i < 2 * LENGTH - 1; i++) {
                res[i] = res[i + 1];
            }
            //最后一位,是否够
            if (isEnough(String.valueOf(Arrays.copyOf(res, LENGTH)), src.toString())) {
                String s = getEnoughString(String.valueOf(Arrays.copyOf(res, LENGTH)), src.toString());
                for (int k = 0; k < LENGTH; k++) {
                    res[k] = s.charAt(k);
                }
                res[2 * LENGTH - 1] = '1';
            } else res[2 * LENGTH - 1] = '0';
        }
        this.remainderReg = new DataType(String.valueOf(Arrays.copyOfRange(res, 0, LENGTH)));
        DataType ans = new DataType(String.valueOf(Arrays.copyOfRange(res, LENGTH, 2 * LENGTH)));
        if (src.toString().charAt(0) == dest.toString().charAt(0)) {
            if (opposite(src).toString().equals(remainderReg.toString())) {
                remainderReg = new DataType("00000000000000000000000000000000");
                return sub(new DataType("00000000000000000000000000000001"), ans);
            }
            if (src.toString().equals(remainderReg.toString())) {
                remainderReg = new DataType("00000000000000000000000000000000");
                return add(new DataType("00000000000000000000000000000001"), ans);
            }
            return ans;
        } else {
            if (opposite(src).toString().equals(remainderReg.toString())) {
                remainderReg = new DataType("00000000000000000000000000000000");
                return sub(new DataType("00000000000000000000000000000001"), opposite(ans));
            }
            if (src.toString().equals(remainderReg.toString())) {
                remainderReg = new DataType("00000000000000000000000000000000");
                return add(new DataType("00000000000000000000000000000001"), opposite(ans));
            }
            return opposite(ans);
        }
    }

    private Boolean isEnough(String remains, String src) {
        int rf = remains.charAt(0) - '0';//余数符号
        int sf = src.charAt(0) - '0';//除数符号
        if (rf == sf) {//同号相减
            String res = sub(new DataType(src), new DataType(remains)).toString();
//            if (TRAN.binaryToInt(res).equals("0"))
//                return true;
            return res.charAt(0) - '0' == rf;
        } else {//异号相加
            String res = add(new DataType(remains), new DataType(src)).toString();
//            if (TRAN.binaryToInt(res).equals("0"))
//                return true;
            return res.charAt(0) - '0' == rf;
        }
    }

    private String getEnoughString(String remains, String src) {
        int rf = remains.charAt(0) - '0';//余数符号
        int sf = src.charAt(0) - '0';//除数符号
        if (rf == sf) {//同号相减
            return sub(new DataType(src), new DataType(remains)).toString();
        } else {//异号相加
            return add(new DataType(remains), new DataType(src)).toString();
        }
    }
}
