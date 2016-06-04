package ToolKit;

import DataModel.RankingGroup;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenhao on 4/10/16.
 */
public class Print {

    public static void printEachGroupSize(Map<String, RankingGroup> groupMap) {
        Map map = groupMap;
        Set set = map.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            RankingGroup group = (RankingGroup) entry.getValue();
            System.out.println(group.getAppSize());
        }
    }

    public static Set<String> printEachGroupSize(Map<String, RankingGroup> candidateCluster, int size) {
        Set entrySet = candidateCluster.entrySet();
        int count = 0;
        Set<String> totalAppCount = new HashSet<>();
        Iterator iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            RankingGroup group = (RankingGroup) entry.getValue();
            if (group.getAppSize() >= size) {
                System.out.println(group.getAppSize());
                count += group.getAppSize();
                totalAppCount.addAll(group.getAppIdSet());
            }
        }


        System.out.println("Total count : " + count);
        System.out.println("Total distinct app : " + totalAppCount.size());
        return totalAppCount;
    }


}
