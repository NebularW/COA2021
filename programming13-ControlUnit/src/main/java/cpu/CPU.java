package cpu;

import cpu.instr.all_instrs.InstrFactory;
import cpu.instr.all_instrs.Instruction;
import cpu.registers.EIP;
import cpu.registers.Register;
import transformer.Transformer;

public class CPU {

    static Transformer transformer = new Transformer();
    static MMU mmu = MMU.getMMU();


    /**
     * execInstr specific numbers of instructions
     *
     * @param number numbers of instructions
     */
    public int execInstr(long number) {
        // 执行过的指令的总长度
        int totalLen = 0;
        while (number > 0) {
            int instrLength = execInstr();
            if (0 > instrLength) {
                break;
            } else {
                number--;
                totalLen += instrLength;
                ((EIP)CPU_State.eip).plus(instrLength);
            }
        }
        return totalLen;
    }

    /**
     * execInstr a single instruction according to eip value
     */
    private int execInstr() {
        String eip = CPU_State.eip.read();
        int len = decodeAndExecute(eip);
        return len;
    }

    private int decodeAndExecute(String eip) {
        int opcode = instrFetch(eip, 1);
        Instruction instruction = InstrFactory.getInstr(opcode);
        assert instruction != null;

        //exec the target instruction
        int len = instruction.exec(opcode);
        return len;


    }

    /**
     * @param eip
     * @param length opcode的字节数，本作业只使用单字节opcode
     * @return
     */
    public static int instrFetch(String eip, int length) {
        /**
         * 目前默认只有一个数据段
         */
        Register cs = CPU_State.cs;

        // length = length * 8  一个字节8位
        String opcode = String.valueOf(mmu.read(cs.read() + eip, length * 8));
        return Integer.parseInt(transformer.binaryToInt(opcode));
    }

    public void execUntilHlt(){
        // TODO ICC
        ControlUnit.exec();
    }

}

