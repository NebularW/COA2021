package memory.disk;

import java.util.ArrayList;
import java.util.Arrays;

public class Scheduler {

    /**
     * 先来先服务算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        int cnt = 0;
        for (int track :
                request) {
            cnt += Math.abs(start - track);
            start = track;
        }
        return ((double)cnt / request.length);
    }

    /**
     * 最短寻道时间优先算法
     *
     * @param start   磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        int cnt = 0;
        int len = request.length;

        while (request.length > 0) {
            int index = getApproximate(start, request);
            cnt += Math.abs(start - request[index]);
            start = request[index];
            request = ArrayCopyDel(index, request);
        }
        return ((double)cnt / len);
    }

    /**
     * 扫描算法
     *
     * @param start     磁头初始位置
     * @param request   请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        int cnt = 0;
        Arrays.sort(request);
        if (request[0] >= start) {
            if (direction) cnt = request[request.length - 1] - start;
            else cnt = start + request[request.length - 1];
        } else if (request[request.length - 1] <= start) {
            if (!direction) cnt = start - request[0];
            else cnt = 510 - start - request[request.length - 1];
        } else {
            if (direction) cnt = 510 - start - request[0];
            else cnt = start + request[request.length - 1];
        }
        return ((double) cnt / request.length);
    }

    private static int getApproximate(int x, int[] src) {
        if (src == null) {
            return -1;
        }
        if (src.length == 1) {
            return 0;
        }
        int minDifference = Math.abs(src[0] - x);
        int minIndex = 0;
        for (int i = 1; i < src.length; i++) {
            int temp = Math.abs(src[i] - x);
            if (temp < minDifference) {
                minIndex = i;
                minDifference = temp;
            }
        }
        return minIndex;
    }

    private int[] ArrayCopyDel(int index, int[] arr) {
        int[] ans = new int[arr.length - 1];
        int j = 0;
        for (int i = 0; i < arr.length; i++) {
            if (i != index) ans[j++] = arr[i];
        }
        return ans;
    }
}
