package ToolKit;

import DataModel.RankingGroup;

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
}
