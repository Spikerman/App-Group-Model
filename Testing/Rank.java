package Testing;

import Controller.DataController;
import Ranking.RankingAnalysis;
import ToolKit.Print;

/**
 * Created by chenhao on 5/3/16.
 */
public class Rank {
    public static void main(String args[]) {
        DataController dataController = new DataController();
        RankingAnalysis rankingAnalysis = new RankingAnalysis(dataController);

        rankingAnalysis.rankGroupMapGenerate();
        System.out.println("" + rankingAnalysis.rankGroupMap.size());
        Print.printEachGroupSize(rankingAnalysis.rankGroupMap);
    }
}
