package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.instr.decode.Operand;
import cpu.instr.decode.OperandType;
import cpu.registers.CS;

public class And implements Instruction{
    private MMU mmu = MMU.getMMU();
    private CS cs = (CS) CPU_State.cs;
    private static String src;
    private static String dest;
    private int len;
    private String instr;

    @Override
    public int exec(int opcode) {
        ALU alu = new ALU();
        if (opcode == 0x25) {
            Operand imm = new Operand();
            imm.setVal(instr.substring(8, 40));
            imm.setType(OperandType.OPR_IMM);

            dest = CPU_State.eax.read();
            src = imm.getVal();
            CPU_State.eax.write(alu.and(imm.getVal(), CPU_State.eax.read()));
        }
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode){
        len = 8 + 32;
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