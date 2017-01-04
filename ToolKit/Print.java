package ToolKit;

import DataModel.AppCluster;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenhao on 4/10/16.
 */
public class Print {

    public static void printEachGroupSize(Map<String, AppCluster> groupMap) {
        Map map = groupMap;
        Set set = map.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            AppCluster group = (AppCluster) entry.getValue();
            System.out.println(group.getAppSize());
        }
    }

    //打印 size 大于指定值的组的各组APP数量
    public static Set<String> printEachGroupSize(Map<String, AppCluster> candidateCluster, int size) {
        Set entrySet = candidateCluster.entrySet();
        int count = 0;
        Set<String> totalApps = new HashSet<>();
        Iterator iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            AppCluster group = (AppCluster) entry.getValue();
            if (group.getAppSize() >= size) {
                System.out.println(group.getAppSize());
                count += group.getAppSize();
                totalApps.addAll(group.getAppIdSet());
            }
        }
        System.out.println("Total count : " + count);
        System.out.println("Total distinct app : " + totalApps.size());
        return totalApps;
    }


}
