package Itegration;

import Controller.DataController;
import DataModel.AppCluster;
import DataModel.AppData;
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

//启动函数
public class Framework {
    public Set<Set<String>> appClusterSet = new HashSet<>();// set of Targeted App Cluster
    public int collusivePairCount = 0;
    public DataController dataController;
    public int totalRankCount = 0;
    public int totalRatingCount = 0;
    public int totalReviewVolumeCount = 0;
    Map<String, AppCluster> AppClusterMap = new HashMap<>();//包含各个 TAC 的 HashMap
    private int adjustDayDiff = 3;
    private RankingAnalysis rankingAnalysis;
    private RateAmountAnalysis rateAmountAnalysis;
    private RatingAnalysis ratingAnalysis;
    private Map<String, List<AppData>> rankRecordMap;
    private Map<String, HashMap<Date, Double>> ratingRecordMap;
    private Map<String, HashMap<Date, RateAmountDiffRecord>> rateVolumeRecordMap;

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
        getRecordMaps();
        groupConstruction();
    }

    public static void main(String args[]) {
        Framework framework = new Framework();
        double jaccardValue = 0.5;
        int candidateLimitSize = 20;
        System.out.println("===================== App Pair ============================ ");
        System.out.println("Collusive pair count: " + framework.collusivePairCount);
        System.out.println("Total rank pair count : " + framework.totalRankCount);
        System.out.println("Total rating pair count : " + framework.totalRatingCount);
        System.out.println("Total review volume pair count: " + framework.totalReviewVolumeCount);
        System.out.println("====================== CandidateClusterCapture 算法 =========================== ");
        System.out.println("递归合并前candidate cluster数 : " + framework.AppClusterMap.size());
        framework.clusterCombine(jaccardValue);
        System.out.println("Jaccard Similarity value : " + jaccardValue);
        System.out.println("递归合并后candidate cluster数 : " + framework.AppClusterMap.size());
        System.out.println("========================= Candidate Cluster ================================ ");
        System.out.println("candidate size 限制 : " + candidateLimitSize);
        Set totalApps = Print.printEachGroupSize(framework.AppClusterMap, candidateLimitSize);


        //以下为本地数据库与远程数据库中 app cluster 数据的数据比较部分
//        DbController db = new DbController();
//        FimController fimController = new FimController(db);
//        fimController.loadClusterMapFromLocalDb();
//        framework.duplicateCount(totalApps, fimController, candidateLimitSize);
//        framework.buildAppClusterSet(candidateLimitSize);
//        framework.exportToRemoteDb();//导出各个 app cluster 到远程数据库
    }

    private void getRecordMaps() {
        rankRecordMap = rankingAnalysis.dataController.getAppMapForRank();
        ratingRecordMap = ratingAnalysis.buildDiffRecordMap();
        rateVolumeRecordMap = rateAmountAnalysis.buildDiffRecordMap();
    }

    //计算原有APP与新试验结果的差值,以记录试验需要增量更新的APP记录
    public void duplicateCount(Set newTotalApps, FimController fimController, int candidateSize) {
        Set oldTotalApps = new HashSet<>();
        for (Map.Entry entry : fimController.candidateClusterMap.entrySet()) {//计算数据库中原有的APP总数
            Set set = (Set) entry.getValue();
            oldTotalApps.addAll(set);
        }
        for (Map.Entry entry : AppClusterMap.entrySet()) {
            AppCluster cluster = (AppCluster) entry.getValue();
            Set clusterAppSet = cluster.getAppIdSet();
            if (oldTotalApps.containsAll(clusterAppSet) && (clusterAppSet.size() >= candidateSize)) {
                System.out.println("Yes!!!");
            }
        }
        System.out.println("old total app count : " + oldTotalApps.size());
        System.out.println("new total app count : " + newTotalApps.size());
        Set newSet = Sets.intersection(oldTotalApps, newTotalApps);
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
            //dataController.exportAppPairToDb(innerId, outerId, "collusive");
            if (AppClusterMap.containsKey(outerId)) {
                AppCluster appCluster = AppClusterMap.get(outerId);
                appCluster.getAppIdSet().add(innerId);
            } else {
                AppCluster newGroup = new AppCluster();
                newGroup.getAppIdSet().add(outerId);
                newGroup.getAppIdSet().add(innerId);
                AppClusterMap.put(outerId, newGroup);
            }
        } else {
            //dataController.exportAppPairToDb(innerId, outerId, "normal");
        }

    }

    //对初步计算出的 TAC 进行递归合并
    public void clusterCombine(double rate) {
        boolean hasDuplicateSet = false;
        Object[] outerIdSet = AppClusterMap.keySet().toArray();
        Object[] innerIdSet = AppClusterMap.keySet().toArray();

        for (int i = 0; i < outerIdSet.length; i++) {
            for (int j = i + 1; j < innerIdSet.length; j++) {
                String outerId = outerIdSet[i].toString();
                String innerId = innerIdSet[j].toString();
                Set<String> outerSet;
                Set<String> innerSet;
                if (AppClusterMap.containsKey(outerId) && AppClusterMap.containsKey(innerId)) {
                    outerSet = AppClusterMap.get(outerId).getAppIdSet();
                    innerSet = AppClusterMap.get(innerId).getAppIdSet();

                    int outerGroupSize = outerSet.size();
                    int innerGroupSize = innerSet.size();

                    if (outerSet.containsAll(innerSet)
                            || innerSet.containsAll(outerSet)
                            || enableCombine(innerSet, outerSet, rate)) {
                        if (outerGroupSize > innerGroupSize) {
                            outerSet.addAll(innerSet);
                            AppClusterMap.remove(innerId);

                        } else {
                            innerSet.addAll(outerSet);
                            AppClusterMap.remove(outerId);
                        }
                        hasDuplicateSet = true;
                    }
                }
            }
        }
        if (hasDuplicateSet)
            clusterCombine(rate);
    }

    public void buildAppClusterSet(int size) {
        Object[] groupArray = AppClusterMap.entrySet().toArray();
        int totalCount = 0;
        for (int i = 0; i < groupArray.length; i++) {
            Map.Entry entry = (Map.Entry) groupArray[i];
            Set<String> idSet = ((AppCluster) entry.getValue()).getAppIdSet();
            totalCount += idSet.size();
            if (idSet.size() >= size)
                appClusterSet.add(idSet);
        }
        System.out.println("avg cluster size : " + (float) totalCount / (float) groupArray.length);
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

    //将各组 cluster 的数据输出到远程服务器中
    public void exportToRemoteDb() {
        System.out.println("============== Export To Remote DataBase ============");
        Iterator clusterIterator = appClusterSet.iterator();
        Iterator appIdIterator;
        int clusterId = 1;
        while (clusterIterator.hasNext()) {
            Set<String> idSet = (Set<String>) clusterIterator.next();
            appIdIterator = idSet.iterator();
            while (appIdIterator.hasNext()) {
                String appId = (String) appIdIterator.next();
                dataController.exportClusterToRemoteDb(clusterId, appId);
            }
            clusterId++;
        }
    }
}



























