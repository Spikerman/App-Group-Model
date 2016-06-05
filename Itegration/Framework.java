package Itegration;

import Controller.DataController;
import Controller.DbController;
import DataModel.AppData;
import DataModel.RankingGroup;
import DataModel.RateAmountDiffRecord;
import FIM.FimController;
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


public class Framework {
    public Set<Set<String>> ccSet = new HashSet<>();
    public int collusivePairCount = 0;
    public DataController dataController;
    public int totalRankCount = 0;
    public int totalRatingCount = 0;
    public int totalReviewVolumeCount = 0;
    Map<String, RankingGroup> candidateClusterMap = new HashMap<>();
    private RankingAnalysis rankingAnalysis;
    private RateAmountAnalysis rateAmountAnalysis;
    private RatingAnalysis ratingAnalysis;
    private Map<String, List<AppData>> rankRecordMap;
    private Map<String, HashMap<Date, Double>> ratingRecordMap;
    private Map<String, HashMap<Date, RateAmountDiffRecord>> rateVolumeRecordMap;
    private int adjustDayDiff = 3;

    public Framework() {
        dataController = new DataController();
        System.out.println("======================= 前置条件 ======================= ");

        System.out.println("Unusual Ranking Float Frequency " + dataController.FREQUENCY);
        System.out.println("Rank Threshold : " + dataController.RANK_MIN_NUM);
        System.out.println("Rating Threshold : " + dataController.RATING_MIN_NUM);
        System.out.println("Review Volume Threshold : " + dataController.RATE_NUM_MIN_NUM);

        //ranking 必须第一个构造,创建rankAppPool
        rankingAnalysis = new RankingAnalysis(dataController);
        rateAmountAnalysis = new RateAmountAnalysis(dataController);
        ratingAnalysis = new RatingAnalysis(dataController);
    }

    public static void main(String args[]) {
        Framework framework = new Framework();
        framework.getRecordMaps();
        framework.groupConstruction();
        double jaccardValue = 0.6;

        int candidateLimitSize = 8;

        System.out.println("===================== App Pair ============================ ");
        System.out.println("Collusive pair count: " + framework.collusivePairCount);
        System.out.println("Total rank pair count : " + framework.totalRankCount);
        System.out.println("Total rating pair count : " + framework.totalRatingCount);
        System.out.println("Total review volume pair count: " + framework.totalReviewVolumeCount);

        System.out.println("====================== CandidateClusterCapture 算法 =========================== ");

        System.out.println("递归合并前candidate cluster数 : " + framework.candidateClusterMap.size());
        framework.mapRecursiveCombine(jaccardValue);
        System.out.println("Jaccard Similarity value : " + jaccardValue);
        System.out.println("递归合并后candidate cluster数 : " + framework.candidateClusterMap.size());

        System.out.println("========================= Candidate Cluster ================================ ");


        System.out.println("candidate size 限制 : " + candidateLimitSize);

        DbController db = new DbController();
        FimController fimController = new FimController(db);
        fimController.loadCandidateCluster();

        Set totalAppCount = Print.printEachGroupSize(framework.candidateClusterMap, candidateLimitSize);
        framework.duplicateCount(totalAppCount, fimController, candidateLimitSize);

        framework.makeCandidateClusterSet(candidateLimitSize);

        //导出数据到远程数据库
        //framework.exportToDatabase();
        //System.out.println("");
    }

    private void getRecordMaps() {
        rankRecordMap = rankingAnalysis.dataController.getAppMapForRank();
        ratingRecordMap = ratingAnalysis.buildDiffRecordMap();
        rateVolumeRecordMap = rateAmountAnalysis.buildDiffRecordMap();
    }

    //计算原有APP与新试验结果的差值,以记录试验需要增量更新的APP记录
    public void duplicateCount(Set totalAppCount, FimController fimController, int candidateSize) {

        Set originalClusterAppCount = new HashSet<>();
        for (Map.Entry entry : fimController.appGroupMap.entrySet()) {
            Set set = (Set) entry.getValue();
            originalClusterAppCount.addAll(set);
        }

        for (Map.Entry entry : candidateClusterMap.entrySet()) {
            RankingGroup group = (RankingGroup) entry.getValue();
            Set cc = group.getAppIdSet();

            if (originalClusterAppCount.containsAll(cc) && (cc.size() >= candidateSize)) {
                System.out.println("Yes!!!");
            }
        }


        System.out.println("original app count : " + originalClusterAppCount.size());
        System.out.println("new app count : " + totalAppCount.size());
        Set newSet = Sets.intersection(originalClusterAppCount, totalAppCount);
        System.out.println("common app count : " + newSet.size());


    }


