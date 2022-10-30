package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.instr.decode.Operand;
import cpu.instr.decode.OperandType;
import cpu.registers.CS;
import memory.Memory;

import static kernel.MainEntry.alu;
import static kernel.MainEntry.eflag;

public class Cmp implements Instruction {
    private MMU mmu = MMU.getMMU();
    private CS cs = (CS) CPU_State.cs;
    private int len;
    private String instr;

    @Override
    public int exec(int opcode) {
		if (opcode == 0x3D) {
			Operand imm = new Operand();
			imm.setVal(instr.substring(8, 40));
			imm.setType(OperandType.OPR_IMM);

			String result = alu.sub(CPU_State.eax.read(), imm.getVal());
			if (result.equals("00000000000000000000000000000000")) {
			    //如果相等则产生zero
				eflag.setZF(true);
			}
		} else if(opcode == 0x39){
		    //如果是这一类指令则需要确定后面8位是指哪个寄存器
            String mod = instr.substring(8, 16);
            if (mod.equals("11001000")) {
            	String result = alu.sub(CPU_State.ecx.read(), CPU_State.eax.read());
            	eflag.setZF(!result.contains("1"));
            	//SF是指符号位置
            	eflag.setSF(result.startsWith("1"));
            }
        }
		//写入log用
		toBinaryStr(instr);
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode) {
    	if (opcode == 0x3D) {
    		len = 8 + 32;
		} else if (opcode == 0x39) {
    		len = 8 + 8;
		}
        instr = String.valueOf(mmu.read(cs.read() + CPU_State.eip.read(),
                len));
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
