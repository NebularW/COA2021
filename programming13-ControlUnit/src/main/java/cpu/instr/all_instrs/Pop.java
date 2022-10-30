package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.alu.ALU;
import cpu.registers.Register;
import memory.Memory;

/*
只支持eax ecx ebx三个寄存器
*/
public class Pop implements Instruction{
    private Memory memory = Memory.getMemory();

    @Override
    public int exec(int opcode) {
        ALU alu = new ALU();
        int len = -1;

        // imm32 处理32位立即数
        if (opcode >= 0x58 && opcode <= 0x5f) {
            len = 8;
            int reg = opcode & 0x7; //确定存储的位置

            Register register = CPU_State.eax;
            //TODO
            if(reg == 0)  register = CPU_State.eax;
            else if(reg == 1) register = CPU_State.ecx;
            else if(reg == 2) register = CPU_State.edx;

            register.write(memory.topOfStack(CPU_State.esp.read()));
            //调整栈指针
            CPU_State.esp.write(alu.add("0100", CPU_State.esp.read()));
        }
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode) {
        return null;
    }

    @Override
    public boolean isIndirectAddressing() {
        return false;
    }

    @Override
    public void fetchOperand() {

    }
}
