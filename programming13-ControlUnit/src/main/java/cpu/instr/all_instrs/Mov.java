package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.alu.ALU;
import cpu.instr.decode.Operand;
import cpu.instr.decode.OperandType;
import cpu.registers.CS;
import memory.Memory;

import java.util.LinkedList;
import java.util.List;

/**
 * 0xb8 mov_i2r_v
 * MOV immediate word or double into word or double register
 * b8 + rd  MOV reg32,imm32
 */
public class Mov implements Instruction {

    private MMU mmu = MMU.getMMU();
    private CS cs = (CS) CPU_State.cs;
    private int len;
    private String instr;
    private List<Operand> operandList = new LinkedList<>();
    private ALU alu = new ALU();

    @Override
    public int exec(int opcode) {
        if (opcode == 0xb8) {
            Operand imm = new Operand();
            imm.setVal(instr.substring(8, 40));
            imm.setType(OperandType.OPR_IMM);
            CPU_State.eax.write(imm.getVal());
            //写入eax 立即数
        } else if (opcode == 0xc7) {
            //写入内存 计算结果
            //把 第二个操作数存储到displacement（第一个操作数） + ebx 位置上
            String mod = instr.substring(8, 16);
            if (mod.equals("10000011")) {
                String displacement = instr.substring(16, 16 + 32);
                Operand imm = new Operand();
                imm.setVal(instr.substring(16 + 32, 16 + 64));
                imm.setType(OperandType.OPR_IMM);

                String location = alu.add(displacement, CPU_State.ebx.read());

                Memory.getMemory().write(location, 32, imm.getVal().toCharArray());
            }
        } else if(opcode == 0x8b){
            //将字写入寄存器中去
            String mod = instr.substring(8, 16);
            if (mod.equals("10000011")) {
                String displacement = instr.substring(16, 16 + 32);
                String location = alu.add(displacement, CPU_State.ebx.read());
                //写入eax
                CPU_State.eax.write(String.valueOf(mmu.read(CPU_State.ds.read() + location, 32)));
            } else if(mod.equals("10001011")){
                String displacement = instr.substring(16, 16 + 32);
                String location = alu.add(displacement, CPU_State.ebx.read());
                //写入ecx
                CPU_State.ecx.write(String.valueOf(mmu.read(CPU_State.ds.read() + location, 32)));
            }
        } else if(opcode == 0x89){
            String mod = instr.substring(8, 16);
            String displacement = "";
            if (mod.equals("10000011")) {
                displacement = instr.substring(16, 16 + 32);
                String location = alu.add(displacement, CPU_State.ebx.read());
                Memory.getMemory().write(location, 32 , CPU_State.eax.read().toCharArray());
            } else if(mod.equals("10001011")){
                displacement = instr.substring(16, 16 + 32);
                String location = alu.add(displacement, CPU_State.ebx.read());
                Memory.getMemory().write(location, 32 , CPU_State.ecx.read().toCharArray());
            }
        }
        toBinaryStr(instr);
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode) {
        //根据不同操作码来确定到底是哪一类
        if(opcode == 0x8b || opcode == 0x89){
            len = 8 + 8 + 32;
            instr = String.valueOf(mmu.read(cs.read() + CPU_State.eip.read(),
                    len));
        }
        else if (opcode == 0xc7){
            len = 8 + 8 + 32 + 32;
            instr = String.valueOf(mmu.read(cs.read() + CPU_State.eip.read(),
                    len));
        } else if (opcode == 0xb8) {
            len = 8 + 32;
            instr = String.valueOf(mmu.read(cs.read() + CPU_State.eip.read(),
                    len));
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
