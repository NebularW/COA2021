package cpu.nbcdu;

import cpu.alu.ALU;
import util.DataType;

import java.util.ArrayList;
import java.util.Arrays;

public class NBCDU {
    private static final String[] NBCD_8421 = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001"};
    private int carry = 0;
    private int carry_for_sub = 0;
    private String sign = null;
    private Boolean flag = false;

    /**
     * @param src  A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest + src
     */
    DataType add(DataType src, DataType dest) {
        if (src.toString().equals("11000000000000000000000000000000")) return dest;
        if (dest.toString().equals("11000000000000000000000000000000")) return src;
        DataType ans = null;
        ArrayList<String> srcArr = strSplit(src.toString());
        ArrayList<String> destArr = strSplit(dest.toString());
        if (srcArr.get(7).equals(destArr.get(7))) {
            String sign = srcArr.get(7);
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                String tmp = simpleAdd(srcArr.get(i), destArr.get(i));
                if (this.carry == 1 || !containBCD(tmp)) {
                    tmp = adjust(tmp);
                    srcArr.set(i + 1, BCD_Carry1(srcArr.get(i + 1)));//符号位被改动了，要提前保存
                }
                res.insert(0, tmp);
                this.carry = 0;
            }
            res.insert(0, sign);
            if (res.toString().equals("11010000000000000000000000000000"))//负零
                return new DataType("11000000000000000000000000000000");
            return new DataType(res.toString());
        } else if (srcArr.get(7).equals("1101")) {
            ans = sub(negative(src), dest);
            if (carry_for_sub == 1) this.sign = dest.toString().substring(0, 4);
            else this.sign = dest.toString().substring(0, 4).equals("1101") ? "1100" : "1101";
        } else {
            ans = sub(negative(dest), src);
            if (carry_for_sub == 1) this.sign = src.toString().substring(0, 4);
            else this.sign = src.toString().substring(0, 4).equals("1101") ? "1100" : "1101";
        }
        return new DataType(this.sign + ans.toString().substring(4));
    }

    /***
     *
     * @param src A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest - src
     */
    DataType sub(DataType src, DataType dest) {
        if (src.toString().equals("11000000000000000000000000000000")) return dest;
        if (dest.toString().equals("11000000000000000000000000000000")) return negative(src);
        if (src.toString().charAt(3) == dest.toString().charAt(3)) {
            /*String tmp = add(dest, subProcess(src.toString())).toString();
            if (this.carry_for_sub == 0) {
                return new DataType(dest.toString().substring(0, 4) + tmp.substring(4));
            } else {
                String sign = null;
                if (dest.toString().substring(0, 4).equals("1101")) {
                    sign = "1100";
                } else sign = "1101";
                return new DataType(sign + tmp.substring(4));
            }*/
            ArrayList<String> srcArr = strSplit(subProcess(src.toString()).toString());
            ArrayList<String> destArr = strSplit(dest.toString());
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                String tmp = simpleAdd(srcArr.get(i), destArr.get(i));
                if (this.carry == 1 || !containBCD(tmp)) {
                    tmp = adjust(tmp);
                    srcArr.set(i + 1, BCD_Carry1(srcArr.get(i + 1)));//符号位被改动了，要提前保存
                }
                res.insert(0, tmp);
                if (i == 6)
                    this.carry_for_sub = carry;
                this.carry = 0;
            }
            res.insert(0, "1101");//补齐位数，符号假的
            String ret = res.toString();
            String ans = res.toString();
            if (this.carry_for_sub == 1) {
                this.sign = dest.toString().substring(0, 4);//有进位和dest同号
            } else {
                this.sign = dest.toString().substring(0, 4).equals("1101") ? "1100" : "1101";
                ans = subProcess(ans).toString();//可能1010
                StringBuilder sb = new StringBuilder(ans);
                ret = "";
                for (int i = 28; i >= 4; i -= 4) {
                    if (sb.substring(i, i + 4).equals("1010")) {
                        ret = "0000" + ret;
                        sb.replace(i - 4, i, BCD_Carry1(sb.substring(i - 4, i)));
                    } else ret = sb.substring(i, i + 4) + ret;
                }
                ret = "1101" + ret;
            }
            String t = this.sign + ret.substring(4);
            if (t.equals("11010000000000000000000000000000")) t = "11000000000000000000000000000000";
            return new DataType(t);
        } else return add(dest, negative(src));
    }

    /***
     *
     * @param a A 4-bits NBCD String
     * @param b A 4-bits NBCD String
     * @return a+b, changing carry by the way
     */
    private String simpleAdd(String a, String b) {
        char car = '0';
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i >= 0; i--) {
            if (a.charAt(i) == b.charAt(i)) {
                if (a.charAt(i) == '1') {
                    sb.insert(0, car);
                    car = '1';
                } else {
                    sb.insert(0, car);
                    car = '0';
                }
            } else {
                if (car == '1') {
                    sb.insert(0, '0');
                    car = '1';
                } else {
                    sb.insert(0, '1');
                    car = '0';
                }
            }
        }
        assert (sb.length() == 4);
        if (car == '1') this.carry = 1;//是否进位
        return sb.toString();
    }

    /***
     *
     * @param s A String to be justified
     * @return s + "0110"
     */
    private String adjust(String s) {
        return simpleAdd(s, "0110");//注意carry被修改了
    }

    private String BCD_Carry1(String s) {
        for (int i = 0; i < 9; i++) {
            if (s.equals(NBCD_8421[i])) return NBCD_8421[i + 1];
        }
        return "1010";//9的情况单独讨论
    }

    private ArrayList<String> strSplit(String s) {//倒过来
        assert (s.length() == 32);
        ArrayList<String> ret = new ArrayList<>();
        for (int i = 28; i >= 0; i -= 4) {
            ret.add(s.substring(i, i + 4));
        }
        assert (ret.size() == 8);
        return ret;
    }

    private static Boolean containBCD(String s) {
        for (String nbcd : NBCD_8421) {
            if (nbcd.equals(s)) return true;
        }
        return false;
    }

    private int BCD_Index(String s) {
        for (int i = 0; i < 10; i++) {
            if (NBCD_8421[i].equals(s)) return i;
        }
        return -1;
    }

    private DataType subProcess(String s) {
        assert (s.length() == 32);
        ArrayList<String> sArr = strSplit(s);
        StringBuilder res = new StringBuilder();
        try {
            res.insert(0, NBCD_8421[10 - BCD_Index(sArr.get(0))]);
        } catch (ArrayIndexOutOfBoundsException e) {
            res.insert(0, "1010");
        }
        for (int i = 1; i < 7; i++) {
            res.insert(0, NBCD_8421[9 - BCD_Index(sArr.get(i))]);
        }
        String sign = sArr.get(7);
        res.insert(0, sign);
        return new DataType(res.toString());
    }

    private DataType negative(DataType d) {
        String abs = d.toString().substring(4);
        String sign = (d.toString().charAt(3) == '1') ? "1100" : "1101";
        return new DataType(sign + abs);
    }


    /*// 模拟寄存器中的进位标志位
    private String CF = "0";

    // 模拟寄存器中的溢出标志位
    private String OF = "0";

    //add two integer
    private String Myadd(String src, String dest) {
        StringBuilder ans = new StringBuilder();  //因为把ans写成全局的，这里要重置
        int c = 0, s = 0;
        for (int i = 31; i >= 0; i--) {
            int x = src.charAt(i) - '0', y = dest.charAt(i) - '0';
            s = x ^ y ^ c;
            c = (x & c) | (y & c) | (x & y);
            ans.append(s);
        }
        CF = "" + c;
        OF = (src.charAt(0) == dest.charAt(0)) && (src.charAt(0) != ans.charAt(0)) ? "1" : "0";
        //不知道这俩东西有什么用，姑且写一写
        return ans.reverse().toString();
    }

    private String bitAdd(String... args) {
        ALU alu = new ALU();
        StringBuilder ret = new StringBuilder(alu.add(new DataType("0000000000000000000000000000" + args[0]), new DataType("0000000000000000000000000000" + args[1])).toString().substring(28));//防止溢出
        if (args.length == 4) return ret.toString();
        else if (args.length == 3)
            if (args[2].equals("1"))
                ret = new StringBuilder(alu.add(new DataType("0000000000000000000000000000" + ret.toString()), new DataType("00000000000000000000000000000001")).toString().substring(28));
        int x = ret.charAt(1) - '0', y = ret.charAt(2) - '0', z = ret.charAt(3) - '0';
        if (ret.charAt(0) == '1') {
            return alu.add(new DataType("0000000000000000000000000000" + ret.toString()), new DataType("00000000000000000000000000000110")).toString().substring(28);
        } else if ((x & (y | z)) == 1) {
            return "1" + Myadd(ret.toString().substring(1, 5), "0110"); //说明进位
            return "1" + alu.add(new DataType("0000000000000000000000000000" + ret.toString()), new DataType("00000000000000000000000000000110")).toString().substring(28);

        }
        return ret.toString();
    }

    private String reverse(String tar, int bits) {
        tar = tar.substring(32 - bits * 4, 32);//取bits位
        StringBuilder reverse = new StringBuilder();
        for (int i = 0; i <= tar.length() - 4; i += 4) {
            String tmp = bitAdd(tar.substring(i, i + 4), "0110", "0", "not_carry")
                    .substring(1, 5)
                    .replaceAll("1", "2").replaceAll("0", "1").replaceAll("2", "0");
            if (i == tar.length() - 4)
                tmp = bitAdd(tmp, "0001", "0", "not_carry").substring(1, 5);
            reverse.append(tmp);
            //反转时不用处理进位,not_carry
        }
        return "1100" + String.format("%28s", reverse.toString()).replaceAll(" ", "0");  //返回正数化的负数
    }

    private static int getBit(String tar) {
        int cnt = 0;
        for (int i = 4; i < 28; i += 4) {
            if (tar.substring(i, i + 4).equals("0000")) {
                cnt++;
                continue;
            }
            break;
        }
        return 7 - cnt;
    }//算位数


    DataType add(DataType src, DataType dest) {
        String a = src.toString();
        String b = dest.toString();
        StringBuilder ret = new StringBuilder();
        if (a.substring(0, 4).equals(b.substring(0, 4))) {  //同号
            String c = "0";
            for (int i = 31; i > 3; i -= 4) {
                String temp = bitAdd(a.substring(i - 3, i + 1), b.substring(i - 3, i + 1), c);
                if (temp.charAt(0) == '1')
                    c = "1";
                else c = "0";
                ret.insert(0, temp.substring(1, 5));
            }
            return new DataType(ret.insert(0, a.substring(0, 4)).toString());
        } else {
            String temp;
            if (a.substring(0, 4).equals("1100")) {
                temp = a;
                a = b;
                b = temp;
            }
            int bits = Math.max(getBit(a), getBit(b));
            temp = add(new DataType(reverse(a, bits)), new DataType(b)).toString();
            if (temp.substring(28 - 4 * bits, 32 - 4 * bits).equals("0001"))
                return new DataType(temp.replaceFirst("0001", "0000"));
            else
                return new DataType(reverse(temp, bits).replaceFirst("1100", "1101"));
        }
    }

    DataType sub(DataType src, DataType dest) {
        String a = src.toString();
        String b = dest.toString();
        return add(new DataType(a.substring(0, 4).equals("1100") ? "1101" + a.substring(4, 32) : "1100" + a.substring(4, 32)), dest);
    }*/


}
