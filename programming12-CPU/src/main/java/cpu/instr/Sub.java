package cpu.instr;

import cpu.CPU_State;
import cpu.alu.ALU;
import cpu.mmu.MMU;
import util.DataType;

/**
 * @Author Morgan Stanton 浮世野指针
 * @CreateTime 2021-12-12-0:03
 */
public class Sub implements Instruction {

    private final MMU mmu = MMU.getMMU();
    private int len = 0;
    private String instr;
    private ALU alu = new ALU();

    @Override
    public int exec(int opcode) {
        if (opcode == 0x2d) {
            len = 1 + 4;
            instr = String.valueOf(mmu.read(CPU_State.cs.read() + CPU_State.eip.read(), len));
            String src = MMU.ToBitStream(instr.substring(1, 5));
            String dest = CPU_State.eax.read();
            String res = alu.sub(new DataType(src), new DataType(dest)).toString();
            CPU_State.eax.write(res);
        }
        return len;
    }

}
