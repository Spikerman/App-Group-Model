package Itegration;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RateAmountDiffRecord;
import Ranking.RankingAnalysis;
import RateAmount.RateAmountAnalysis;
import Rating.RatingAnalysis;
import ToolKit.DateFormat;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Created by chenhao on 5/10/16.
 */
public class IndicatorIntegration {
    public Set<Set<String>> groupSet = new HashSet<>();
    private RankingAnalysis rankingAnalysis;
    private RateAmountAnalysis rateAmountAnalysis;
    private RatingAnalysis ratingAnalysis;
    private Map<String, List<AppData>> rankRecordMap;
    private Map<String, HashMap<Date, Double>> ratingRecordMap;
    private Map<String, HashMap<Date, RateAmountDiffRecord>> rateVolumeRecordMap;
    private DataController dataController;
    private int adjustDayDiff = 3;

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

        System.out.print("hello");

        //导出数据到远程数据库
        //integrationAnalyse.exportGroupData();
    }

    public int getGroupSetSize() {
        return groupSet.size();
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

                String outerId = outerEntry.getKey().toString();
                String innerId = innerEntry.getKey().toString();

                List outerList = (List) outerEntry.getValue();
                List innerList = (List) innerEntry.getValue();

                pairwiseCalculation(outerEntry, innerEntry);
            }
        }
    }

    public void pairwiseCalculation(Map.Entry outerEntry, Map.Entry innerEntry) {

        String outerId = outerEntry.getKey().toString();
        String innerId = innerEntry.getKey().toString();
        int rankCount = 0;
        int ratingCount = 0;
        //ranking
        List<AppData> outerList = (List) outerEntry.getValue();
        List<AppData> innerList = (List) innerEntry.getValue();

        //rating
        HashMap<Date, Double> outerRatingMap = (HashMap) ratingRecordMap.get(outerId);
        HashMap<Date, Double> innerRatingMap = (HashMap) ratingRecordMap.get(innerId);

        //review volume
        HashMap outerVolumeMap = (HashMap) rateVolumeRecordMap.get(outerId);
        HashMap innerVolumeMap = (HashMap) rateVolumeRecordMap.get(innerId);

        for (int i = 0; i < outerList.size(); i++) {
            for (int j = 0; j < innerList.size(); j++) {
                AppData appA = outerList.get(i);
                AppData appB = innerList.get(j);
                if (appA.rankType.equals(appB.rankType) && appA.date.equals(appB.date))
                    rankCount++;
            }
        }

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



























