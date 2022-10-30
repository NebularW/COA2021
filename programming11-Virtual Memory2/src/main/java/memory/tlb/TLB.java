package memory.tlb;

import memory.Memory;
import memory.cache.Cache;

import java.util.Arrays;

public class TLB {

    public static boolean isAvailable = true;   // 默认启用TLB

    public static final int TLB_SIZE = 256;     // 256行 总大小

    private final TLBLine[] TLB = new TLBLine[TLB_SIZE];

    // 单例模式
    private static final TLB tlbInstance = new TLB();
    private static final Memory memory = Memory.getMemory();

    private TLB() {
    }

    public static TLB getTLB() {
        return tlbInstance;
    }

    public int getLineNo(int vPageNo) {
        for (int i = 0; i < TLB_SIZE; i++) {
            TLBLine line = TLB[i];
            if (line == null || !line.valid) continue;
            else {
                if (line.vPageNo == vPageNo) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void process(int vPageNo) {
        if (getLineNo(vPageNo) == -1) {
            if (!memory.isValidPage(vPageNo)) memory.page_load(vPageNo);
            write(vPageNo);
        }
        return;
    }

    public String getFrameNo(int lineNo) {
        return String.valueOf(getTLBLine(lineNo).pageFrame);
    }

    /**
     * 填写TLB
     *
     * @param vPageNo 虚拟页号
     */
    public void write(int vPageNo) {
        long minTime = Long.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < TLB_SIZE; i++) {
            long curTime = getTimeStamp(i);
            if (curTime < minTime) {
                minTime = curTime;
                minIndex = i;
            }
        }
        char[] pageFrame = Memory.getMemory().getFrameOfPage(vPageNo);
        TLBLine tlbLine = getTLBLine(minIndex);
        tlbLine.valid = true;
        tlbLine.vPageNo = vPageNo;
        tlbLine.timeStamp = System.currentTimeMillis();
        System.arraycopy(pageFrame, 0, tlbLine.pageFrame, 0, pageFrame.length);
    }

    /**
     * 清除TLB全部缓存
     */
    public void clear() {
        for (TLBLine tlbLine : TLB) {
            if (tlbLine != null) {
                tlbLine.valid = false;
            }
        }
    }

    /**
     * The PageNumber and PageFrame is stored in TLBLine.
     */
    private static class TLBLine {

        boolean valid = false;  // 有效位

        int vPageNo;    // 虚拟页号

        char[] pageFrame = new char[20];    // 物理页框号

        Long timeStamp = 0L;    // 时间戳
    }

    private TLBLine getTLBLine(int index) {
        if (TLB[index] == null) {
            TLB[index] = new TLBLine();
        }
        return TLB[index];
    }

    // 获取时间戳
    private long getTimeStamp(int row) {
        TLBLine tlbLine = getTLBLine(row);
        if (tlbLine.valid) {
            return tlbLine.timeStamp;
        }
        return -1;
    }

    /**
     * 输入行号和对应的预期值，判断TLB当前状态是否符合预期
     * 这个方法仅用于测试，请勿修改
     *
     * @param lineNOs     行号
     * @param validations 有效值
     * @param vPageNos    虚拟页号
     * @param pageFrames  物理页框号
     * @return 判断结果
     */
    public boolean checkStatus(int[] lineNOs, boolean[] validations, int[] vPageNos, char[][] pageFrames) {
        if (lineNOs.length != validations.length || validations.length != pageFrames.length) {
            return false;
        }
        for (int i = 0; i < lineNOs.length; i++) {
            TLBLine line = getTLBLine(lineNOs[i]);
            if (line.valid != validations[i]) {
                return false;
            }
            if (line.vPageNo != vPageNos[i]) {
                return false;
            }
            if (!Arrays.equals(line.pageFrame, pageFrames[i])) {
                return false;
            }
        }
        return true;
    }

}
