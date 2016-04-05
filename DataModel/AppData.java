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
    public String currentVersion;
    public int userRateCountForCur;
    public int userTotalRateCount;
    public boolean hasNumDecrease = false;
    public double averageDailyRateNum;

    public double averageUserRating;
    public double averageUserRatingForCurrentVersion;
    public double delta;

    public double minus(AppData appData){
        if(this.currentVersion==appData.currentVersion){
            this.delta = this.averageUserRatingForCurrentVersion - appData.averageUserRatingForCurrentVersion;
        }else {
            this.delta = this.averageUserRating - appData.averageUserRating;
        }
        return delta;
    }

}
