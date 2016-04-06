package DataModel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenhao on 3/27/16.
 */
public class RankingGroup {
    public int dateDiffNum = 0;
    public String groupType = "";
    public Set<Date> commonChangeDateSet = new HashSet<>();
    private Set<String> appIdSet = new HashSet<>();
    private Date beginDate;
    private Date endDate;

    public RankingGroup(String groupType, int dateDiffNum, Set<String> appIdSet) {
        this.groupType = groupType;
        this.dateDiffNum = dateDiffNum;
        this.appIdSet = appIdSet;
    }

    public RankingGroup() {
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Set<String> getAppIdSet() {
        return appIdSet;
    }

    public void setAppIdSet(Set<String> appIdSet) {
        this.appIdSet = appIdSet;
    }

    public void setDateDiffNum(int dateDiffNum) {
        this.dateDiffNum = dateDiffNum;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public void setDate(Date beginDate, Date endDate) {
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public int getAppSize() {
        return appIdSet.size();
    }

    public int getCommonDateNum() {
        return commonChangeDateSet.size();
    }

    public void print(){
        System.out.println("----------------------------------");
        System.out.print("app id:");
        for (String s:
             appIdSet) {
            System.out.print("\t"+s);
        }
        System.out.print("\nDates:");
        for (Date date:
                commonChangeDateSet) {
            System.out.print("\t"+date);
        }
    }
}
