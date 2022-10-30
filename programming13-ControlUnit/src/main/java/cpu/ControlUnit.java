package cpu;

import cpu.instr.all_instrs.InstrFactory;
import cpu.instr.all_instrs.Instruction;
import cpu.registers.EIP;

public class ControlUnit {
    private static int ICC = 0;  //control unit
    //状态机
    private static Instruction instruction;
    private static int currentOpcode = -1;


    public static Object exec() {
        ICC = 0;
        int len = -1;
        while (ICC != 3) {
            //3是中断
            switch (ICC) {
                //fetch instruction
                case 0:
                    // 先取出opcode
                    currentOpcode = CPU.instrFetch(CPU_State.eip.read(), 1);
                    // 根据opcode产生正确的指令
                    ControlUnit.instruction = InstrFactory.getInstr(currentOpcode);
                    // 再由具体的指令取出剩下的部分
                    ControlUnit.instruction.fetchInstr(CPU_State.eip.read(), currentOpcode);

                    assert ControlUnit.instruction != null;
                    if (ControlUnit.instruction.isIndirectAddressing()) {
                        ICC = 1;
                    } else {
                        ICC = 2;
                    }
                    break;
                //indirect addressing
                case 1:
                    ControlUnit.instruction.fetchOperand();
                    ICC = 2;
                    break;
                //execute according to opcode
                case 2:
                    len = ControlUnit.instruction.exec(currentOpcode);
                    if(len == -1) {
                        ICC = 3;//停机
                        break;
                    }
                    ((EIP)CPU_State.eip).plus(len);
                    ICC = 0;
                    break;

            }
        }
        return len;
    }


}
