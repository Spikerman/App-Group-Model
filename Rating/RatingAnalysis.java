package Rating;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RankingGroup;
import ToolKit.DateComparator;
import ToolKit.Print;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Created by 景舜 on 2016/3/30.
 */
public class RatingAnalysis {
    public Map<String, RankingGroup> ratingGroupMap = new HashMap<>();
    private DataController dataController;

    private Map<String, List<AppData>> appDataMap;
    private Map<String, HashMap<Date, Double>> rateRecordMap = new HashMap<>();

//    public RatingAnalysis() {
//        DataController dataController = new DataController();
//        dataController = dataController.buildAppDataListForRatingFromDb().buildAppDataMapForRating();
//        appDataMap = dataController.getAppMapForRating();
//    }

    public RatingAnalysis(DataController dataController) {
        this.dataController = dataController;
        dataController.buildAppDataListForRatingFromDb().buildAppDataMapForRating();
        appDataMap = dataController.getAppMapForRating();
    }

    public static void main(String[] args) {
        DataController dataController = new DataController();
        RatingAnalysis ratingAnalysis = new RatingAnalysis(dataController);
        ratingAnalysis.startAnalyzing();
        System.out.println("递归合并前: rating group size: " + ratingAnalysis.ratingGroupMap.size());
        double rate = 0.8;
        ratingAnalysis.mapRecursiveCombine(rate);
        System.out.println("递归后并后: rating group size: " + ratingAnalysis.ratingGroupMap.size());

        Print.printEachGroupSize(ratingAnalysis.ratingGroupMap);
    }

    public void buildDiffRecordMap() {
        Iterator iterator = appDataMap.entrySet().iterator();
        DateComparator dateComparator = new DateComparator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String appId = (String) entry.getKey();
            List<AppData> appList = (List) entry.getValue();
            Collections.sort(appList, dateComparator);
            HashMap<Date, Double> rateDiffRecordMap = new HashMap<>();
            for (int i = 1; i < appList.size(); i++) {
                AppData preDayAppData = appList.get(i);
                AppData curDayAppData = appList.get(i - 1);
                double rateDiff = preDayAppData.getRateDiff(curDayAppData);
                if (rateDiff != 0)
                    rateDiffRecordMap.put(preDayAppData.date, rateDiff);
            }
            //System.out.println(appId + " " + rateDiffRecordMap.size());
            rateRecordMap.put(appId, rateDiffRecordMap);
        }

//        System.out.println("--------------------------------");
//        System.out.println("有评分变化的APP数" + rateRecordMap.size());
//        System.out.println("--------------------------------");
    }

    public void makeGroup(HashMap<Date, Double> outerMap, String outerAppId,
                          HashMap<Date, Double> innerMap, String innerAppId) {

        Set<Date> outerDateSet = outerMap.keySet();
        Set<Date> innerDateSet = innerMap.keySet();

        Set<Date> shareDateSet = (Set) Sets.intersection(outerDateSet, innerDateSet);

        Set<Date> commonDateSet = new HashSet<>();
        int duplicateCount = 0;
        for (Date date : shareDateSet) {
            Double outerRateDiff = outerMap.get(date);
            Double innerRateDiff = innerMap.get(date);

            if ((outerRateDiff > 0 && innerRateDiff > 0)
                    || (outerRateDiff < 0 && innerRateDiff < 0)) {
                duplicateCount++;
                commonDateSet.add(date);
            }
        }

        if (duplicateCount >= 3) {
            if (ratingGroupMap.containsKey(outerAppId)) {
                RankingGroup ratingGroup = ratingGroupMap.get(outerAppId);
                ratingGroup.getAppIdSet().add(innerAppId);
                ratingGroup.commonChangeDateSet.addAll(commonDateSet);
            } else {
                RankingGroup newGroup = new RankingGroup();
                newGroup.getAppIdSet().add(outerAppId);
                newGroup.getAppIdSet().add(innerAppId);
                newGroup.commonChangeDateSet.addAll(commonDateSet);
                ratingGroupMap.put(outerAppId, newGroup);
            }
        }
    }

    public void ratingGroupMapGenerate() {
        Object[] outerArray = rateRecordMap.entrySet().toArray();
        Object[] innerArray = rateRecordMap.entrySet().toArray();
        for (int i = 0; i < outerArray.length; i++) {
            for (int j = i + 1; j < innerArray.length; j++) {

                Map.Entry outerEntry = (Map.Entry) outerArray[i];
                Map.Entry innerEntry = (Map.Entry) innerArray[j];

                String outerId = outerEntry.getKey().toString();
                String innerId = innerEntry.getKey().toString();

                HashMap outerMap = (HashMap) outerEntry.getValue();
                HashMap innerMap = (HashMap) innerEntry.getValue();

                makeGroup(outerMap, outerId, innerMap, innerId);
            }
        }
    }

    public void startAnalyzing() {
        buildDiffRecordMap();
        ratingGroupMapGenerate();
    }

    public void print(Map<String, RankingGroup> ratingGroupMap) {
        Iterator iter = ratingGroupMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry item = (Map.Entry) iter.next();
            RankingGroup group = (RankingGroup) item.getValue();
            group.print();
        }
    }

    public void print(String appid) {
        HashMap<Date, Double> record = rateRecordMap.get(appid);
        Iterator iter = record.entrySet().iterator();
        System.out.println("App id:" + appid);
        while (iter.hasNext()) {
            Map.Entry item = (Map.Entry) iter.next();
            System.out.println(item.getKey() + "\t" + item.getValue());
        }
    }

    public void mapRecursiveCombine(double rate) {

        boolean hasDuplicateSet = false;
        Object[] outerRankGroupArray = ratingGroupMap.entrySet().toArray();
        Object[] innerRankGroupArray = ratingGroupMap.entrySet().toArray();

        for (int i = 0; i < outerRankGroupArray.length; i++) {
            for (int j = i + 1; j < innerRankGroupArray.length; j++) {

                Map.Entry outerEntry = (Map.Entry) outerRankGroupArray[i];
                Map.Entry innerEntry = (Map.Entry) innerRankGroupArray[j];

                String outerId = outerEntry.getKey().toString();
                String innerId = innerEntry.getKey().toString();

                RankingGroup outerRatingGroup = (RankingGroup) outerEntry.getValue();
                RankingGroup innerRatingGroup = (RankingGroup) innerEntry.getValue();

                int outerGroupSize = outerRatingGroup.getAppSize();
                int innerGroupSize = innerRatingGroup.getAppSize();

                if (outerRatingGroup.getAppIdSet().containsAll(innerRatingGroup.getAppIdSet())
                        || innerRatingGroup.getAppIdSet().containsAll(outerRatingGroup.getAppIdSet())
                        || enableCombine(innerRatingGroup.getAppIdSet(), outerRatingGroup.getAppIdSet(), rate)) {
                    if (outerGroupSize > innerGroupSize)
                        ratingGroupMap.remove(innerId);
                    else
                        ratingGroupMap.remove(outerId);
                    hasDuplicateSet = true;
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

}
