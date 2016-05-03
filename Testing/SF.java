package Testing;

import Controller.DataController;

/**
 * Created by chenhao on 5/3/16.
 */
public class SF {
    public static void main(String args[]) {
        DataController dataController = new DataController();
        dataController.getRankAppInfoFromDb();
        dataController.constructRankAppMap();
        int i=1;
        while(i<60){
            dataController.countValidAppAmount(i);
            i++;
        }

    }

}