    public void groupConstruction() {
        Object[] outerArray = rankRecordMap.entrySet().toArray();
        Object[] innerArray = rankRecordMap.entrySet().toArray();

        for (int i = 0; i < outerArray.length; i++) {
            for (int j = 0; j < innerArray.length; j++) {
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

        Set<Date> commonDate = new HashSet<>();
        //rank
        for (int i = 0; i < outerList.size(); i++) {
            for (int j = 0; j < innerList.size(); j++) {
                AppData appA = outerList.get(i);
                AppData appB = innerList.get(j);
                if ((appA.rankType.equals(appB.rankType) && appA.date.equals(appB.date)) && !commonDate.contains(appA.date)) {
                    rankCount++;
                    commonDate.add(appA.date);
                }
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
            if ((outerRateDiff > 0) && (innerRateDiff > 0)) {
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
            boolean outerSurgeFlag = false;
            boolean innerSurgeFlag = false;

            double outer = (double) outerDiffRecord.amountDiff / (double) outerAppAvgDiffNum;
            double inner = (double) innerDiffRecord.amountDiff / (double) innerAppAvgDiffNum;
            if (outer > 1.3 && inner > 1.3)
                volumeCount++;
        }

        boolean rankFlag = false;
        boolean ratingFlag = false;
        boolean volumeFlag = false;


        if (rankCount > dataController.RANK_MIN_NUM) {
            totalRankCount += rankCount;
            rankFlag = true;
        }
        if (ratingCount > dataController.RATING_MIN_NUM) {
            totalRatingCount += ratingCount;
            ratingFlag = true;
        }
        if (volumeCount > dataController.RATE_NUM_MIN_NUM) {
            totalReviewVolumeCount += volumeCount;
            volumeFlag = true;
        }

        // if ((rankFlag && ratingFlag) || (volumeFlag && rankFlag) || (volumeFlag && ratingFlag)) {
        if (rankFlag || ratingFlag || volumeFlag) {

            collusivePairCount++;

            if (candidateClusterMap.containsKey(outerId)) {
                RankingGroup rankingGroup = candidateClusterMap.get(outerId);
                rankingGroup.getAppIdSet().add(innerId);
            } else {
                RankingGroup newGroup = new RankingGroup();
                newGroup.getAppIdSet().add(outerId);
                newGroup.getAppIdSet().add(innerId);
                candidateClusterMap.put(outerId, newGroup);
            }

        }

    }

    public void mapRecursiveCombine(double rate) {
        boolean hasDuplicateSet = false;
        Object[] outerIdSet = candidateClusterMap.keySet().toArray();
        Object[] innerIdSet = candidateClusterMap.keySet().toArray();

        for (int i = 0; i < outerIdSet.length; i++) {
            for (int j = i + 1; j < innerIdSet.length; j++) {
                String outerId = outerIdSet[i].toString();
                String innerId = innerIdSet[j].toString();

                Set<String> outerSet;
                Set<String> innerSet;
                if (candidateClusterMap.containsKey(outerId) && candidateClusterMap.containsKey(innerId)) {
                    outerSet = candidateClusterMap.get(outerId).getAppIdSet();
                    innerSet = candidateClusterMap.get(innerId).getAppIdSet();

                    int outerGroupSize = outerSet.size();
                    int innerGroupSize = innerSet.size();

                    if (outerSet.containsAll(innerSet)
                            || innerSet.containsAll(outerSet)
                            || enableCombine(innerSet, outerSet, rate)) {
                        if (outerGroupSize > innerGroupSize) {
                            outerSet.addAll(innerSet);
                            candidateClusterMap.remove(innerId);

                        } else {
                            innerSet.addAll(outerSet);
                            candidateClusterMap.remove(outerId);
                        }
                        hasDuplicateSet = true;
                    }
                }
            }
        }

        if (hasDuplicateSet)
            mapRecursiveCombine(rate);
    }

    public void makeCandidateClusterSet(int size) {
        Object[] groupArray = candidateClusterMap.entrySet().toArray();
        for (int i = 0; i < groupArray.length; i++) {
            Map.Entry entry = (Map.Entry) groupArray[i];
            Set<String> idSet = ((RankingGroup) entry.getValue()).getAppIdSet();
            if (idSet.size() >= size)
                ccSet.add(idSet);
        }
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

    public void exportToDatabase() {
        System.out.println("============== Export To Remote DataBase ============");
        Iterator groupIterator = ccSet.iterator();
        Iterator appIdIterator;
        int clusterId = 1;
        while (groupIterator.hasNext()) {
            Set<String> idSet = (Set<String>) groupIterator.next();
            appIdIterator = idSet.iterator();
            while (appIdIterator.hasNext()) {
                String appId = (String) appIdIterator.next();
                dataController.exportCCToDb(clusterId, appId);
            }
            clusterId++;
        }
    }
}



























