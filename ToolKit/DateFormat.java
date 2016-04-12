package ToolKit;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by chenhao on 3/27/16.
 */
public class DateFormat {
    private static final SimpleDateFormat monthDayYearFormatter = new SimpleDateFormat("MMMMM dd, yyyy");

    public static Date timestampToMonthDayYear(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            return new Date(timestamp.getYear(), timestamp.getMonth(), timestamp.getDate());
        }
    }

    //取指定日期想前或向后几天的Date对象,正数往后推,负数往前移动
    public static Date adjustDay(Date date, int num) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE, num);
        return calendar.getTime();
    }

    public static void main(String args[]) {
        Date date = new Date();
        System.out.println(date);
        System.out.println(adjustDay(date,1));
        System.out.println(adjustDay(date,2));
        System.out.println(adjustDay(date,0));
        System.out.println(adjustDay(date,-2));
    }
}
