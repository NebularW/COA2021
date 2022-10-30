package memory.cache.cacheWriteStrategy;

import memory.Memory;
import memory.cache.Cache;

public abstract class WriteStrategy {

    /**
     * 将数据写入Cache，并且根据策略选择是否修改内存
     *
     * @param rowNO 行号
     * @param input 数据
     */
    public void write(int rowNO, char[] input) {
    }

    /**
     * 修改内存
     *
     */
    public void writeBack(int rowNO) {
    }

    public abstract Boolean isWriteBack();
}
