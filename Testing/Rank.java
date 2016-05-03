package Testing;

import Controller.DataController;
import Ranking.RankingAnalysis;

import java.util.TreeMap;

/**
 * Created by chenhao on 5/3/16.
 */
public class Rank {
    public static void main(String args[]) {
        DataController dataController = new DataController();
        RankingAnalysis rankingAnalysis = new RankingAnalysis(dataController);

        int i = 1;
        while (i < 20) {
            rankingAnalysis.rankGroupMapGenerateTest(i);
            System.out.println(i + "    " + rankingAnalysis.rankGroupMap.size());
            rankingAnalysis.rankGroupMap = new TreeMap<>();
            i++;
        }
    }
}
