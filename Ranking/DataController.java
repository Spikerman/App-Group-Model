package Ranking;

import Controller.DbController;
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

    private DbController dbController = new DbController();
    private LinkedList<AppData> dataList = new LinkedList<>();
    private Map<Date, Set<String>> map = new TreeMap<>();

    public static void main(String args[]) {
        DataController dataController = new DataController();
        dataController.getDataFromDb().buildMap();
        LinkedList list = dataController.getDataList();
        System.out.println(list.size());

    }

    public LinkedList<AppData> getDataList() {
        return dataList;
    }

    public DataController getDataFromDb() {
        //String selectSql = "SELECT * FROM Data.AppInfo WHERE( appId='1062817956')";
        String selectSql = "SELECT * FROM Data.AppInfo";
        Statement statement = null;
        ResultSet rs = null;
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
                appData.date = DateFormat.timestampToMonthDayYear(rs.getTimestamp("date"));//?
                //appData.dateString = rs.getTimestamp("date").toString();

                System.out.println(appData.appId + " " + appData.rankType + " " + appData.ranking + " " + appData.rankFloatNum + " " + appData.date);
                dataList.add((appData));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }

        return this;
    }

    public DataController buildMap() {
        for (AppData appData : dataList) {
            if (map.containsKey(appData.date))
                map.get(appData.date).add(appData.appId);
            else {
                Set<String> newSet = new HashSet<>();
                newSet.add(appData.appId);
                map.put(appData.date, newSet);
            }
        }
        return this;
    }

}
