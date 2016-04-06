package ToolKit;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chenhao on 3/27/16.
 */
public class DateFormat {
    private static final SimpleDateFormat monthDayYearFormatter = new SimpleDateFormat("MMMMM dd, yyyy");

    public static Date timestampToMonthDayYear(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        } else {
            return new Date(timestamp.getYear(),timestamp.getMonth(),timestamp.getDate());
        }
    }


}
