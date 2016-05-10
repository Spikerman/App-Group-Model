package Itegration;

import Controller.DataController;
import DataModel.AppData;
import DataModel.RateAmountDiffRecord;
import Ranking.RankingAnalysis;
import RateAmount.RateAmountAnalysis;
import Rating.RatingAnalysis;

import java.util.*;

/**
 * Created by chenhao on 5/10/16.
 */
public class IndicatorIntegration {
    public Set<Set<String>> groupSet = new HashSet<>();
    private RankingAnalysis rankingAnalysis;
    private RateAmountAnalysis rateAmountAnalysis;
    private RatingAnalysis ratingAnalysis;
    private Map<String, List<AppData>> rankRecordMap;
    private Map<String, HashMap<Date, Double>> rateRecordMap;
    private Map<String, HashMap<Date, RateAmountDiffRecord>> rateVolumeRecordMap;
    private DataController dataController;

    public IndicatorIntegration() {
        dataController = new DataController();
        //ranking 必须第一个构造,创建rankAppPool
        rankingAnalysis = new RankingAnalysis(dataController);
        rateAmountAnalysis = new RateAmountAnalysis(dataController);
        ratingAnalysis = new RatingAnalysis(dataController);
    }

    public static void main(String args[]) {
        IndicatorIntegration indicatorIntegration = new IndicatorIntegration();
        indicatorIntegration.getRecordMaps();

        System.out.print("hello");
        //导出数据到远程数据库
        //integrationAnalyse.exportGroupData();
    }

    public int getGroupSetSize() {
        return groupSet.size();
    }

    private void  getRecordMaps() {
        rankRecordMap = rankingAnalysis.dataController.getAppMapForRank();
        rateRecordMap = ratingAnalysis.buildDiffRecordMap();
        rateVolumeRecordMap = rateAmountAnalysis.buildDiffRecordMap();

    }
}
