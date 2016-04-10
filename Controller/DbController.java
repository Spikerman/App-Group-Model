package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Created by chenhao on 3/24/16.
 */
public class DbController {
    public static final String url = "jdbc:mysql://127.0.0.1/Data";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "root";
    public static final String rankQuerySql = "select appId,rankType,currentVersion,currentVersionReleaseDate,userRatingCountForCurrentVersion,userRatingCount,date from Data.AppInfo where rankType='update' and appId=? order by date";
    public static final String insertTestSql = "insert into Data.RateNumTest (date,appA,appB,appC,avgA,avgB,avgC) values (?,?,?,?,?,?,?)";
    public static final String insertAppGroupSql = "insert into Data.AppGroup (groupId,appId) values (?,?)";
    public Connection connection = null;
    public PreparedStatement rankNumQueryStmt = null;
    public PreparedStatement insertRateNumTestStmt = null;
    public PreparedStatement insertAppGroupStmt = null;

    public DbController() {
        try {
            Class.forName(name);
            connection = DriverManager.getConnection(url, user, password);
            //connection test
            System.out.println("Connect Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        DbController dbController = new DbController();
    }

    public void setRankNumQueryStmt(String sql) {
        try {
            rankNumQueryStmt = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInsertRateNumTestStmt(String sql) {
        try {
            insertRateNumTestStmt = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInsertAppGroupStmt(String sql) {
        try {
            insertAppGroupStmt = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
