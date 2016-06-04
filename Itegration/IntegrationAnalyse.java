package Itegration;

import Controller.DataController;
import DataModel.RankingGroup;
import Ranking.RankingAnalysis;
import RateAmount.RateAmountAnalysis;
import Rating.RatingAnalysis;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenhao on 4/5/16.
 */
public class IntegrationAnalyse {
    public Set<Set<String>> groupSet = new HashSet<>();
    private RankingAnalysis rankingAnalysis;
    private RateAmountAnalysis rateAmountAnalysis;
    private RatingAnalysis ratingAnalysis;
    private Map rankGroupMap;
    private Map rateNumGroupMap;
    private Map ratingGroupMap;
    private DataController dataController;

    public IntegrationAnalyse() {
        dataController = new DataController();
        //ranking 必须第一个构造,创建rankAppPool
        rankingAnalysis = new RankingAnalysis(dataController);
        rateAmountAnalysis = new RateAmountAnalysis(dataController);
        ratingAnalysis = new RatingAnalysis(dataController);
    }

    public static void main(String args[]) {
        IntegrationAnalyse integrationAnalyse = new IntegrationAnalyse();
        integrationAnalyse.getRecordMaps(0.8, 0.8, 0.8).integrateGroup(0.8);
        System.out.println("三组指标集成后的总数: " + integrationAnalyse.groupSet.size());

        //System.out.println("递归合并...");
        //integrationAnalyse.recursiveCombine(0.8);
        //System.out.println("递归合并后group size大小: " + integrationAnalyse.getGroupSetSize());
        System.out.println("------------------------------------------");
        integrationAnalyse.printAvg();
        System.out.println("------------------------------------------");

        integrationAnalyse.filterData(30);
        System.out.println("过滤后group size大小: " + integrationAnalyse.getGroupSetSize());
        System.out.println("------------------------------------------");
        integrationAnalyse.printEachGroupSize();


        //导出数据到远程数据库
        //integrationAnalyse.exportToDatabase();
    }

    public int getGroupSetSize() {
        return groupSet.size();
    }

    private IntegrationAnalyse getRecordMaps(double rankRate, double rateNumRate, double ratingRate) {

        //获取评分榜指标数据
        ratingAnalysis.buildDiffRecordMap();
        ratingAnalysis.ratingGroupMapGenerate();
        System.out.println("rating group 合并前Group数: " + ratingAnalysis.ratingGroupMap.size());
        ratingAnalysis.mapRecursiveCombine(ratingRate, ratingAnalysis.ratingGroupMap);
        System.out.println("rating group 合并后Group数: " + ratingAnalysis.ratingGroupMap.size());
        this.ratingGroupMap = ratingAnalysis.ratingGroupMap;


        //获取排行榜指标数据
        rankingAnalysis.rankGroupMapGenerate();
        System.out.println("ranking group 合并前Group数: " + rankingAnalysis.rankGroupMap.size());
        rankingAnalysis.mapRecursiveCombine(rankRate, rankingAnalysis.rankGroupMap);
        System.out.println("ranking group 合并后Group数: " + rankingAnalysis.rankGroupMap.size());
        this.rankGroupMap = rankingAnalysis.rankGroupMap;


        //获取评论数量指标数据
        rateAmountAnalysis.buildDiffRecordMap();
        rateAmountAnalysis.rateNumGroupMapGenerate();
        System.out.println("rate num group 合并前Group数: " + rateAmountAnalysis.rateNumGroupMap.size());
        rateAmountAnalysis.mapRecursiveCombine(rateNumRate, rateAmountAnalysis.rateNumGroupMap);
        System.out.println("rate num group 合并后Group数: " + rateAmountAnalysis.rateNumGroupMap.size());
        this.rateNumGroupMap = rateAmountAnalysis.rateNumGroupMap;
        int total = ratingAnalysis.ratingGroupMap.size() + rankingAnalysis.rankGroupMap.size() + rateAmountAnalysis.rateNumGroupMap.size();
        System.out.println("三组指标集成前的总数: " + total);

        return this;
    }

    public void printEachGroupSize() {
        for (Set set : groupSet) {
            System.out.println(set.size());
        }
    }

