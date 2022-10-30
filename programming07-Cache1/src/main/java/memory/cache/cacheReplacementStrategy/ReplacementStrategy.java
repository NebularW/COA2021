package memory.cache.cacheReplacementStrategy;

import memory.cache.cacheWriteStrategy.WriteStrategy;

public abstract class ReplacementStrategy {

    WriteStrategy writeStrategy;

    /**
     * 结合具体的替换策略，进行命中后进行相关操作
     * @param rowNO 行号
     */
    public abstract void hit(int rowNO);

    /**
     * 结合具体的映射策略，在给定范围内对cache中的数据进行替换
     * @param start 起始行
     * @param end 结束行 闭区间
     * @param addrTag tag
     * @param input  数据
     */
    public abstract void replace(int start, int end, char[] addrTag, char[] input);

    public void setWriteStrategy(WriteStrategy writeStrategy) {
        this.writeStrategy = writeStrategy;
    }
}
