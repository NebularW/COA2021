package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

import java.util.AbstractMap;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {
    static final int OP_LEN = 27;
    static final Transformer TRAN = new Transformer();
    static final ALU alu = new ALU();
    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    /**
     * compute the float add of (dest + src)
     */
    public DataType add(DataType src, DataType dest) {
        String res = null;
        String a = dest.toString();
        String b = src.toString();
        res = this.cornerCheck(addCorner, a, b);
        if (res != null) return new DataType(res);// 0、INF情况
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);//NaN情况
        }
        //剩余情况
        char sign = '\0';
        if (a.charAt(0) == '0' && b.charAt(0) == '0') sign = '0';
        if (a.charAt(0) == '1' && b.charAt(0) == '1') sign = '1';
        AbstractMap.SimpleEntry<String, String> pairA = getOpEX(a);
        AbstractMap.SimpleEntry<String, String> pairB = getOpEX(b);
        int eA = Integer.parseInt(TRAN.binaryToInt(pairA.getValue()));
        int eB = Integer.parseInt(TRAN.binaryToInt(pairB.getValue()));
        String exp = null;
        String sig_GRS = null;
        AbstractMap.SimpleEntry<String, Integer> op_ex = null;
        if (eA >= eB) {
            if (sign == '\0') {
                if (eA > eB) {
                    sign = a.charAt(0);
                    op_ex = operandSub(pairB.getKey(), pairA.getKey(), eA - eB, Integer.parseInt(TRAN.binaryToInt(pairA.getValue())));
                } else {
                    if (Integer.valueOf(TRAN.binaryToInt("00000" + pairA.getKey())) > Integer.valueOf(TRAN.binaryToInt("00000" + pairB.getKey()))) {
                        sign = a.charAt(0);
                        op_ex = operandSub(pairB.getKey(), pairA.getKey(), 0, Integer.parseInt(TRAN.binaryToInt(pairA.getValue())));
                    } else {
                        sign = b.charAt(0);
                        op_ex = operandSub(pairA.getKey(), pairB.getKey(), 0, Integer.parseInt(TRAN.binaryToInt(pairA.getValue())));
                    }
                }
            } else {
                op_ex = operandAdd(pairB.getKey(), pairA.getKey(), eA - eB, Integer.parseInt(TRAN.binaryToInt(pairA.getValue())));
            }//同号
        } else {
            if (sign == '\0') {
                sign = b.charAt(0);
                op_ex = operandSub(pairA.getKey(), pairB.getKey(), eB - eA, Integer.parseInt(TRAN.binaryToInt(pairB.getValue())));
            } else
                op_ex = operandAdd(pairA.getKey(), pairB.getKey(), eB - eA, Integer.parseInt(TRAN.binaryToInt(pairB.getValue())));
        }
        try {
            sig_GRS = op_ex.getKey();
            exp = TRAN.intToBinary(String.valueOf(op_ex.getValue())).substring(24);
            res = round(sign, exp, sig_GRS);
        } catch (NullPointerException e) {
            if (sign == '1') res = IEEE754Float.N_INF;
            else res = IEEE754Float.P_INF;
        }
        if (res.equals(IEEE754Float.N_ZERO)) res = IEEE754Float.P_ZERO;
        return new DataType(res);
    }

    /**
     * return operand, expo(changed if denormalized)
     */
    private AbstractMap.SimpleEntry<String, String> getOpEX(String IEEE) {
        StringBuilder operand = new StringBuilder(IEEE.substring(9, 32));//尾数的最前面添加上隐藏位，规格化数为1，非规格化数为0   1+23+3=27位
        String expo = new String(IEEE.substring(1, 9));//阶数
        if (expo.equals("00000000")) {//非规格化
            operand.insert(0, '0');
            operand.append("000");
            expo = "00000001";
        } else {//规格化
            operand.insert(0, '1');
            operand.append("000");
        }
        assert (operand.toString().length() == OP_LEN && expo.length() == 8);
        return new AbstractMap.SimpleEntry<String, String>(operand.toString(), expo);
    }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src, DataType dest) {
        String tmp = src.toString();
        if (tmp.charAt(0) == '0') tmp = "1" + tmp.substring(1);
        else tmp = "0" + tmp.substring(1);
        return add(new DataType(tmp), dest);
    }

    private AbstractMap.SimpleEntry<String, Integer> operandAdd(String small, String large, int n, int exp) {
        String sig_GRS = null;
        String shifted = "00000" + rightShift(small, n);
        String add = alu.add(new DataType(shifted), new DataType("00000" + large)).toString();
        if (add.charAt(4) == '1') {//进位为28位
            if (exp == 254) return null;//上溢出
            else {
                add = rightShift(add, 1);
                assert (add.charAt(4) != '1');
                exp++;
            }
        } else if (add.charAt(5) != '1') {//<27位
            while (exp > 1 && add.charAt(5) != '1') {
                add = add.substring(1) + "0";//左移
                exp--;//exp-1
            }
            if (add.charAt(5) != '1') {//非规格化
                exp = 0;
                //0.f
            }
        }
        sig_GRS = add.substring(5);
        return new AbstractMap.SimpleEntry<>(sig_GRS, exp);
    }

    private AbstractMap.SimpleEntry<String, Integer> operandSub(String small, String large, int n, int exp) {
        String sig_GRS = null;
        String shifted = "00000" + rightShift(small, n);
        String sub = alu.sub(new DataType(shifted), new DataType("00000" + large)).toString();
        if (sub.charAt(4) == '1') {//进位为28位
            if (exp == 254) return null;//上溢出
            else {
                sub = rightShift(sub, 1);
                assert (sub.charAt(4) != '1');
                exp++;
            }
        } else if (sub.charAt(5) != '1') {//<27位
            while (exp > 1 && sub.charAt(5) != '1') {
                sub = sub.substring(1) + "0";//左移
                exp--;//exp-1
            }
            if (sub.charAt(5) != '1') {//非规格化
                exp = 0;
                //0.f
            }
        }
        sig_GRS = sub.substring(5);
        return new AbstractMap.SimpleEntry<>(sig_GRS, exp);
    }

    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24), 2);
        String sig = sig_grs.substring(0, 24);
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carray to the next) and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }

}
