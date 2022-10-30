package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.instr.decode.Operand;
import cpu.instr.decode.OperandType;
import cpu.registers.CS;
import cpu.registers.EFlag;

import java.util.Arrays;

import static kernel.MainEntry.alu;

public class Sub implements Instruction {

    private MMU mmu = MMU.getMMU();
    private CS cs = (CS) CPU_State.cs;
    private EFlag eflag = (EFlag) CPU_State.eflag;
    private int len;
    private String instr;

    @Override
    public int exec(int opcode) {
        if (opcode == 0x2d) {
            Operand imm = new Operand();
            imm.setVal(instr.substring(8, 40));
            imm.setType(OperandType.OPR_IMM);
            CPU_State.eax.write(alu.sub(imm.getVal(), CPU_State.eax.read()));
        }
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode) {
        len = 8 + 32;//拉取的时候已经确定了指令长度
        instr = String.valueOf(mmu.read(cs.read() + CPU_State.eip.read(), len));
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
