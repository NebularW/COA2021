package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;

/**
 * TODO 先进先出算法
 */
public class FIFOReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        timeFlow();
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        long MaxTime = -1;
        int rowNo = -1;
        for (int i = start; i < end; i++) {//找到时间最久的
            if (Cache.getCache().getTimeStamp(i) > MaxTime) {
                rowNo = i;
                MaxTime = Cache.getCache().getTimeStamp(i);
            }
        }
        return rowNo;
    }

    @Override
    public void init(int rowNo, char[] addrTag, char[] input) {
        timeFlow();
        Cache.getCache().update(rowNo, addrTag, input);
        Cache.getCache().setTimeStamp(rowNo);//新行时间戳置为0
    }

    private void timeFlow() {//对cache每行时间戳进行调整
        Cache.getCache().changeTimeStamp();
    }
}