    //对三个指标计算出的APP Group进行组合计算
    public IntegrationAnalyse integrateGroup2(double rate) {
        Object[] rankArray = rankGroupMap.entrySet().toArray();
        Object[] rateNumArray = rateNumGroupMap.entrySet().toArray();
        for (int i = 0; i < rankArray.length; i++) {
            for (int j = 0; j < rateNumArray.length; j++) {
                Map.Entry rankEntry = (Map.Entry) rankArray[i];
                Map.Entry rateNumEntry = (Map.Entry) rateNumArray[j];
                RankingGroup rankGroup = (RankingGroup) rankEntry.getValue();
                RankingGroup rateNumGroup = (RankingGroup) rateNumEntry.getValue();
                if (enableCombine(rankGroup.getAppIdSet(), rateNumGroup.getAppIdSet(), rate)) {
                    groupSet.add(rankGroup.getAppIdSet());
                    groupSet.add(rateNumGroup.getAppIdSet());
                }
            }
        }
        System.out.println("group的合并比例为: " + rate);
        System.out.println("递归合并前的 group size: " + groupSet.size());
        return this;
    }

    public IntegrationAnalyse integrateGroup2() {
        Object[] rankArray = rankGroupMap.entrySet().toArray();
        Object[] rateNumArray = rateNumGroupMap.entrySet().toArray();
        for (int i = 0; i < rankArray.length; i++) {
            for (int j = 0; j < rateNumArray.length; j++) {
                Map.Entry rankEntry = (Map.Entry) rankArray[i];
                Map.Entry rateNumEntry = (Map.Entry) rateNumArray[j];
                RankingGroup rankGroup = (RankingGroup) rankEntry.getValue();
                RankingGroup rateNumGroup = (RankingGroup) rateNumEntry.getValue();
                groupSet.add(rankGroup.getAppIdSet());
                groupSet.add(rateNumGroup.getAppIdSet());
            }
        }
        return this;
    }

    public IntegrationAnalyse integrateGroup() {
        Iterator rankIterator = rankGroupMap.entrySet().iterator();
        Iterator rateNumIterator = rateNumGroupMap.entrySet().iterator();
        Iterator ratingIterator = ratingGroupMap.entrySet().iterator();
        while (rankIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) rankIterator.next();
            RankingGroup group = (RankingGroup) entry.getValue();
            groupSet.add(group.getAppIdSet());
        }

