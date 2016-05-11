package Itegration;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RankingGroup;
import DataModel.RateAmountDiffRecord;
import Ranking.RankingAnalysis;
import RateAmount.RateAmountAnalysis;
import Rating.RatingAnalysis;
import ToolKit.DateFormat;
import ToolKit.Print;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Created by chenhao on 5/10/16.
 */
public class IndicatorIntegration {
    public int count = 0;
    Map<String, RankingGroup> candidateGroupMap = new HashMap<>();
    private RankingAnalysis rankingAnalysis;
    private RateAmountAnalysis rateAmountAnalysis;
    private RatingAnalysis ratingAnalysis;
    private Map<String, List<AppData>> rankRecordMap;
    private Map<String, HashMap<Date, Double>> ratingRecordMap;
    private Map<String, HashMap<Date, RateAmountDiffRecord>> rateVolumeRecordMap;
    private DataController dataController;
    private int adjustDayDiff = 4;

    public IndicatorIntegration() {
        dataController = new DataController();
        //ranking 必须第一个构造,创建rankAppPool
        rankingAnalysis = new RankingAnalysis(dataController);
        rateAmountAnalysis = new RateAmountAnalysis(dataController);
        ratingAnalysis = new RatingAnalysis(dataController);
    }

    public static void main(String args[]) {
        IndicatorIntegration indicatorIntegration = new IndicatorIntegration();
        indicatorIntegration.getRecordMaps();
        indicatorIntegration.groupConstruction();
        System.out.println(indicatorIntegration.candidateGroupMap.size());
        System.out.println("递归合并");
        indicatorIntegration.mapRecursiveCombine(0.8);
        System.out.println(indicatorIntegration.candidateGroupMap.size());

        System.out.println("--------------------------------------------------------------------");

        Print.printEachGroupSize(indicatorIntegration.candidateGroupMap);
        //导出数据到远程数据库
        //integrationAnalyse.exportGroupData();
    }


    private void getRecordMaps() {
        rankRecordMap = rankingAnalysis.dataController.getAppMapForRank();
        ratingRecordMap = ratingAnalysis.buildDiffRecordMap();
        rateVolumeRecordMap = rateAmountAnalysis.buildDiffRecordMap();
    }

    public void groupConstruction() {
        Object[] outerArray = rankRecordMap.entrySet().toArray();
        Object[] innerArray = rankRecordMap.entrySet().toArray();

        for (int i = 0; i < outerArray.length; i++) {
            for (int j = i + 1; j < innerArray.length; j++) {
                Map.Entry outerEntry = (Map.Entry) outerArray[i];
                Map.Entry innerEntry = (Map.Entry) innerArray[j];
                pairwiseCalculation(outerEntry, innerEntry);
            }
        }
    }

