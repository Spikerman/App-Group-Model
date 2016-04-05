package Rating;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RankingGroup;
import ToolKit.DateComparator;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * Created by 景舜 on 2016/3/30.
 */
public class RatingAnalysis {
    public Map<String, RankingGroup> ratingGroupMap = new HashMap<>();
    private Map<String, List<AppData>> appDataMap;
    private Map<String, HashMap<Date, Double>> recordMap = new HashMap<>();

    public RatingAnalysis() {
        DataController dataController = new DataController();
        dataController = dataController.buildAppDataListForRatingFromDb().buildAppDataMapForRating();
        appDataMap = dataController.getAppMapForRating();
    }

    public static void main(String[] args) {
        RatingAnalysis ratingAnalysis = new RatingAnalysis();
        ratingAnalysis.startAnalyzing();
    }

    private void generateRecordMap() {
        Iterator iter = appDataMap.entrySet().iterator();
        DateComparator dateComparator = new DateComparator();
        while (iter.hasNext()) {
            Map.Entry item = (Map.Entry) iter.next();
            List<AppData> applist = (List) item.getValue();
            Collections.sort(applist, dateComparator);
            System.out.println("calculate app" + item.getKey());
            HashMap<Date, Double> record = new HashMap<>();
            for (int i = 1; i < applist.size(); i++) {
                AppData app1 = applist.get(i);
                AppData app0 = applist.get(i - 1);
                double delta = app1.minus(app0);
                if (delta != 0) record.put(app1.date, delta);
            }
            if (record.size() > DataController.RATING_MIN_NUM) {
                recordMap.put((String) item.getKey(), record);
            }
        }
    }

    public void makeGroup(HashMap<Date, Double> outerMap, String outerAppId,
                          HashMap<Date, Double> innerMap, String innerAppId) {

        Set<Date> outerDateSet = outerMap.keySet();
        Set<Date> innerDateSet = innerMap.keySet();

        Set<Date> shareDateSet = (Set) Sets.intersection(outerDateSet, innerDateSet);

        int duplicateCount = 0;
        for (Date date : shareDateSet) {
            Double outerRecord = outerMap.get(date);
            Double innerRecord = innerMap.get(date);

            if ((outerRecord > 0 && innerRecord > 0)
                    || (outerRecord < 0 && innerRecord < 0))
                duplicateCount++;
        }


        if (duplicateCount >= DataController.RATING_MIN_NUM) {
            System.out.println(duplicateCount);
            if (ratingGroupMap.containsKey(outerAppId)) {
                RankingGroup rankingGroup = ratingGroupMap.get(outerAppId);
                rankingGroup.getAppIdSet().add(innerAppId);
            } else {
                RankingGroup newGroup = new RankingGroup();
                newGroup.getAppIdSet().add(outerAppId);
                newGroup.getAppIdSet().add(innerAppId);
                ratingGroupMap.put(outerAppId, newGroup);
            }
        }
    }

    public void generateRankingGroup() {
        Object[] outerArray = recordMap.entrySet().toArray();
        Object[] innerArray = recordMap.entrySet().toArray();

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
        generateRecordMap();
        generateRankingGroup();
    }

}
