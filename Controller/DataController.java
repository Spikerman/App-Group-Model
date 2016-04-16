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

    //// TODO: 4/13/16 三组参数调整统一,更具有说理性
    public static final int RANK_MIN_NUM = 8;
    public static final int RATE_NUM_MIN_NUM =8;
    public static final int RATING_MIN_NUM = 4;

    private DbController dbController = new DbController();
    private List<AppData> appDataRecordListForRank = new LinkedList<>();
    private List<AppData> appDataRecordListForRateNum = new LinkedList<>();
    private List<AppData> appDataRecordListForRating = new LinkedList<>();
    private Map<Date, Set<String>> freeUpMap = new TreeMap<>();
    private Map<Date, Set<String>> paidUpMap = new TreeMap<>();
    private Map<Date, Set<String>> freeDownMap = new TreeMap<>();
    private Map<Date, Set<String>> paidDownMap = new TreeMap<>();
    private Map<String, List<AppData>> appMapForRank = new HashMap<>();
    private Map<String, List<AppData>> appMapForRating = new HashMap<>();
    private Map<String, List<AppData>> appMapForRateNum = new HashMap<>();
    private Map<String, AppData> appMetaDataMapForRateNum = new HashMap<>();
    private Set<String> rankAppIdPool = new HashSet<>();

    public DataController() {
        //initial the rank query statement
        dbController.setRankNumQueryStmt(DbController.rankQuerySql);
        dbController.setInsertRateNumTestStmt(DbController.insertTestSql);
        dbController.setInsertAppGroupStmt(DbController.insertAppGroupSql);
    }

    public static void main(String args[]) {
        DataController dataController = new DataController();
        List<AppData> tmpList = dataController.getAppRatingNumRecordFromDb("855000728");
        System.out.print(tmpList.size());
    }

    public Map<String, AppData> getAppMetaDataMapForRateNum() {
        return appMetaDataMapForRateNum;
    }

    public Map<String, List<AppData>> getAppMapForRateNum() {
        return appMapForRateNum;
    }

    public List<AppData> getAppDataRecordListForRateNum() {
        return appDataRecordListForRateNum;
    }

    public Map<String, List<AppData>> getAppMapForRank() {
        return appMapForRank;
    }

    public Map<String, List<AppData>> getAppMapForRating() {
        return appMapForRating;
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

    public List<AppData> getAppDataRecordListForRank() {
        return appDataRecordListForRank;
    }

    public DataController getRankAppInfoFromDb() {
        //String selectSql = "SELECT * FROM Data.AppInfo WHERE( appId='1062817956')";

        String selectSql = "SELECT * FROM Data.AppInfo Where(rankType in ('topFreeFlowDown','topFreeFlowUp' ,'topPaidFlowDown' ,'topPaidFlowUp'))";
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start rank data fetch...");
            rs = statement.executeQuery(selectSql);
            System.out.println("end rank data fetch...");

            while (rs.next()) {
                AppData appData = new AppData();
                appData.appId = rs.getString("appId");
                appData.rankType = rs.getString("rankType");
                appData.ranking = rs.getInt("ranking");
                appData.rankFloatNum = rs.getInt("rankFloatNum");
                appData.date = DateFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                //System.out.println(appData.appId + " " + appData.rankType + " " + appData.ranking + " " + appData.rankFloatNum + " " + appData.date);
                appDataRecordListForRank.add((appData));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return this;
    }

    //get every ranking record of certain app given by appId
    public List<AppData> getAppRatingNumRecordFromDb(String appId) {
        ResultSet rs;
        List<AppData> list = new LinkedList<>();
        try {
            dbController.rankNumQueryStmt.setString(1, appId);
            rs = dbController.rankNumQueryStmt.executeQuery();
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

    public void insertTestDataToDb(Date date, int diffA, int diffB, int diffC, double avgA, double avgB, double avgC) {
        try {
            dbController.insertRateNumTestStmt.setDate(1, new java.sql.Date(date.getTime()));
            dbController.insertRateNumTestStmt.setInt(2, diffA);
            dbController.insertRateNumTestStmt.setInt(3, diffB);
            dbController.insertRateNumTestStmt.setInt(4, diffC);
            dbController.insertRateNumTestStmt.setDouble(5, avgA);
            dbController.insertRateNumTestStmt.setDouble(6, avgB);
            dbController.insertRateNumTestStmt.setDouble(7, avgC);
            dbController.insertRateNumTestStmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportAppGroupToDb(int groupId, String appId) {
        try {
            dbController.insertAppGroupStmt.setInt(1, groupId);
            dbController.insertAppGroupStmt.setString(2, appId);
            dbController.insertAppGroupStmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //generate appData List for rank num analysis
    public void buildAppMapForRateNumFromDb() {
        String selectSql = "SELECT * FROM Data.AppInfo";
        ResultSet rs;
        Statement statement;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start rate num data fetch");
            rs = statement.executeQuery(selectSql);
            System.out.println("end rate num data fetch");
            while (rs.next()) {
                AppData appData = new AppData();
                appData.appId = rs.getString("appId");
                appData.rankType = rs.getString("rankType");
                appData.currentVersion = rs.getString("currentVersion");
                appData.currentVersionReleaseDate = rs.getString("currentVersionReleaseDate");
                appData.userRateCountForCur = rs.getInt("userRatingCountForCurrentVersion");
                appData.userTotalRateCount = rs.getInt("userRatingCount");
                appData.date = DateFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                if (rankAppIdPool.contains(appData.appId)) {
                    //appDataRecordListForRateNum.add(appData);
                    if (appMapForRateNum.containsKey(appData.appId)) {
                        appMapForRateNum.get(appData.appId).add(appData);
                    } else {
                        List<AppData> newList = new LinkedList<>();
                        newList.add(appData);
                        appMapForRateNum.put(appData.appId, newList);
                    }
                    addMetaDataToApp(appData.appId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMetaDataToApp(String appId) {
        AppData appData = new AppData(appId);
        appMetaDataMapForRateNum.put(appId, appData);

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
        for (AppData appData : appDataRecordListForRank) {
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


    public void buildAppMapForRatingFromDb() {
        String selectSql = "SELECT * FROM AppInfo";
        ResultSet rs;
        Statement statement;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start rate fetch...");
            rs = statement.executeQuery(selectSql);
            System.out.println("end rate fetch!");

            while (rs.next()) {
                AppData appData = new AppData();
                appData.appId = rs.getString("appId");
                appData.rankType = rs.getString("rankType");
                appData.currentVersion = rs.getString("currentVersion");
                appData.currentVersionReleaseDate = rs.getString("currentVersionReleaseDate");
                appData.averageUserRating = Double.parseDouble(rs.getString("averageUserRating"));
                appData.averageUserRatingForCurrentVersion = Double.parseDouble(rs.getString("averageUserRatingForCurrentVersion"));
                appData.date = DateFormat.timestampToMonthDayYear(rs.getTimestamp("date"));
                if (rankAppIdPool.contains(appData.appId)) {
                    //appDataRecordListForRating.add(appData);
                    if (appMapForRating.containsKey(appData.appId)) {
                        appMapForRating.get(appData.appId).add(appData);
                    } else {
                        List<AppData> newList = new LinkedList<>();
                        newList.add(appData);
                        appMapForRating.put(appData.appId, newList);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //build app map according to the app data list that fetch from the database
    public DataController buildAppDataMapForRank() {
        for (AppData appData : appDataRecordListForRank) {
            if (appMapForRank.containsKey(appData.appId)) {
                appMapForRank.get(appData.appId).add(appData);
            } else {
                List<AppData> newList = new LinkedList<>();
                newList.add(appData);
                appMapForRank.put(appData.appId, newList);
            }
        }
        System.out.println("数据库中的rank app总数: " + appMapForRank.size());

        Iterator iterator = appMapForRank.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String appId = entry.getKey().toString();
            List appDataList = (List) entry.getValue();
            if (appDataList.size() < RANK_MIN_NUM)
                iterator.remove();
            else
                rankAppIdPool.add(appId);
        }

        System.out.println("符合rank条件的App数: " + appMapForRank.size());
        return this;
    }

    //构建评论数量变化检测的的HashMap,key值为app id, value值为该app在数据库中的所有记录
    public DataController buildAppDataMapForRateNum() {
        for (AppData appData : appDataRecordListForRateNum) {
            if (appMapForRateNum.containsKey(appData.appId)) {
                appMapForRateNum.get(appData.appId).add(appData);
            } else {
                List<AppData> newList = new LinkedList<>();
                newList.add(appData);
                appMapForRateNum.put(appData.appId, newList);
            }
        }

        //若持续监测天数小于最小监测天数值,则从List中剔除该项
//        Iterator iterator = appMapForRateNum.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//            String appId = (String) entry.getKey();
//
//            List appDataList = (List) entry.getValue();
//            if (appDataList.size() < RATE_NUM_MIN_NUM) {
//                iterator.remove();
//                appMetaDataMapForRateNum.remove(appId);
//
//            }
//        }

        return this;
    }

    public DataController buildAppDataMapForRating() {
        for (AppData appData : appDataRecordListForRating) {
            if (appMapForRating.containsKey(appData.appId)) {
                appMapForRating.get(appData.appId).add(appData);
            } else {
                List<AppData> newList = new LinkedList<>();
                newList.add(appData);
                appMapForRating.put(appData.appId, newList);
            }
        }

//        Iterator iterator = appMapForRating.entrySet().iterator();
//        while (iterator.hasNext()) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//            List appDataList = (List) entry.getValue();
//            if (appDataList.size() < RATING_MIN_NUM)
//                iterator.remove();
//        }
        return this;
    }
}