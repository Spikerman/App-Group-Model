package Controller;

import DataModel.AppData;
import ToolKit.DateFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by chenhao on 3/27/16.
 */
public class DataController {

    public static final int RANK_MIN_NUM = 4;

    //APP被持续监测的最少天数
    public static final int RATE_NUM_MIN_NUM = 5;

    private DbController dbController = new DbController();
    private List<AppData> appDataListForRank = new LinkedList<>();
    private List<AppData> appDataListForRateNum = new LinkedList<>();
    private Map<Date, Set<String>> freeUpMap = new TreeMap<>();
    private Map<Date, Set<String>> paidUpMap = new TreeMap<>();
    private Map<Date, Set<String>> freeDownMap = new TreeMap<>();
    private Map<Date, Set<String>> paidDownMap = new TreeMap<>();
    private Map<String, List<AppData>> appMapForRank = new TreeMap<>();
    private Map<String, List<AppData>> appMapForRateNum = new TreeMap<>();

    public DataController() {
        //initial the rank query statement
        dbController.setRankNumQueryStmt(DbController.rankQuerySql);
    }

    public static void main(String args[]) {
        DataController dataController = new DataController();
        //dataController.getRankAppInfoFromDb().buildRankPatternDateMap();
        List<AppData> tmpList = dataController.getAppRatingNumRecordFromDb("855000728");
        System.out.print(tmpList.size());
    }

    public Map<String, List<AppData>> getAppMapForRateNum() {
        return appMapForRateNum;
    }

    public List<AppData> getAppDataListForRateNum() {
        return appDataListForRateNum;
    }

    public Map<String, List<AppData>> getAppMapForRank() {
        return appMapForRank;
    }

    public Map<Date, Set<String>> getFreeUpMap() {
        return freeUpMap;
    }

    public Map<Date, Set<String>> getPaidUpMap() {
        return paidUpMap;
    }

    public Map<Date, Set<String>> getFreeDownMap() {
        return freeDownMap;
    }

    public Map<Date, Set<String>> getPaidDownMap() {
        return paidDownMap;
    }

    public List<AppData> getAppDataListForRank() {
        return appDataListForRank;
    }

    public DataController getRankAppInfoFromDb() {
        //String selectSql = "SELECT * FROM Data.AppInfo WHERE( appId='1062817956')";

        String selectSql = "SELECT * FROM Data.AppInfo Where(rankType in ('topFreeFlowDown','topFreeFlowUp' ,'topPaidFlowDown' ,'topPaidFlowUp'))";
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start fetch");
            rs = statement.executeQuery(selectSql);
            System.out.println("end fetch");

            while (rs.next()) {
                AppData appData = new AppData();
                appData.appId = rs.getString("appId");
                appData.rankType = rs.getString("rankType");
                appData.ranking = rs.getInt("ranking");
                appData.rankFloatNum = rs.getInt("rankFloatNum");
                appData.date = DateFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                System.out.println(appData.appId + " " + appData.rankType + " " + appData.ranking + " " + appData.rankFloatNum + " " + appData.date);
                appDataListForRank.add((appData));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }

        return this;
    }

