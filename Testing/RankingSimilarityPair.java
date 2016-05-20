package Testing;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RankingGroup;
import DataModel.RateAmountDiffRecord;
import Ranking.RankingAnalysis;
import RateAmount.RateAmountAnalysis;
import Rating.RatingAnalysis;
import ToolKit.DateFormat;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Created by chenhao on 5/3/16.
 */
public class RankingSimilarityPair {
    public int count = 0;
    public Set<Set<String>> groupSet = new HashSet<>();
    public Map<Integer, Integer> rankDt = new TreeMap<>();
    public Map<Integer, Integer> ratingDt = new TreeMap<>();
    public Map<Integer, Integer> volumeDt = new TreeMap<>();
    Map<String, RankingGroup> candidateGroupMap = new HashMap<>();
    private RankingAnalysis rankingAnalysis;
    private RateAmountAnalysis rateAmountAnalysis;
    private RatingAnalysis ratingAnalysis;
    private Map<String, List<AppData>> rankRecordMap;
    private Map<String, HashMap<Date, Double>> ratingRecordMap;
    private Map<String, HashMap<Date, RateAmountDiffRecord>> rateVolumeRecordMap;
    private DataController dataController;
    private int adjustDayDiff = 3;

    public RankingSimilarityPair() {
        dataController = new DataController();

        //ranking 必须第一个构造,创建rankAppPool
        rankingAnalysis = new RankingAnalysis(dataController);
        rateAmountAnalysis = new RateAmountAnalysis(dataController);
        ratingAnalysis = new RatingAnalysis(dataController);
    }

    public static void main(String args[]) {
        RankingSimilarityPair rankingSimilarityPair = new RankingSimilarityPair();
        rankingSimilarityPair.getRecordMaps();
        rankingSimilarityPair.startCalculate();

        rankingSimilarityPair.exportGroupData();

    }

    private void getRecordMaps() {
        rankRecordMap = rankingAnalysis.dataController.getAppMapForRank();
        ratingRecordMap = ratingAnalysis.buildDiffRecordMap();
        rateVolumeRecordMap = rateAmountAnalysis.buildDiffRecordMap();
    }

    public void startCalculate() {
        Object[] outerArray = rankRecordMap.entrySet().toArray();
        Object[] innerArray = rankRecordMap.entrySet().toArray();

        for (int i = 0; i < outerArray.length; i++) {
            for (int j = 0; j < innerArray.length; j++) {
                Map.Entry outerEntry = (Map.Entry) outerArray[i];
                Map.Entry innerEntry = (Map.Entry) innerArray[j];
                pairwiseDistribution(outerEntry, innerEntry);
            }
        }
    }

    public void pairwiseDistribution(Map.Entry outerEntry, Map.Entry innerEntry) {

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
            if (((outerDiffRecord.amountDiff/outerAppAvgDiffNum>1.3) && (innerDiffRecord.amountDiff/innerAppAvgDiffNum>1.3)))
                volumeCount++;
        }

        if (rankDt.containsKey(rankCount)) {
            Integer x = (Integer) rankDt.get(rankCount);
            x++;
            rankDt.put(rankCount, x);
        } else {
            rankDt.put(rankCount, 1);
        }

        if (ratingDt.containsKey(ratingCount)) {
            Integer x = (Integer) ratingDt.get(ratingCount);
            x++;
            ratingDt.put(ratingCount, x);
        } else {
            ratingDt.put(ratingCount, 1);
        }

        if (volumeDt.containsKey(volumeCount)) {
            Integer x = (Integer) volumeDt.get(volumeCount);
            x++;
            volumeDt.put(volumeCount, x);
        } else {
            volumeDt.put(volumeCount, 1);
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

    public void exportGroupData() {
        System.out.println("------------------------");
        System.out.println("Start to export...");


        for(int i=0;i<31;i++){
            if(rankDt.containsKey(i)){
                int y=rankDt.get(i);
                dataController.insertDistribution(i,y,"rank");
            }else{
                dataController.insertDistribution(i,0,"rank");
            }
            if(ratingDt.containsKey(i)){
                int y=ratingDt.get(i);
                dataController.insertDistribution(i,y,"rating");
            }else{
                dataController.insertDistribution(i,0,"rating");
            }

            if(volumeDt.containsKey(i)){
                int y=volumeDt.get(i);
                dataController.insertDistribution(i,y,"volume");
            }else{
                dataController.insertDistribution(i,0,"volume");
            }

        }


    }

}
