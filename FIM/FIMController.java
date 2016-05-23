package FIM;

import Controller.DbController;
import com.google.common.collect.Sets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by chenhao on 5/20/16.
 */
public class FimController {
    public Map<Integer, Set<String>> appGroupMap = new HashMap<>();
    public Map<Integer, Set<String>> testAppGroupMap = new HashMap<>();
    public Map<String, Set<String>> userAppMap = new HashMap<>();
    public Set<String> userSet = new HashSet<>();
    public PreparedStatement selectRevewSqlStmt;

    public PreparedStatement selectRevewForAppStmt;
    DbController dbController;
    Map<Integer, Set<String>> userGroupMap = new TreeMap<>();
    String selectUserSql = "SELECT * FROM Data.user_group;";
    String selectClusterSql = "SELECT groupId,appId FROM Data.AppGroup;";
    String selectReviewSql = "SELECT * FROM Data.Review where userId=?; ";

    String selectReviewForApp = "SELECT count(*) FROM Data.Review where appId=?";

    public FimController(DbController dbController) {
        this.dbController = dbController;
        try {
            selectRevewSqlStmt = dbController.connection.prepareStatement(selectReviewSql);

            selectRevewForAppStmt = dbController.connection.prepareStatement(selectReviewForApp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String arge[]) {
        DbController dbController = new DbController();
        FimController fimController = new FimController(dbController);
        fimController.loadUserGroup();
        fimController.loadCluster();
        fimController.countClusterReviewAmount();
        fimController.buildTestAppGroupMap();


        //fimController.groupCompareTest(5);
    }


    //构造user map, key值为对应的组数, value为得到的collusive attackers的集合
    public void loadUserGroup() {
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start user data fetch...");
            rs = statement.executeQuery(selectUserSql);
            System.out.println("end user data fetch...");

            int clusterId;
            String userId;
            while (rs.next()) {
                clusterId = Integer.parseInt(rs.getString("cluster_id"));
                userId = rs.getString("user_id");
                userSet.add(userId);
                if (userGroupMap.containsKey(clusterId)) {
                    userGroupMap.get(clusterId).add(userId);
                } else {
                    Set newUserGroup = new HashSet<>();
                    newUserGroup.add(userId);
                    userGroupMap.put(clusterId, newUserGroup);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        buildUserAppMap();

    }

    public void buildUserAppMap() {
        Statement statement;
        ResultSet rs;
        String sql = sqlGenerateForReview();
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start user data fetch...");
            rs = statement.executeQuery(sql);
            System.out.println("end user data fetch...");

            String userId;
            String appId;
            while (rs.next()) {
                userId = rs.getString("userId");
                appId = rs.getString("appId");
                insertToUserAppMap(userId, appId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //构造app group map, key为对应的组, value为组内的app数
    public void loadCluster() {
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            System.out.println("start cluster data fetch...");
            rs = statement.executeQuery(selectClusterSql);
            System.out.println("end cluster data fetch...");
            String appId;
            int groupId;
            while (rs.next()) {
                groupId = rs.getInt("groupId");
                appId = rs.getString("appId");
                insertToAppMap(groupId, appId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void insertToAppMap(Integer groupId, String appId) {
        if (appGroupMap.containsKey(groupId)) {
            appGroupMap.get(groupId).add(appId);
        } else {
            Set<String> newIdSet = new HashSet<>();
            newIdSet.add(appId);
            appGroupMap.put(groupId, newIdSet);
        }
    }

    private void insertToUserAppMap(String userId, String appId) {
        if (userAppMap.containsKey(userId)) {
            userAppMap.get(userId).add(appId);
        } else {
            Set<String> newIdSet = new HashSet<>();
            newIdSet.add(appId);
            userAppMap.put(userId, newIdSet);
        }

    }


    private void insertToTestAppMap(Integer clusterId, Set<String> appIdSet) {
        if (testAppGroupMap.containsKey(clusterId)) {
            testAppGroupMap.get(clusterId).addAll(appIdSet);
        } else {
            Set<String> newIdSet = new HashSet<>();
            newIdSet.addAll(appIdSet);
            testAppGroupMap.put(clusterId, newIdSet);
        }

    }


    public void buildTestAppGroupMap() {
        for (Map.Entry entry : userGroupMap.entrySet()) {
            int clusterId = (Integer) entry.getKey();
            Set<String> userGroup = (Set) entry.getValue();
            for (String userId : userGroup) {
                Set<String> reviewApps = userAppMap.get(userId);
                insertToTestAppMap(clusterId, reviewApps);
            }
        }
    }

    public Set<String> getReviewApps(String userId) {
        Set<String> appSet = new HashSet<>();
        ResultSet rs;
        try {
            selectRevewSqlStmt.setString(1, userId);
            rs = selectRevewSqlStmt.executeQuery();
            while (rs.next()) {
                String appId = rs.getString("appId");
                appSet.add(appId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appSet;
    }

    public void groupCompareTest() {
        Set<String> appGroup = appGroupMap.get(1);
        Set<String> revewAppGroup = testAppGroupMap.get(1);
        Set<String> commonAppSet = Sets.intersection(appGroup, revewAppGroup);
        double x = commonAppSet.size();
        double y = appGroup.size();
        double z = x / y;
        System.out.println("result: " + z);

    }

    public double groupCompareTest(int key) {
        Set<String> appGroup = appGroupMap.get(key);
        Set<String> revewAppGroup = testAppGroupMap.get(key);
        Set<String> commonAppSet = Sets.intersection(appGroup, revewAppGroup);
        double x = commonAppSet.size();
        double y = appGroup.size();
        double z = x / y;
        System.out.println("result: " + z);
        return z;
    }

    public String sqlGenerateForReview() {
        StringBuffer sql = new StringBuffer("SELECT * FROM Data.Review where userId in ");
        StringBuffer range = new StringBuffer("(");
        Object[] array = userSet.toArray();
        for (int i = 0; i < array.length; i++) {
            if (i != (array.length - 1)) {
                String id = array[i].toString();
                range.append(id + ",");
            } else {
                String id = array[i].toString();
                range.append(id + ")");
            }
        }
        sql.append(range);
        return sql.toString();
    }

    public String sqlGenerateForApp(Set<String> cluster) {
        StringBuffer sql = new StringBuffer("SELECT count(*) as amount FROM Data.Review where appId in ");

        StringBuffer range = new StringBuffer("(");

        Object[] array = cluster.toArray();

        for (int i = 0; i < array.length; i++) {
            if (i != (array.length - 1)) {
                String id = array[i].toString();
                range.append(id + ",");
            } else {
                String id = array[i].toString();
                range.append(id + ")");
            }
        }
        sql.append(range);
        return sql.toString();
    }

    public void countClusterReviewAmount() {
        Statement statement;
        ResultSet rs;
        for (Map.Entry entry : appGroupMap.entrySet()) {
            Set<String> cluster = (Set) entry.getValue();
            String sql = sqlGenerateForApp(cluster);
            try {
                statement = dbController.connection.createStatement();
                rs = statement.executeQuery(sql);
                while (rs.next()) {
                    int amount = rs.getInt("amount");
                    System.out.println(amount);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
