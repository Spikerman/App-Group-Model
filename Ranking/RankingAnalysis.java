package Ranking;

import DataModel.RankingGroup;

import java.util.*;

/**
 * Created by chenhao on 3/27/16.
 */
public class RankingAnalysis {
    private DataController dataController;
    private List<RankingGroup> groupList;

    public RankingAnalysis(DataController dataController) {
        this.dataController = dataController;
        dataController.getDataFromDb().buildMap();
    }

    public static void main(String args[]) {
        DataController dataController = new DataController();
        RankingAnalysis rankingAnalysis = new RankingAnalysis(dataController);
        rankingAnalysis.getGroupByRank();
        List<RankingGroup> list = rankingAnalysis.getGroupList();
    }

    public void getGroupByRank() {
        Iterator freeDownIter;
        groupList = new LinkedList<>();

        Iterator freeUpIter = dataController.getFreeUpMap().entrySet().iterator();
        while (freeUpIter.hasNext()) {
            Map.Entry upEntry = (Map.Entry) freeUpIter.next();
            Set<String> upSet = (HashSet) upEntry.getValue();
            Date upDate = (Date) upEntry.getKey();

            freeDownIter = dataController.getFreeDownMap().entrySet().iterator();
            while (freeDownIter.hasNext()) {
                Map.Entry downEntry = (Map.Entry) freeDownIter.next();
                Set<String> downSet = (HashSet) downEntry.getValue();
                Date downDate = (Date) downEntry.getKey();
                Set<String> interSet = getIntersectionSet(upSet, downSet);
                if (!interSet.isEmpty()) {
                    int dayDiff = dayDiff(downDate, upDate);
                    String groupType = "UpAndDown";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    groupList.add(group);
                }
            }
        }
    }

    private Set<String> getIntersectionSet(Set<String> setA, Set<String> setB) {
        Set<String> tmpSet = new HashSet<>();
        for (String appId : setA) {
            if (setB.contains(appId))
                tmpSet.add(appId);
        }
        return tmpSet;
    }

    //return day difference date1-date2
    private int dayDiff(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int day1 = calendar.get(Calendar.DAY_OF_YEAR);
        calendar.setTime(date2);
        int day2 = calendar.get(Calendar.DAY_OF_YEAR);

        return day1 - day2;
    }

    public List<RankingGroup> getGroupList() {
        return groupList;
    }
}
