package Rating;

import Controller.DataController;
import DataModel.AppData;
import ToolKit.DateComparator;
import java.util.*;

/**
 * Created by 景舜 on 2016/3/30.
 */
public class RatingAnalysis {
    private Map<String, List<AppData>> appDataMap;

    public RatingAnalysis(){
        DataController dataController = new DataController();
        dataController = dataController.buildAppDataListForRatingFromDb().buildAppDataMapForRating();
        appDataMap = dataController.getAppMapForRating();
    }

    private void sortAppDataMap(){
        Iterator iter = appDataMap.entrySet().iterator();
        DateComparator dateComparator = new DateComparator();
        while(iter.hasNext()){
            Map.Entry item = (Map.Entry) iter.next();
            List<AppData> applist = (List)item.getValue();
            Collections.sort(applist, dateComparator);
            System.out.println("caculate app"+item.getKey());
            for(int i = 1;i<applist.size();i++){
                AppData app1 =  applist.get(i);
                AppData app0 = applist.get(i-1);
                app1.minus(app0);
                applist.remove(i);
                applist.add(i,app1);
            }
            System.out.println("过滤");
            Iterator iter1 = applist.iterator();
            while(iter1.hasNext()){
                AppData app = (AppData)iter1.next();
                if(app.delta==0){
                    iter1.remove();
                }
            }
            appDataMap.put((String)item.getKey(),applist);
        }
    }

    public  void startAnalyzing(){
        sortAppDataMap();
    }
    public static  void main(String[] args){
        RatingAnalysis ratingAnalysis = new RatingAnalysis();
        ratingAnalysis.startAnalyzing();
    }

}
