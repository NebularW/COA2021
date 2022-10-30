package memory.cache.cacheReplacementStrategy;

import memory.cache.Cache;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache.getCache().addVisited(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        int useCnt = Integer.MAX_VALUE;
        int rowNo = -1;
        for (int i = start; i < end; i++) {//替换visited最小的
            if (Cache.getCache().getVisited(i) < useCnt) {
                rowNo = i;
                useCnt = Cache.getCache().getVisited(i);
            }
        }
        return rowNo;
    }

    @Override
    public void init(int rowNo, char[] addrTag, char[] input) {
        Cache.getCache().update(rowNo, addrTag, input);
        Cache.getCache().setVisited(rowNo);
    }
}
