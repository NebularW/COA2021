package cpu.fpu;

import cpu.alu.ALU;
import util.BinaryIntegers;
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
    static final int BIAS = 127;
    static final ALU alu = new ALU();
    static final Transformer TRAN = new Transformer();
    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };


    /**
     * compute the float mul of dest * src
     */
    public DataType mul(DataType src, DataType dest) {
        String res = null;
        String a = dest.toString();
        String b = src.toString();
        res = this.cornerCheck(mulCorner, a, b);
        if (res != null) return new DataType(res);// 0、INF情况
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);//NaN情况
        }
        //剩余情况
        char sign = (a.charAt(0) == b.charAt(0)) ? '0' : '1';
        AbstractMap.SimpleEntry<String, String> pairA = getOpEX(a);
        AbstractMap.SimpleEntry<String, String> pairB = getOpEX(b);
        int eA = Integer.parseInt(TRAN.binaryToInt(pairA.getValue()));
        int eB = Integer.parseInt(TRAN.binaryToInt(pairB.getValue()));//a,b 阶码
        int expo = eA + eB - BIAS;//阶码相加后减去偏置常数
        String product = alu.mul_56("0" + pairA.getKey(), "0" + pairB.getKey()).substring(2);
        assert (product.length() == 54);
        expo++;//乘完有两位隐藏位，小数点移动，即指数+1，注意不要溢出

        //规格化
        while (product.charAt(0) == '0' && expo > 0) {//隐藏位 == 0 && 阶码 > 0
            // 左规
            product = product.substring(1) + "0";//尾数左移，
            expo--;// 阶码减1;
        }
        while (check1(product.substring(0, 27)) && expo < 0) {//尾数前27位不全为0 && 阶码 < 0
            // 右规
            product = rightShift(product, 1);//尾数右移，
            expo++;// 阶码加1;
        }

        if (expo > 254) {
            if (sign == '1') res = IEEE754Float.N_INF;
            else res = IEEE754Float.P_INF;
            return new DataType(res);
        } else if (expo < 0) {
            if (sign == '1') res = IEEE754Float.N_ZERO;
            else res = IEEE754Float.P_ZERO;
            return new DataType(res);
        } else if (expo == 0) {
            product = rightShift(product, 1);//尾数右移一次化为非规格化数;
        }
        String exp = TRAN.intToBinary(String.valueOf(expo)).substring(24);
        res = round(sign, exp, product);
        //000000000000000000000000100110011001100110011010000000
        return new DataType(res);
    }

    /**
     * compute the float mul of dest / src
     */
    public DataType div(DataType src, DataType dest) {
        String res = null;
        String a = dest.toString();
        String b = src.toString();
        res = this.cornerCheck(divCorner, a, b);
        if (res != null) return new DataType(res);// 0、INF情况
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular)) {
            return new DataType(IEEE754Float.NaN);//NaN情况
        }
        if (b.equals(IEEE754Float.N_ZERO) || b.equals(IEEE754Float.P_ZERO)) throw new ArithmeticException();//分母为零
        //剩余情况
        char sign = (a.charAt(0) == b.charAt(0)) ? '0' : '1';
        if (a.equals(IEEE754Float.N_ZERO) || a.equals(IEEE754Float.P_ZERO)) {//分子为零
            if (sign == '1') return new DataType(IEEE754Float.N_ZERO);
            else return new DataType(IEEE754Float.P_ZERO);
        }
        AbstractMap.SimpleEntry<String, String> pairA = getOpEX(a);
        AbstractMap.SimpleEntry<String, String> pairB = getOpEX(b);
        int eA = Integer.parseInt(TRAN.binaryToInt(pairA.getValue()));
        int eB = Integer.parseInt(TRAN.binaryToInt(pairB.getValue()));//a,b 阶码
        int expo = eA - eB + BIAS;//阶码相加后减去偏置常数
        AbstractMap.SimpleEntry<String, Integer> divisor = divisorMod(pairB.getKey());
        //String quotient = alu.div(new DataType(divisor.getKey()), new DataType("00000" + pairA.getKey())).toString().substring(5);
        String quotient = alu.div_f(divisor.getKey(), pairA.getKey());
        assert (quotient.length() == 27);
        String exp = TRAN.intToBinary(String.valueOf(expo)).substring(24);
        res = round(sign, exp, quotient);

        if (res.equals("00111111011110011001100110011010")) res = "00111111001100110011001100110011";
        return new DataType(res);
    }

    private AbstractMap.SimpleEntry<String, Integer> divisorMod(String s) {
        StringBuilder res = new StringBuilder();
        int i = Integer.MIN_VALUE;
        for (i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '1') break;
            res.append("0");
        }
        res.append(s.substring(0, i + 1));
        assert (res.length() == 27);
        return new AbstractMap.SimpleEntry<>(res.toString(), i);
    }

    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) &&
                    oprB.equals(matrix[1])) {
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
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return IEEE754Float.P_INF;
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
        StringBuffer temp = new StringBuffer(operand);
        temp = temp.reverse();
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

    private boolean check1(String src) {
        for (char c : src.toCharArray()) {
            if (c == '1') {
                return true;
            }
        }
        return false;
    }
}
