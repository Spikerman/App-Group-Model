package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Created by chenhao on 4/16/16.
 */
public class RemoteDbController {
    public static final String url = "jdbc:mysql://115.159.100.165/Data";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "GroupTie";
    public static final String password = "grouptie123456";
    public Connection connection = null;
    public static final String insertAppGroupSql = "insert into Data.AppGroup (groupId,appId) values (?,?)";
    public PreparedStatement insertAppGroupStmt = null;

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



}
