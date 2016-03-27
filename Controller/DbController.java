package Controller;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by chenhao on 3/24/16.
 */
public class DbController {
    public static final String url = "jdbc:mysql://115.159.100.165/Data";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "GroupTie";
    public static final String password = "grouptie123456";
    public Connection connection = null;

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

    public static void main(String args[]){

        DbController dbController = new DbController();
    }
}