    public void pairwiseCalculation(Map.Entry outerEntry, Map.Entry innerEntry) {

        String outerId = outerEntry.getKey().toString();
        String innerId = innerEntry.getKey().toString();

        int rankCount = 0;
        int ratingCount = 0;
        int volumeCount = 0;

        //ranking
        List<AppData> outerList = (List) outerEntry.getValue();
        List<AppData> innerList = (List) innerEntry.getValue();

        //rating
        HashMap<Date, Double> outerRatingMap = (HashMap) ratingRecordMap.get(outerId);
        HashMap<Date, Double> innerRatingMap = (HashMap) ratingRecordMap.get(innerId);

        //review volume
        HashMap<Date, RateAmountDiffRecord> outerVolumeMap = (HashMap) rateVolumeRecordMap.get(outerId);
        HashMap<Date, RateAmountDiffRecord> innerVolumeMap = (HashMap) rateVolumeRecordMap.get(innerId);

        //rank
        for (int i = 0; i < outerList.size(); i++) {
            for (int j = 0; j < innerList.size(); j++) {
                AppData appA = outerList.get(i);
                AppData appB = innerList.get(j);
                if (appA.rankType.equals(appB.rankType) && appA.date.equals(appB.date))
                    rankCount++;
            }
        }

        //rating
        Set<Date> outerDateSet = outerRatingMap.keySet();
        Set<Date> innerDateSet = innerRatingMap.keySet();
        Set<Date> shareDateSet = (Set) Sets.intersection(outerDateSet, innerDateSet);
        Set<Date> commonDateSet = new HashSet<>();
        for (Date date : shareDateSet) {
            Double outerRateDiff = outerRatingMap.get(date);
            Double innerRateDiff = innerRatingMap.get(date);
            //相同日起时,两个APP的变化趋势相同
            if (outerRateDiff * innerRateDiff > 0) {
                ratingCount++;
                commonDateSet.add(date);
            } else {
                ratingCount += approxEquals(outerRatingMap, innerRatingMap, date);
            }
        }

        //review volume
        double outerAppAvgDiffNum = rateAmountAnalysis.appMetaDataMap.get(outerId).averageDailyRateNum;
        double innerAppAvgDiffNum = rateAmountAnalysis.appMetaDataMap.get(innerId).averageDailyRateNum;
        Set<Date> outerDateSetV = outerVolumeMap.keySet();
        Set<Date> innerDateSetV = innerVolumeMap.keySet();
        Set<Date> shareDateSetV = (Set) Sets.intersection(outerDateSetV, innerDateSetV);
        for (Date date : shareDateSetV) {
            RateAmountDiffRecord outerDiffRecord = outerVolumeMap.get(date);
            RateAmountDiffRecord innerDiffRecord = innerVolumeMap.get(date);
            if ((outerDiffRecord.amountDiff > outerAppAvgDiffNum && innerDiffRecord.amountDiff > innerAppAvgDiffNum))
                volumeCount++;
        }

        boolean rankFlag = false;
        boolean ratingFlag = false;
        boolean volumeFlag = false;

        if (rankCount > dataController.RANK_MIN_NUM)
            rankFlag = true;
        if (ratingCount > dataController.RATING_MIN_NUM)
            ratingFlag = true;
        if (volumeCount > dataController.RATE_NUM_MIN_NUM)
            volumeFlag = true;

        if (rankFlag || (ratingFlag && volumeFlag)) {
            if (candidateGroupMap.containsKey(outerId)) {
                RankingGroup rankingGroup = candidateGroupMap.get(outerId);
                rankingGroup.getAppIdSet().add(innerId);
            } else {
                RankingGroup newGroup = new RankingGroup();
                newGroup.getAppIdSet().add(outerId);
                newGroup.getAppIdSet().add(innerId);
                candidateGroupMap.put(outerId, newGroup);
            }

        }
    }

    public void mapRecursiveCombine(double rate) {
        boolean hasDuplicateSet = false;
        Object[] outerIdSet = candidateGroupMap.keySet().toArray();
        Object[] innerIdSet = candidateGroupMap.keySet().toArray();

        for (int i = 0; i < outerIdSet.length; i++) {
            for (int j = i + 1; j < innerIdSet.length; j++) {
                String outerId = outerIdSet[i].toString();
                String innerId = innerIdSet[j].toString();

                Set<String> outerSet;
                Set<String> innerSet;
                if (candidateGroupMap.containsKey(outerId) && candidateGroupMap.containsKey(innerId)) {
                    outerSet = candidateGroupMap.get(outerId).getAppIdSet();
                    innerSet = candidateGroupMap.get(innerId).getAppIdSet();

                    int outerGroupSize = outerSet.size();
                    int innerGroupSize = innerSet.size();

                    if (outerSet.containsAll(innerSet)
                            || innerSet.containsAll(outerSet)
                            || enableCombine(innerSet, outerSet, rate)) {
                        if (outerGroupSize > innerGroupSize) {
                            outerSet.addAll(innerSet);
                            candidateGroupMap.remove(innerId);

                        } else {
                            innerSet.addAll(outerSet);
                            candidateGroupMap.remove(outerId);
                        }
                        hasDuplicateSet = true;
                    }
                }
            }
        }

        if (hasDuplicateSet)
            mapRecursiveCombine(rate);
    }

    private boolean enableCombine(Set<String> setA, Set<String> setB, double rate) {
        Set<String> unionSet = Sets.union(setA, setB);
        Set<String> intersectionSet = Sets.intersection(setA, setB);

        double unionSize = unionSet.size();
        double intersectionSize = intersectionSet.size();

        return (intersectionSize / unionSize) >= rate;
    }


    private int approxEquals(HashMap<Date, Double> outerMap, HashMap<Date, Double> innerMap, Date date) {
        Double outerRateDiff;
        Double innerRateDiff;
        Double innerRateDiff2;
        int count = 0;
        for (int i = 1; i < adjustDayDiff; i++) {
            outerRateDiff = outerMap.get(date);
            innerRateDiff = innerMap.get(DateFormat.adjustDay(date, i));
            innerRateDiff2 = innerMap.get(DateFormat.adjustDay(date, -i));
            if ((innerRateDiff != null && innerRateDiff * outerRateDiff > 0)
                    || innerRateDiff2 != null && innerRateDiff2 * outerRateDiff > 0) {
                return ++count;
            }
        }
        return count;
    }
}



























