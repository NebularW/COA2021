package cpu.instr;

/**
 * @Author Morgan Stanton 浮世野指针
 * @CreateTime 2021-12-12-0:46
 */

import cpu.CPU_State;
import cpu.alu.ALU;
import cpu.mmu.MMU;
import util.DataType;

public class Jmp implements Instruction {

    private final MMU mmu = MMU.getMMU();
    private int len = 0;
    private String instr;
    private ALU alu = new ALU();

    @Override
    public int exec(int opcode) {
        if (opcode == 0xe9) {
            len = 1 + 4;
            instr = String.valueOf(mmu.read(CPU_State.cs.read() + CPU_State.eip.read(), len));
            String rel = MMU.ToBitStream(instr.substring(1, 5));
            String res = alu.add(new DataType(rel), new DataType(CPU_State.eip.read())).toString();
            CPU_State.eip.write(res);
        }
        return len;
    }

}