package Ranking;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RankingGroup;

import java.util.*;

/**
 * Created by chenhao on 3/27/16.
 */
public class RankingAnalysis {
    private DataController dataController;
    private List<RankingGroup> groupList = new LinkedList<>();

    private TreeMap<Date, Set<RankingGroup>> endDayMap = new TreeMap<>();
    private TreeMap<Date, Set<RankingGroup>> beginDayMap = new TreeMap<>();
    private TreeMap<String, RankingGroup> rankGroupMap = new TreeMap<>();

    public RankingAnalysis(DataController dataController) {
        this.dataController = dataController;
        dataController.getRankAppInfoFromDb().buildRankPatternDateMap().buildAppDataMapForRank();
    }

    public static void main(String args[]) {
        DataController dataController = new DataController();
        RankingAnalysis rankingAnalysis = new RankingAnalysis(dataController);
        rankingAnalysis.rankGroupMapGenerate();

        double rate = 0.5;

        rankingAnalysis.mapRecursiveCombine(rate);

        Iterator iterator = rankingAnalysis.getRankGroupMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            RankingGroup group = (RankingGroup) entry.getValue();
            System.out.println(group.getAppSize());
        }
        Map<String, RankingGroup> rankingGroupMap = rankingAnalysis.getRankGroupMap();
    }

    public TreeMap<String, RankingGroup> getRankGroupMap() {
        return rankGroupMap;
    }

    public void getGroupByRank() {

        findUpDownPattern();
        findDownUpPattern();
        findUpUpPattern();
        findDownDownPattern();

        beginEndMapBuilder(beginDayMap, endDayMap);
        expandGroup(beginDayMap, endDayMap);
    }

    private Set<String> getIntersectionIdSet(Set<String> setA, Set<String> setB) {
        Set<String> tmpSet = new HashSet<>();
        for (String appId : setA) {
            if (setB.contains(appId))
                tmpSet.add(appId);
        }
        return tmpSet;
    }

    private Set<Date> getDateIntersectionSet(Set<Date> setA, Set<Date> setB) {
        Set<Date> tmpSet = new HashSet<>();
        for (Date date : setA) {
            if (setB.contains(date))
                tmpSet.add(date);
        }

        if (tmpSet.isEmpty())
            return null;
        else
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


    //include same day up and down
    //insert new group object into the groupList
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
                Set<String> interSet = getIntersectionIdSet(upSet, downSet);
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
                Set<String> interSet = getIntersectionIdSet(upSet, downSet);
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

    //exclude same day up down
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
                Set<String> interSet = getIntersectionIdSet(downSet, upSet);
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

            // pass one
            paidUpIter.next();
            while (paidUpIter.hasNext()) {
                Map.Entry upEntry = (Map.Entry) paidUpIter.next();
                Set<String> upSet = (HashSet) upEntry.getValue();
                Date upDate = (Date) upEntry.getKey();
                Set<String> interSet = getIntersectionIdSet(downSet, upSet);
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
                Set<String> interSet = getIntersectionIdSet(upSet, upNextSet);
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
                Set<String> interSet = getIntersectionIdSet(upSet, upNextSet);
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
                Set<String> interSet = getIntersectionIdSet(downSet, downNextSet);
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
                Set<String> interSet = getIntersectionIdSet(downSet, downNextSet);
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

    //use group list to generate two map, the begin day map and the end day map.
    public void beginEndMapBuilder(Map<Date, Set<RankingGroup>> beginDayMap, Map<Date, Set<RankingGroup>> endDayMap) {

        //remove duplicate data and build tree
        Iterator iterator = groupList.iterator();
        while (iterator.hasNext()) {
            RankingGroup group = (RankingGroup) iterator.next();
            if (group.dateDiffNum < 0 || ((group.groupType.equals("FreeDownUp") || group.groupType.equals("PaidDownUp")) && group.dateDiffNum == 0)) {
                iterator.remove();
            } else {

                if (beginDayMap.containsKey(group.getBeginDate())) {
                    beginDayMap.get(group.getBeginDate()).add(group);
                } else {
                    Set<RankingGroup> newSet = new HashSet<>();
                    newSet.add(group);
                    beginDayMap.put(group.getBeginDate(), newSet);
                }

                if (endDayMap.containsKey(group.getEndDate())) {
                    endDayMap.get(group.getEndDate()).add(group);
                } else {
                    Set<RankingGroup> newSet = new HashSet<>();
                    newSet.add(group);
                    endDayMap.put(group.getEndDate(), newSet);
                }
            }


        }

    }

    public void beginEndMapBuilder(List<RankingGroup> list, Map<Date, Set<RankingGroup>> beginDayMap, Map<Date, Set<RankingGroup>> endDayMap) {
        //remove duplicate data and build tree
        if (list.isEmpty())
            return;

        //clear the original elements
        beginDayMap.clear();
        endDayMap.clear();

        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            RankingGroup group = (RankingGroup) iterator.next();

            if (beginDayMap.containsKey(group.getBeginDate())) {
                beginDayMap.get(group.getBeginDate()).add(group);
            } else {
                Set<RankingGroup> newSet = new HashSet<>();
                newSet.add(group);
                beginDayMap.put(group.getBeginDate(), newSet);
            }

            if (endDayMap.containsKey(group.getEndDate())) {
                endDayMap.get(group.getEndDate()).add(group);
            } else {
                Set<RankingGroup> newSet = new HashSet<>();
                newSet.add(group);
                endDayMap.put(group.getEndDate(), newSet);
            }
        }
    }


    // return the intersection of endGroup and beginGroup, return null if intersect nothing
    private RankingGroup getCombineGroup(RankingGroup endGroup, RankingGroup beginGroup) {
        RankingGroup mixedGroup = new RankingGroup();
        Set<String> intersectionSet = getIntersectionIdSet(endGroup.getAppIdSet(), beginGroup.getAppIdSet());

        if (intersectionSet.isEmpty())
            return null;

        mixedGroup.setAppIdSet(intersectionSet);
        mixedGroup.setDate(endGroup.getBeginDate(), beginGroup.getEndDate());
        mixedGroup.dateDiffNum = endGroup.dateDiffNum + beginGroup.dateDiffNum;
        return mixedGroup;
    }

    //combine the group with same end day and begin day
    private void expandGroup(Map<Date, Set<RankingGroup>> entryBeginDayMap, Map<Date, Set<RankingGroup>> entryEndDayMap) {
        Iterator beginDatIter;
        Iterator endDatIter = entryEndDayMap.entrySet().iterator();
        List<RankingGroup> tmpGroupList = new LinkedList<>();
        endDatePlusLoop:
        while (endDatIter.hasNext()) {
            beginDatIter = entryBeginDayMap.entrySet().iterator();
            Map.Entry endEntry = (Map.Entry) endDatIter.next();
            Date endDate = (Date) endEntry.getKey();

            Map.Entry beginEntry;
            Date beginDate;
            if (beginDatIter.hasNext()) {

                beginDatIter.next();
                beginEntry = (Map.Entry) beginDatIter.next();
                beginDate = (Date) beginEntry.getKey();

                while (!beginDate.equals(endDate)) {
                    if (beginDatIter.hasNext()) {
                        beginDatIter.next();
                        beginEntry = (Map.Entry) beginDatIter.next();
                        beginDate = (Date) beginEntry.getKey();
                    } else {
                        continue endDatePlusLoop;
                    }
                }
            } else {
                continue;
            }

            Set<RankingGroup> beginGroupSet = (Set) beginEntry.getValue();
            Set<RankingGroup> endGroupSet = (Set) endEntry.getValue();

            Object[] beginGroupArray = beginGroupSet.toArray();
            Object[] endGroupArray = endGroupSet.toArray();

            tmpGroupList = new LinkedList<>();
            for (int i = 0; i < beginGroupArray.length; i++) {
                for (int j = i; j < endGroupArray.length; j++) {
                    RankingGroup beginGroup = (RankingGroup) beginGroupArray[i];
                    RankingGroup endGroup = (RankingGroup) endGroupArray[j];
                    RankingGroup combinedGroup = getCombineGroup(endGroup, beginGroup);
                    if (combinedGroup != null)
                        tmpGroupList.add(combinedGroup);
                }
            }
        }
        beginEndMapBuilder(tmpGroupList, entryBeginDayMap, entryEndDayMap);
    }

    public void expandToAll(Map<Date, Set<RankingGroup>> entryBeginDayMap, Map<Date, Set<RankingGroup>> entryEndDayMap) {

        while (true) {
            Set<Date> endDateSet = entryEndDayMap.keySet();
            Set<Date> beginDateSet = entryBeginDayMap.keySet();
            if (getDateIntersectionSet(endDateSet, beginDateSet) == null)
                return;
            expandGroup(entryBeginDayMap, entryEndDayMap);
        }
    }

    public void rankPatternCombine(List<AppData> outerAppList, String outerAppId, List<AppData> innerAppList, String innerAppId) {

        int duplicateCount = 0;
        Set<Date> dateSet = new HashSet<>();

        for (int i = 0; i < outerAppList.size(); i++) {
            for (int j = i; j < innerAppList.size(); j++) {
                AppData appA = outerAppList.get(i);
                AppData appB = innerAppList.get(j);
                dateSet.add(appA.date);
                dateSet.add(appB.date);
                if (appA.rankType.equals(appB.rankType) && appA.date.equals(appB.date))
                    duplicateCount++;
            }
        }

        if (duplicateCount >= DataController.RANK_MIN_NUM) {
            if (rankGroupMap.containsKey(outerAppId)) {
                RankingGroup rankingGroup = rankGroupMap.get(outerAppId);
                rankingGroup.getAppIdSet().add(innerAppId);
                rankingGroup.commonChangeDateSet.addAll(dateSet);
            } else {
                RankingGroup newGroup = new RankingGroup();
                newGroup.getAppIdSet().add(outerAppId);
                newGroup.getAppIdSet().add(innerAppId);
                newGroup.commonChangeDateSet.addAll(dateSet);
                rankGroupMap.put(outerAppId, newGroup);
            }
        }
    }

    //generate the rankGroupMap that has appId as key, and RankingGroup as element
    public void rankGroupMapGenerate() {
        Map appRankMap = dataController.getAppMapForRank();
        Object[] outerAppRankArray = appRankMap.entrySet().toArray();
        Object[] innerAppRankArrayB = appRankMap.entrySet().toArray().clone();

        for (int i = 0; i < outerAppRankArray.length; i++) {
            for (int j = i + 1; j < innerAppRankArrayB.length; j++) {
                Map.Entry outerEntry = (Map.Entry) outerAppRankArray[i];
                Map.Entry innerEntry = (Map.Entry) innerAppRankArrayB[j];

                String outerId = outerEntry.getKey().toString();
                String innerId = innerEntry.getKey().toString();

                List outerList = (List) outerEntry.getValue();
                List innerList = (List) innerEntry.getValue();
                rankPatternCombine(outerList, outerId, innerList, innerId);
            }
        }
    }

    //combine the generated app rank map, to remove the subset of some entry
    public void mapRecursiveCombine(double rate) {

        boolean hasDuplicateSet = false;
        Object[] outerRankGroupArray = rankGroupMap.entrySet().toArray().clone();
        Object[] innerRankGroupArray = rankGroupMap.entrySet().toArray().clone();

        for (int i = 0; i < outerRankGroupArray.length; i++) {
            for (int j = i + 1; j < innerRankGroupArray.length; j++) {

                Map.Entry outerEntry = (Map.Entry) outerRankGroupArray[i];
                Map.Entry innerEntry = (Map.Entry) innerRankGroupArray[j];

                String outerId = outerEntry.getKey().toString();
                String innerId = innerEntry.getKey().toString();

                RankingGroup outerRankingGroup = (RankingGroup) outerEntry.getValue();
                RankingGroup innerRankingGroup = (RankingGroup) innerEntry.getValue();

                if (outerRankingGroup.getAppSize() > innerRankingGroup.getAppSize()) {
                    if (outerRankingGroup.getAppIdSet().containsAll(innerRankingGroup.getAppIdSet()) || enableCombine(innerRankingGroup.getAppIdSet(), outerRankingGroup.getAppIdSet(), rate)) {
                        rankGroupMap.remove(innerId);
                        hasDuplicateSet = true;
                    }
                } else {
                    if (innerRankingGroup.getAppIdSet().containsAll(outerRankingGroup.getAppIdSet()) || enableCombine(innerRankingGroup.getAppIdSet(), outerRankingGroup.getAppIdSet(), rate)) {
                        rankGroupMap.remove(outerId);
                        hasDuplicateSet = true;
                    }
                }
            }
        }
        if (hasDuplicateSet)
            mapRecursiveCombine(rate);
    }

    private boolean enableCombine(Set<String> setA, Set<String> setB, double rate) {
        Set<String> unionSet = new HashSet<>();
        Set<String> intersectionSet = getIntersectionIdSet(setA, setB);
        unionSet.addAll(setA);
        unionSet.addAll(setB);

        double unionSize = unionSet.size();
        double intersectionSize = intersectionSet.size();

        return (intersectionSize / unionSize) >= rate;
    }
}