        while (rateNumIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) rateNumIterator.next();
            RankingGroup group = (RankingGroup) entry.getValue();
            groupSet.add(group.getAppIdSet());
        }

        while (ratingIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) ratingIterator.next();
            RankingGroup group = (RankingGroup) entry.getValue();
            groupSet.add(group.getAppIdSet());
        }

        return this;
    }

    public IntegrationAnalyse integrateGroup(double rate) {
        Object[] rankArray = rankGroupMap.entrySet().toArray();
        Object[] rateNumArray = rateNumGroupMap.entrySet().toArray();
        Object[] ratingArray = ratingGroupMap.entrySet().toArray();
        for (int i = 0; i < rankArray.length; i++) {
            for (int j = i; j < rateNumArray.length; j++) {
                for (int k = j; k < ratingArray.length; k++) {
                    Map.Entry rankEntry = (Map.Entry) rankArray[i];
                    Map.Entry rateNumEntry = (Map.Entry) rateNumArray[j];
                    Map.Entry ratingEntry = (Map.Entry) ratingArray[k];
//                    RankingGroup rankGroup = (RankingGroup) rankEntry.getValue();
//                    RankingGroup rateNumGroup = (RankingGroup) rateNumEntry.getValue();
//                    RankingGroup ratingGroup = (RankingGroup) ratingEntry.getValue()
                    Set<String> rankSet = ((RankingGroup) rankEntry.getValue()).getAppIdSet();
                    Set<String> rateNumSet = ((RankingGroup) rateNumEntry.getValue()).getAppIdSet();
                    Set<String> ratingSet = ((RankingGroup) ratingEntry.getValue()).getAppIdSet();
                    combineGroup(rankSet, rateNumSet, ratingSet, rate);
                }
            }
        }
        return this;
    }


    //对已建好的group set进行合并操作
    public void recursiveCombine(double rate) {
        boolean hasDuplicateSet = false;
        Object[] outerRankGroupArray = groupSet.toArray();
        Object[] innerRankGroupArray = groupSet.toArray();
        for (int i = 0; i < outerRankGroupArray.length; i++) {
            for (int j = i + 1; j < innerRankGroupArray.length; j++) {

                Set<String> outerSet = (Set<String>) outerRankGroupArray[i];
                Set<String> innerSet = (Set<String>) innerRankGroupArray[j];

                int outerGroupSize = outerSet.size();
                int innerGroupSize = innerSet.size();

                if (outerSet.containsAll(innerSet) || innerSet.containsAll(outerSet) || enableCombine(innerSet, outerSet, rate)) {
                    if (outerGroupSize > innerGroupSize)
                        groupSet.remove(innerSet);
                    else
                        groupSet.remove(outerSet);
                    hasDuplicateSet = true;
                }
            }
        }

        if (hasDuplicateSet)
            recursiveCombine(rate);
    }

    public void recursiveCombineX(double rate) {
        boolean hasDuplicateSet = false;
        Object[] outerRankGroupArray = groupSet.toArray();
        Object[] innerRankGroupArray = groupSet.toArray();
        for (int i = 0; i < outerRankGroupArray.length; i++) {
            for (int j = i + 1; j < innerRankGroupArray.length; j++) {
                Set<String> outerSet = (Set<String>) outerRankGroupArray[i];
                Set<String> innerSet = (Set<String>) innerRankGroupArray[j];
                int outerGroupSize = outerSet.size();
                int innerGroupSize = innerSet.size();
                if (outerSet.containsAll(innerSet) || innerSet.containsAll(outerSet) || enableCombine(innerSet, outerSet, rate)) {
                    if (outerGroupSize > innerGroupSize) {
                        groupSet.remove(innerSet);
                    } else {
                        hasDuplicateSet = true;
                    }
                    groupSet.remove(outerSet);
                }
            }
        }

        if (hasDuplicateSet)
            recursiveCombine(rate);
    }

    public void printAvg() {
        double avg = 0;
        Iterator iterator = groupSet.iterator();
        while (iterator.hasNext()) {
            Set<String> set = (Set) iterator.next();
            avg += set.size();
        }

        System.out.println(avg / groupSet.size());
    }

    public void filterData(int limit) {


        Iterator iterator = groupSet.iterator();
        while (iterator.hasNext()) {
            Set<String> set = (Set) iterator.next();
            if (set.size() < limit)
                iterator.remove();
        }
    }

    private boolean enableCombine(Set<String> setA, Set<String> setB, double rate) {
        Set<String> unionSet = Sets.union(setA, setB);
        Set<String> intersectionSet = Sets.intersection(setA, setB);
        double unionSize = unionSet.size();
        double intersectionSize = intersectionSet.size();
        return (intersectionSize / unionSize) >= rate;
    }

    private Set<String> getCombineSet(Set<String> setA, Set<String> setB, double rate) {
        Set<String> unionSet = Sets.union(setA, setB);
        Set<String> intersectionSet = Sets.intersection(setA, setB);
        double unionSize = unionSet.size();
        double intersectionSize = intersectionSet.size();
        if ((intersectionSize / unionSize) >= rate)
            return unionSet;
        else
            return null;
    }

    //三个集合的重合度计算
    private boolean enableCombine(Set<String> unionSet, Set<String> setA, Set<String> setB, Set<String> setC, double rate) {
        unionSet.addAll(setA);
        unionSet.addAll(setB);
        unionSet.addAll(setC);

        Set<String> tmpIntersect = Sets.intersection(setA, setB);
        Set<String> intersectionSet = Sets.intersection(tmpIntersect, setC);

        double unionSize = unionSet.size();
        double intersectionSize = intersectionSet.size();
        return (intersectionSize / unionSize) >= rate;
    }

    //三个集合的重合度计算, 返回unionSet如果符合条件,否则返回null
    private Set<String> getCombineSet(Set<String> setA, Set<String> setB, Set<String> setC, double rate) {
        Set<String> unionSet = new HashSet<>();
        unionSet.addAll(setA);
        unionSet.addAll(setB);
        unionSet.addAll(setC);
        Set<String> tmpIntersect = Sets.intersection(setA, setB);
        Set<String> intersectionSet = Sets.intersection(tmpIntersect, setC);

        double unionSize = unionSet.size();
        double intersectionSize = intersectionSet.size();
        if ((intersectionSize / unionSize) >= rate)
            return unionSet;
        else
            return null;
    }

    private void combineGroup(Set<String> rankSet, Set<String> rateNumSet, Set<String> ratingSet, double rate) {
//        if (rankSet.containsAll(rateNumSet)) {
//            if (rankSet.containsAll(ratingSet)) {
//                ccSet.add(rankSet);
//                return true;
//            } else {
//                if (enableCombine(rankSet, ratingSet, rate)) {
//                    ccSet.add(Sets.union(rankSet, ratingSet));
//                    return true;
//                }
//            }
//        } else if (rateNumSet.containsAll(rankSet)) {
//            if (rateNumSet.containsAll(ratingSet)) {
//                ccSet.add(rateNumSet);
//                return true;
//            } else {
//                if (enableCombine(rateNumSet, ratingSet, rate)) {
//                    ccSet.add(Sets.union(rateNumSet, ratingSet));
//                    return true;
//                }
//            }
//        } else if (ratingSet.containsAll(rankSet)) {
//            if (ratingSet.containsAll(rateNumSet)) {
//                ccSet.add(ratingSet);
//                return true;
//            } else {
//                if (enableCombine(rankSet, ratingSet, rate)) {
//                    ccSet.add(Sets.union(rankSet, ratingSet));
//                    return true;
//                }
//            }
//        }
        Set<String> unionSet;
        if (rankSet.size() >= rateNumSet.size() && rankSet.size() >= ratingSet.size()) {
            if (!rankSet.containsAll(rateNumSet) && !rankSet.containsAll(ratingSet)) {
                unionSet = getCombineSet(rankSet, rateNumSet, ratingSet, rate);
            } else if (rankSet.containsAll(rateNumSet) && !rankSet.containsAll(ratingSet)) {
                unionSet = getCombineSet(rankSet, ratingSet, rate);
            } else if (!rankSet.containsAll(rateNumSet) && rankSet.containsAll(ratingSet)) {
                unionSet = getCombineSet(rankSet, rateNumSet, rate);
            } else {
                unionSet = rankSet;
            }
            if (unionSet != null) {
                groupSet.add(unionSet);
            } else {
                groupSet.add(rankSet);
                // ccSet.add(rateNumSet);
                // ccSet.add(ratingSet);
            }
        } else if (rateNumSet.size() >= rankSet.size() && rateNumSet.size() >= ratingSet.size()) {
            if (!rateNumSet.containsAll(rankSet) && !rateNumSet.containsAll(ratingSet)) {
                unionSet = getCombineSet(rankSet, rateNumSet, ratingSet, rate);
            } else if (rateNumSet.containsAll(rankSet) && !rateNumSet.containsAll(ratingSet)) {
                unionSet = getCombineSet(rateNumSet, ratingSet, rate);
            } else if (!rateNumSet.containsAll(rankSet) && rateNumSet.containsAll(ratingSet)) {
                unionSet = getCombineSet(rateNumSet, rankSet, rate);
            } else {
                unionSet = rateNumSet;
            }
            if (unionSet != null) {
                groupSet.add(unionSet);
            } else {
                groupSet.add(rankSet);
                //ccSet.add(rateNumSet);
                //ccSet.add(ratingSet);
            }
        } else {
            if (!ratingSet.containsAll(rankSet) && !ratingSet.containsAll(rateNumSet)) {
                unionSet = getCombineSet(rankSet, rateNumSet, ratingSet, rate);
            } else if (ratingSet.containsAll(rankSet) && !ratingSet.containsAll(rateNumSet)) {
                unionSet = getCombineSet(ratingSet, rateNumSet, rate);
            } else if (!ratingSet.containsAll(rankSet) && ratingSet.containsAll(rateNumSet)) {
                unionSet = getCombineSet(ratingSet, rankSet, rate);
            } else {
                unionSet = ratingSet;
            }
            if (unionSet != null) {
                groupSet.add(unionSet);
            } else {
                groupSet.add(rankSet);
                // ccSet.add(rateNumSet);
                // ccSet.add(ratingSet);
            }
        }
    }


    public void exportGroupData() {
        System.out.println("------------------------");
        System.out.println("Start to export...");
        Iterator groupIterator = groupSet.iterator();
        Iterator appIdIterator;
        int groupNum = 0;
        while (groupIterator.hasNext()) {
            Set<String> idSet = (Set<String>) groupIterator.next();
            appIdIterator = idSet.iterator();
            while (appIdIterator.hasNext()) {
                String appId = (String) appIdIterator.next();
                System.out.println(groupNum + "  " + appId);
                dataController.exportAppGroupToDb(groupNum, appId);
            }
            groupNum++;
        }
    }
}
