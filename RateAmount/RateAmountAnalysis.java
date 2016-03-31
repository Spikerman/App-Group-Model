package RateAmount;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RankingGroup;
import DataModel.RateAmountDiffRecord;
import ToolKit.Combination;

import java.util.*;

/**
 * Created by chenhao on 3/30/16.
 */
public class RateAmountAnalysis {
    public TreeMap<String, RankingGroup> rateNumGroupMap = new TreeMap<>();
    private DateComparator dateComparator = new DateComparator();
    private DataController dataController;
    private Map<String, List<AppData>> appDataMap = new HashMap<>();
    private Map<String, List<RateAmountDiffRecord>> diffRecordMap = new HashMap<>();
    private Map<String, AppData> appMetaDataMap;

    public RateAmountAnalysis() {
        dataController = new DataController();
        dataController.buildAppDataListForRateNumFromDb().buildAppDataMapForRateNum();
        appDataMap = dataController.getAppMapForRateNum();
        appMetaDataMap = dataController.getAppMetaDataMapForRateNum();
    }

    public static void main(String args[]) {
        RateAmountAnalysis rateAmountAnalysis = new RateAmountAnalysis();
        rateAmountAnalysis.dataController.getAppMapForRateNum();
        rateAmountAnalysis.buildDiffRecordMap();
        rateAmountAnalysis.quickRateNumGroupRankGenerate();
        System.out.println(rateAmountAnalysis.rateNumGroupMap.size());
    }

    //生成评论差值的hash map, key 是app Id, value是存储着每天差值记录rateAmountDiffRecord的集合
    private void buildDiffRecordMap() {
        Iterator iterator = appDataMap.keySet().iterator();
        List<RateAmountDiffRecord> list;
        while (iterator.hasNext()) {
            String appId = iterator.next().toString();
            list = generateDiffSet(appId);
            diffRecordMap.put(appId, list);
        }
    }

    //根据输入的app id 值,得到每天评论的差值集合,比如2号-1号,3号-2号...构成的集合
    private List<RateAmountDiffRecord> generateDiffSet(String appId) {
        List<RateAmountDiffRecord> diffList = new LinkedList<>();
        List<AppData> list = appDataMap.get(appId);
        Collections.sort(list, dateComparator);
        int totalDiffAmount = 0;
        for (int i = 0, next = 1; next < list.size() && i < list.size(); next++, i++) {
            RateAmountDiffRecord record = new RateAmountDiffRecord();
            record.amountDiff = list.get(next).userTotalRateCount - list.get(i).userTotalRateCount;
            //若评论数量呈正增长区属,则计入增长总数中
            //对于评论数量下降的异常情况,考虑苹果删除评论的可能性,则不从已有的累计数量中扣除
            if (record.amountDiff > 0)
                totalDiffAmount += record.amountDiff;

            //若评论数为负增长,对相应app的metadata值标记
            if (record.amountDiff < 0) {
                recordRateNumDecreace(appId);
            }
            record.date = list.get(next).date;
            record.appId = appId;
            diffList.add(record);
            appMetaDataMap.get(appId).averageDailyRateNum = (double) totalDiffAmount / (double) list.size();
        }
        return diffList;
    }

    //在app data list里找到指定的app,并标记其内的isRateNumDecrease值
    private void recordRateNumDecreace(String appId) {
        appMetaDataMap.get(appId).hasNumDecrease = true;
    }

    public void rateNumGroupRankGenerate() {

        Object[] outerArray = diffRecordMap.entrySet().toArray();
        Object[] innerArray = diffRecordMap.entrySet().toArray();
        for (int i = 0; i < outerArray.length; i++) {
            for (int j = i + 1; j < innerArray.length; j++) {

                Map.Entry outerEntry = (Map.Entry) outerArray[i];
                Map.Entry innerEntry = (Map.Entry) innerArray[j];

                String outerId = outerEntry.getKey().toString();
                String innerId = innerEntry.getKey().toString();

                List outerList = (List) outerEntry.getValue();
                List innerList = (List) innerEntry.getValue();

                System.out.println("id pair: " + outerId + "  " + innerId);
                rateNumDiffPatternCombine(outerList, outerId, innerList, innerId);

            }
        }
    }

    public void quickRateNumGroupRankGenerate() {
        Object[] keyArray = diffRecordMap.keySet().toArray();
        int[] num = new int[keyArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            num[i] = i;
        }
        List<int[]> keyPair = Combination.combine(num, 2);
        for (int i = 0; i < keyPair.size(); i++) {
            int[] x = keyPair.get(i);
            String appId1 = keyArray[x[0]].toString();
            String appId2 = keyArray[x[1]].toString();
            System.out.println(i);
            rateNumDiffPatternCombine(diffRecordMap.get(appId1), appId1, diffRecordMap.get(appId2), appId2);
        }
//
    }

    private int[] getSubset(int[] input, int[] subset) {
        int[] result = new int[subset.length];
        for (int i = 0; i < subset.length; i++)
            result[i] = input[subset[i]];
        return result;
    }

    private void rateNumDiffPatternCombine(List<RateAmountDiffRecord> outerAppList, String outerAppId, List<RateAmountDiffRecord> innerAppList, String innerAppId) {

        double outerAppAvgDiffNum = appMetaDataMap.get(outerAppId).averageDailyRateNum;
        double innerAppAvgDiffNum = appMetaDataMap.get(innerAppId).averageDailyRateNum;

        int duplicateCount = 0;
        for (int i = 0; i < outerAppList.size(); i++) {
            for (int j = i; j < innerAppList.size(); j++) {
                RateAmountDiffRecord outerDiffRecord = outerAppList.get(i);
                RateAmountDiffRecord innerDiffRecord = innerAppList.get(j);
                if (outerDiffRecord.date.equals(innerDiffRecord.date)) {
                    if ((outerDiffRecord.amountDiff > outerAppAvgDiffNum && innerDiffRecord.amountDiff > innerAppAvgDiffNum)
                            || (outerDiffRecord.amountDiff < outerAppAvgDiffNum && innerDiffRecord.amountDiff < innerAppAvgDiffNum
                            && outerDiffRecord.amountDiff > 0 && innerDiffRecord.amountDiff > 0))
                        duplicateCount++;
                }
            }

        }
        if (duplicateCount >= DataController.RATE_NUM_MIN_NUM) {
            System.out.println(duplicateCount);
            if (rateNumGroupMap.containsKey(outerAppId)) {
                RankingGroup rankingGroup = rateNumGroupMap.get(outerAppId);
                rankingGroup.getAppIdSet().add(innerAppId);
            } else {
                RankingGroup newGroup = new RankingGroup();
                newGroup.getAppIdSet().add(outerAppId);
                newGroup.getAppIdSet().add(innerAppId);
                rateNumGroupMap.put(outerAppId, newGroup);
            }
        }
    }

    private static class DateComparator implements Comparator<AppData> {
        public int compare(AppData app1, AppData app2) {
            Date date1 = app1.date;
            Date date2 = app2.date;
            if (date1.after(date2))
                return 1;
            else if (date2.after(date1))
                return -1;
            else
                return 0;
        }
    }
}
