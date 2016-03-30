package DataModel;

import java.util.Date;

/**
 * Created by chenhao on 3/30/16.
 */
public class RateAmountDiffRecord {
    public String appId;
    public int amountDiff;
    public int rate;
    public Date date;

//    public boolean equals(Object object) {
//        if (this == object)
//            return true;
//        if (object instanceof RateAmountDiffRecord) {
//            RateAmountDiffRecord diffRecord = (RateAmountDiffRecord) object;
//            if (this.appId.equals(diffRecord.appId) && (this.amountDiff == diffRecord.amountDiff) && (this.date.equals(date)))
//                return true;
//        }
//        return false;
//    }
//
//    public int hashCode() {
//        int result = 17;
//        return result + appId.hashCode() + amountDiff + date.hashCode();
//    }
}
