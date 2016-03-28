package Ranking;

import DataModel.RankingGroup;

import java.util.*;

/**
 * Created by chenhao on 3/27/16.
 */
public class RankingAnalysis {
    private DataController dataController;
    private List<RankingGroup> groupList = new LinkedList<>();
    ;

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
        findUpDownPattern();
        findDownUpPattern();
        findUpUpPattern();
        findDownDownPattern();
        resultFilter();
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

    private void findUpDownPattern() {

        //free up and down try catch
        Iterator freeDownIter;
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
                    String groupType = "FreeUpDown";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    group.setDate(upDate, downDate);
                    groupList.add(group);
                }
            }
        }

        //paid up and down try catch
        Iterator paidDownIter;
        Iterator paidUpIter = dataController.getPaidUpMap().entrySet().iterator();
        while (paidUpIter.hasNext()) {
            Map.Entry upEntry = (Map.Entry) paidUpIter.next();
            Set<String> upSet = (HashSet) upEntry.getValue();
            Date upDate = (Date) upEntry.getKey();

            paidDownIter = dataController.getPaidDownMap().entrySet().iterator();
            while (paidDownIter.hasNext()) {
                Map.Entry downEntry = (Map.Entry) paidDownIter.next();
                Set<String> downSet = (HashSet) downEntry.getValue();
                Date downDate = (Date) downEntry.getKey();
                Set<String> interSet = getIntersectionSet(upSet, downSet);
                if (!interSet.isEmpty()) {
                    int dayDiff = dayDiff(downDate, upDate);
                    String groupType = "PaidUpDown";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    group.setDate(upDate, downDate);
                    groupList.add(group);
                }
            }
        }
    }

    private void findDownUpPattern() {
        //free down and up try catch
        Iterator freeUpIter;
        Iterator freeDownIter = dataController.getFreeDownMap().entrySet().iterator();
        while (freeDownIter.hasNext()) {
            Map.Entry downEntry = (Map.Entry) freeDownIter.next();
            Set<String> downSet = (HashSet) downEntry.getValue();
            Date downDate = (Date) downEntry.getKey();
            freeUpIter = dataController.getFreeUpMap().entrySet().iterator();
            while (freeUpIter.hasNext()) {
                Map.Entry upEntry = (Map.Entry) freeUpIter.next();
                Set<String> upSet = (HashSet) upEntry.getValue();
                Date upDate = (Date) upEntry.getKey();
                Set<String> interSet = getIntersectionSet(downSet, upSet);
                if (!interSet.isEmpty()) {
                    int dayDiff = dayDiff(upDate, downDate);
                    String groupType = "FreeDownUp";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    group.setDate(downDate, upDate);
                    groupList.add(group);
                }
            }
        }

        //paid down and up try catch
        Iterator paidUpIter;
        Iterator paidDownIter = dataController.getPaidDownMap().entrySet().iterator();
        while (paidDownIter.hasNext()) {
            Map.Entry downEntry = (Map.Entry) paidDownIter.next();
            Set<String> downSet = (HashSet) downEntry.getValue();
            Date downDate = (Date) downEntry.getKey();
            paidUpIter = dataController.getPaidUpMap().entrySet().iterator();
            while (paidUpIter.hasNext()) {
                Map.Entry upEntry = (Map.Entry) paidUpIter.next();
                Set<String> upSet = (HashSet) upEntry.getValue();
                Date upDate = (Date) upEntry.getKey();
                Set<String> interSet = getIntersectionSet(downSet, upSet);
                if (!interSet.isEmpty()) {
                    int dayDiff = dayDiff(upDate, downDate);
                    String groupType = "PaidDownUp";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    group.setDate(downDate, upDate);
                    groupList.add(group);
                }
            }
        }
    }

    private void findUpUpPattern() {
        //free up up
        Iterator freeUpNextIter;
        Iterator freeUpIter = dataController.getFreeUpMap().entrySet().iterator();
        while (freeUpIter.hasNext()) {
            Map.Entry upEntry = (Map.Entry) freeUpIter.next();
            Set<String> upSet = (HashSet) upEntry.getValue();
            Date upDate = (Date) upEntry.getKey();

            freeUpNextIter = dataController.getFreeUpMap().entrySet().iterator();
            //pass one
            freeUpNextIter.next();
            while (freeUpNextIter.hasNext()) {
                Map.Entry upNextEntry = (Map.Entry) freeUpNextIter.next();
                Set<String> upNextSet = (HashSet) upNextEntry.getValue();
                Date upDateNext = (Date) upNextEntry.getKey();
                Set<String> interSet = getIntersectionSet(upSet, upNextSet);
                if (!interSet.isEmpty()) {
                    int dayDiff = dayDiff(upDateNext, upDate);
                    String groupType = "FreeUpUp";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    group.setDate(upDate, upDateNext);
                    groupList.add(group);
                }
            }
        }

        //paid up and  up catch
        Iterator paidUpNextIter;
        Iterator paidUpIter = dataController.getPaidUpMap().entrySet().iterator();
        while (paidUpIter.hasNext()) {
            Map.Entry upEntry = (Map.Entry) paidUpIter.next();
            Set<String> upSet = (HashSet) upEntry.getValue();
            Date upDate = (Date) upEntry.getKey();
            paidUpNextIter = dataController.getPaidUpMap().entrySet().iterator();
            //pass next
            paidUpNextIter.next();
            while (paidUpNextIter.hasNext()) {
                Map.Entry upNextEntry = (Map.Entry) paidUpNextIter.next();
                Set<String> upNextSet = (HashSet) upNextEntry.getValue();
                Date upDateNext = (Date) upNextEntry.getKey();
                Set<String> interSet = getIntersectionSet(upSet, upNextSet);
                if (!interSet.isEmpty()) {
                    int dayDiff = dayDiff(upDateNext, upDate);
                    String groupType = "PaidUpUp";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    group.setDate(upDate, upDateNext);
                    groupList.add(group);
                }
            }
        }
    }

    private void findDownDownPattern() {
        //free up up
        Iterator freeDownNextIter;
        Iterator freeDownIter = dataController.getFreeDownMap().entrySet().iterator();
        while (freeDownIter.hasNext()) {
            Map.Entry downEntry = (Map.Entry) freeDownIter.next();
            Set<String> downSet = (HashSet) downEntry.getValue();
            Date downDate = (Date) downEntry.getKey();

            freeDownNextIter = dataController.getFreeDownMap().entrySet().iterator();
            //pass one
            freeDownNextIter.next();
            while (freeDownNextIter.hasNext()) {
                Map.Entry downNextEntry = (Map.Entry) freeDownNextIter.next();
                Set<String> downNextSet = (HashSet) downNextEntry.getValue();
                Date downDateNext = (Date) downNextEntry.getKey();
                Set<String> interSet = getIntersectionSet(downSet, downNextSet);
                if (!interSet.isEmpty()) {
                    int dayDiff = dayDiff(downDateNext, downDate);
                    String groupType = "FreeUpUp";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    group.setDate(downDate, downDateNext);
                    groupList.add(group);
                }
            }
        }

        //paid up and  up catch
        Iterator paidDownNextIter;
        Iterator paidDownIter = dataController.getPaidDownMap().entrySet().iterator();
        while (paidDownIter.hasNext()) {
            Map.Entry downEntry = (Map.Entry) paidDownIter.next();
            Set<String> downSet = (HashSet) downEntry.getValue();
            Date downDate = (Date) downEntry.getKey();

            paidDownNextIter = dataController.getPaidDownMap().entrySet().iterator();
            paidDownNextIter.next();
            while (paidDownNextIter.hasNext()) {
                Map.Entry downNextEntry = (Map.Entry) paidDownNextIter.next();
                Set<String> downNextSet = (HashSet) downNextEntry.getValue();
                Date downDateNext = (Date) downNextEntry.getKey();
                Set<String> interSet = getIntersectionSet(downSet, downNextSet);
                if (!interSet.isEmpty()) {
                    int dayDiff = dayDiff(downDateNext, downDate);
                    String groupType = "PaidDownDown";
                    RankingGroup group = new RankingGroup(groupType, dayDiff, interSet);
                    group.setDate(downDate, downDateNext);
                    groupList.add(group);
                }
            }
        }
    }

    public void resultFilter() {
//        List<RankingGroup> tmpList = new LinkedList<>();
//        for (int i = 0; i < groupList.size(); i++) {
//            if (groupList.get(i).dateDiffNum < 0)
//                tmpList.add(groupList.get(i));
//        }
//        groupList.removeAll(tmpList);

        Iterator iterator = groupList.iterator();
        while (iterator.hasNext()) {
            RankingGroup group = (RankingGroup) iterator.next();
            if (group.dateDiffNum < 0)
                iterator.remove();
        }
    }

}