    //get every ranking record of certain app given by appId
    public List<AppData> getAppRatingNumRecordFromDb(String appId) {
        ResultSet rs;
        List<AppData> list = new LinkedList<>();
        try {
            dbController.rankNumQueryState.setString(1, appId);
            rs = dbController.rankNumQueryState.executeQuery();
            while (rs.next()) {
                AppData appData = new AppData();
                appData.appId = rs.getString("appId");
                appData.rankType = rs.getString("rankType");
                appData.currentVersion = rs.getString("currentVersion");
                appData.currentVersionReleaseDate = rs.getString("currentVersionReleaseDate");
                appData.userRateCountForCur = rs.getInt("userRatingCountForCurrentVersion");
                appData.userTotalRateCount = rs.getInt("userRatingCount");
                appData.date = DateFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                list.add(appData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //generate appData List for rank num analysis
    public DataController getAppRatingNumInfoFromDb() {
        String selectSql = "SELECT * FROM Data.AppInfo Where rankType ='update'";
        ResultSet rs;
        Statement statement;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start fetch");
            rs = statement.executeQuery(selectSql);
            System.out.println("end fetch");

            while (rs.next()) {
                AppData appData = new AppData();
                appData.appId = rs.getString("appId");
                appData.rankType = rs.getString("rankType");
                appData.currentVersion = rs.getString("currentVersion");
                appData.currentVersionReleaseDate = rs.getString("currentVersionReleaseDate");
                appData.userRateCountForCur = rs.getInt("userRatingCountForCurrentVersion");
                appData.userTotalRateCount = rs.getInt("userRatingCount");
                appData.date = DateFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                appDataListForRateNum.add(appData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return this;
    }

    public List<String> getAllAppIdFromDb() {
        List<String> appIdList = new LinkedList<>();
        String selectSql = "select distinct appId from Data.AppInfo";
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start fetch...");
            rs = statement.executeQuery(selectSql);
            System.out.println("end fetch");

            while (rs.next()) {
                appIdList.add(rs.getString("appId"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appIdList;
    }


    public DataController buildRankPatternDateMap() {
        for (AppData appData : appDataListForRank) {
            if (appData.rankType.equals("topFreeFlowUp")) {
                if (freeUpMap.containsKey(appData.date))
                    freeUpMap.get(appData.date).add(appData.appId);
                else {
                    Set<String> newSet = new HashSet<>();
                    newSet.add(appData.appId);
                    freeUpMap.put(appData.date, newSet);
                }
            } else if (appData.rankType.equals("topPaidFlowUp")) {
                if (paidUpMap.containsKey(appData.date))
                    paidUpMap.get(appData.date).add(appData.appId);
                else {
                    Set<String> newSet = new HashSet<>();
                    newSet.add(appData.appId);
                    paidUpMap.put(appData.date, newSet);
                }
            } else if (appData.rankType.equals("topFreeFlowDown")) {
                if (freeDownMap.containsKey(appData.date))
                    freeDownMap.get(appData.date).add(appData.appId);
                else {
                    Set<String> newSet = new HashSet<>();
                    newSet.add(appData.appId);
                    freeDownMap.put(appData.date, newSet);
                }
            } else {
                if (paidDownMap.containsKey(appData.date))
                    paidDownMap.get(appData.date).add(appData.appId);
                else {
                    Set<String> newSet = new HashSet<>();
                    newSet.add(appData.appId);
                    paidDownMap.put(appData.date, newSet);
                }
            }
        }
        return this;
    }


    //build app map according to the app data list that fetch from the database
    public DataController buildAppDataMapForRank() {
        for (AppData appData : appDataListForRank) {
            if (appMapForRank.containsKey(appData.appId)) {
                appMapForRank.get(appData.appId).add(appData);
            } else {
                List<AppData> newList = new LinkedList<>();
                newList.add(appData);
                appMapForRank.put(appData.appId, newList);
            }
        }

        Iterator iterator = appMapForRank.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            List appDataList = (List) entry.getValue();
            if (appDataList.size() < RANK_MIN_NUM)
                iterator.remove();
        }

        return this;
    }

    public DataController buildAppDataMapForRateNum() {
        for (AppData appData : appDataListForRateNum) {
            if (appMapForRateNum.containsKey(appData.appId)) {
                appMapForRateNum.get(appData.appId).add(appData);
            } else {
                List<AppData> newList = new LinkedList<>();
                newList.add(appData);
                appMapForRateNum.put(appData.appId, newList);
            }
        }

        //若持续监测天数小于最小监测天数值,则从List中剔除该项
        Iterator iterator = appMapForRateNum.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            List appDataList = (List) entry.getValue();
            if (appDataList.size() < RATE_NUM_MIN_NUM)
                iterator.remove();
        }

        return this;
    }


}