package cpu.instr;


import cpu.CPU;
import cpu.CPU_State;
import kernel.MainEntry;
import memory.Disk;
import memory.Memory;
import org.junit.Test;

import static org.junit.Assert.*;

public class ICCTest {

    @Test
    public void file_test1() {
        String[] args = {"static/icc_test_1.txt"};
        String a = "00000000000000000000000000000100";
        String b = "11111111111111000110110010110100";
        CPU_State.eax.write(a);
        MainEntry.main(args);
        assertFalse(MainEntry.eflag.getOF());
        //No carry before
        assertEquals(b,
                CPU_State.eax.read());
        MainEntry.memory.clear();
    }

    @Test
    public void file_test2() {
        String[] args = {"static/icc_test_2.txt"};
        String ans = "00000000000000000000000000000000";
        CPU_State.eax.write("00000000000000000000000000000000");
        MainEntry.main(args);
        assertFalse(MainEntry.eflag.getOF());
        assertEquals(ans, CPU_State.eax.read());
        MainEntry.memory.clear();
    }

    @Test
    public void file_test3() {
        String[] args = {"static/icc_test_3.txt"};
        String ans = "11111111111111111111111111111000";
        CPU_State.eax.write("00000000000000000000000000000000");
        MainEntry.main(args);
        assertFalse(MainEntry.eflag.getOF());
        assertEquals(ans, CPU_State.eax.read());
        MainEntry.memory.clear();
    }

    @Test
    public void file_test4() {
        String[] args = {"static/icc_test_4.txt"};
        String ans = "00010000000000000101101011100100";
        CPU_State.eax.write("00000000000000000000000000000000");
        MainEntry.main(args);
        assertFalse(MainEntry.eflag.getOF());
        assertEquals(ans, CPU_State.eax.read());
        MainEntry.memory.clear();
    }

    @Test
    public void file_test5() {
        String[] args = {"static/icc_test_5.txt"};
        String ans = "00101011000000101001001101010000";
        CPU_State.eax.write("00010001000000100000000000000000");
        MainEntry.main(args);
        assertFalse(MainEntry.eflag.getOF());
        assertEquals(ans, CPU_State.eax.read());
        MainEntry.memory.clear();
    }

    @Test
    public void file_test6() {
        String[] args = {"static/icc_test_6.txt"};
        String zero = "00000000000000000000000000000000";
        CPU_State.eax.write(zero);
        MainEntry.main(args);
        assertFalse(MainEntry.eflag.getOF());
        assertEquals(zero, CPU_State.eax.read());
        MainEntry.memory.clear();
    }

}
