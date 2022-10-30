package cpu.instr.all_instrs;

import program.Log;

public interface Instruction {

    int exec(int opcode);

    String fetchInstr(String eip, int opcode);

    boolean isIndirectAddressing();

    void fetchOperand();

    default String toBinaryStr(String logInfo) {
        Log.write(logInfo);
        return logInfo;
    }

}
