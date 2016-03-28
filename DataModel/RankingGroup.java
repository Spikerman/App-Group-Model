package DataModel;

import java.util.Date;
import java.util.Set;

/**
 * Created by chenhao on 3/27/16.
 */
public class RankingGroup {
    public int dateDiffNum = 0;
    public String groupType;
    private Set<String> appSet;
    private Date beginDate;
    private Date endDate;

    public RankingGroup(String groupType, int dateDiffNum, Set<String> appSet) {
        this.groupType = groupType;
        this.dateDiffNum = dateDiffNum;
        this.appSet = appSet;
    }

    public void setDateDiffNum(int dateDiffNum) {
        this.dateDiffNum = dateDiffNum;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public void setAppSet(Set<String> appSet) {
        this.appSet = appSet;
    }

    public void setDate(Date beginDate, Date endDate) {
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public int getSize() {
        return appSet.size();
    }
}
