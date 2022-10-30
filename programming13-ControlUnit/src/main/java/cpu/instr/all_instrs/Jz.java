package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.instr.decode.Operand;
import cpu.instr.decode.OperandType;
import cpu.registers.CS;
import cpu.registers.EFlag;

/**
 * je_short_  JE rel8 等于跳转 opcodeEntry[0x74]
 * Jump short if equal (ZF=1)
 */
public class Jz implements Instruction {
    private MMU mmu = MMU.getMMU();
    private CS cs = (CS) CPU_State.cs;
    private EFlag eflag = (EFlag) CPU_State.eflag;
    private int len;
    private String eip;
    private String instr;
    private ALU alu = new ALU();

    @Override
    public int exec(int opcode) {
        if (opcode == 0x74) {
            if(eflag.getZF()) { // ZF = 1
                //如果为0则发生跳转
                Operand imm = new Operand();
                imm.setVal("000000000000000000000000" + instr.substring(8, 16));
                imm.setType(OperandType.OPR_IMM);
                CPU_State.eip.write(alu.add(CPU_State.eip.read(), imm.getVal()));
                eflag.setZF(true);
                // 返回值置为0
                len = 0;
            }
        }
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode) {
        if(opcode == 0x74) {
            len = 8 + 8;
            this.eip = eip;
            instr = String.valueOf(mmu.read(cs.read() + CPU_State.eip.read(), len));
        }
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
