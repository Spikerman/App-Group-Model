package RateAmount;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RateAmountDiffRecord;

import java.util.*;

/**
 * Created by chenhao on 3/30/16.
 */
public class RateAmountAnalysis {
    private DateComparator dateComparator = new DateComparator();
    private DataController dataController;
    private Map<String, List<AppData>> appDataMap = new HashMap<>();
    private Map<String, Set<RateAmountDiffRecord>> diffRecordMap = new HashMap<>();

    public RateAmountAnalysis() {
        dataController = new DataController();
        dataController.getAppRatingNumInfoFromDb().buildAppDataMapForRateNum();
        appDataMap = dataController.getAppMapForRateNum();
    }

    public static void main(String args[]) {
        RateAmountAnalysis rateAmountAnalysis = new RateAmountAnalysis();
        rateAmountAnalysis.dataController.getAppMapForRateNum();
//        Set<RateAmountDiffRecord> set = rateAmountAnalysis.generateDiffSet("855000728");
//        System.out.println(set.size());
        rateAmountAnalysis.buildDiffRecordMap();
        System.out.println("hehe");

    }

    //生成评论差值的hash map, key 是app Id, value是存储着每天差值记录rateAmountDiffRecord的集合
    public void buildDiffRecordMap() {
        Iterator iterator = appDataMap.keySet().iterator();
        Set<RateAmountDiffRecord> set;
        while (iterator.hasNext()) {
            String appId = iterator.next().toString();
            set = generateDiffSet(appId);
            diffRecordMap.put(appId, set);
        }
    }

    //根据输入的app id 值,得到每天评论的差值集合,比如2号-1号,3号-2号...构成的集合
    public Set<RateAmountDiffRecord> generateDiffSet(String appId) {
        Set<RateAmountDiffRecord> set = new HashSet<>();
        List<AppData> list = appDataMap.get(appId);
        Collections.sort(list, dateComparator);

        for (int i = 0, next = 1; next < list.size() && i < list.size(); next++, i++) {
            RateAmountDiffRecord record = new RateAmountDiffRecord();
            record.amountDiff = list.get(next).userTotalRateCount - list.get(i).userTotalRateCount;
            record.date = list.get(next).date;
            record.appId = appId;
            set.add(record);
        }
        return set;
    }

    public static class DateComparator implements Comparator<AppData> {
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
