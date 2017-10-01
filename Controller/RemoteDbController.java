package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Created by chenhao on 4/16/16.
 */
public class RemoteDbController {
    public static final String url = "";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "";
    public static final String password = "";
    public static final String insertAppGroupSql = "insert into Data.AppGroup (groupId,appId) values (?,?)";
    public static final String insertCandidateClusterSql = "insert into Data.CandidateCluster (clusterId,appId) values (?,?)";
    public Connection connection = null;
    public PreparedStatement insertAppGroupStmt = null;
    public PreparedStatement insertCCStmt = null;


    public RemoteDbController() {
        try {
            Class.forName(name);
            connection = DriverManager.getConnection(url, user, password);
            //connection test
            System.out.println("Remote Database Connect Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        DbController dbController = new DbController();
    }

    public void setInsertAppGroupStmt(String sql) {
        try {
            insertAppGroupStmt = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInsertCCStmt(String sql) {
        try {
            insertCCStmt = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
