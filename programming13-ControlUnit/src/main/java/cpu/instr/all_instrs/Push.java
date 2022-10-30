package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.registers.CS;
import cpu.registers.SS;
import memory.Memory;

import static kernel.MainEntry.alu;

public class Push implements Instruction {
    private Memory memory = Memory.getMemory();
    private MMU mmu = MMU.getMMU();
    private CS cs = (CS) CPU_State.cs;
    private String eip;
    private int len;
    private String instr;

    @Override
    public int exec(int opcode) {
        ALU alu = new ALU();
        if (opcode == 0x53) {
            len = 8;
            //TODO 先出栈再进栈
            CPU_State.esp.write(alu.sub("0100", CPU_State.esp.read()));

            memory.pushStack(CPU_State.esp.read(), CPU_State.ebx.read());
        }
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode) {
        len = 8;
        this.eip = eip;
        instr = String.valueOf(mmu.read(cs.read() + eip, len));
        return instr;
    }

    @Override
    public boolean isIndirectAddressing() {
        return false;
    }

    @Override
    public void fetchOperand() {

    }
}
