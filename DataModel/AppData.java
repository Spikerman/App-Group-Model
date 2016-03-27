package DataModel;

import java.util.Date;

/**
 * Created by chenhao on 3/27/16.
 */
public class AppData {

    public String appId;
    public String rankType;
    public int ranking;
    public int rankFloatNum;
    public Date date;
    public String currentVersionReleaseDate;
    public String dateString;


    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (this == null)
            return false;
        if (!(other instanceof AppData))
            return false;

        final AppData appData = (AppData) other;

        if (!this.appId.equals(appData.appId))
            return false;

        return true;
    }


}
