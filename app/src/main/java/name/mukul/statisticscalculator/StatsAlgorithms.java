package name.mukul.statisticscalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.stat.StatUtils;

import name.mukul.statisticscalculator.parser.coeffofcorr.NumPair;

/**
 *
 * Implementations of common statistics algorithms.
 */
public class StatsAlgorithms {

    public static float findArMean(List<Float> numList) {
        float sumVal = 0.0f;
        for (Iterator iter = numList.iterator(); iter.hasNext(); ) {
            Float val = (Float) iter.next();
            sumVal = sumVal + val.floatValue();
        }

        return  sumVal / numList.size();
    }

    public static float findGeometricMean(List<Float> numList) {
        float sumLogVal = 0.0f;
        for (Iterator iter = numList.iterator(); iter.hasNext(); ) {
            Float val = (Float) iter.next();
            sumLogVal = sumLogVal + (float)Math.log10(val.floatValue());
        }
        float numTemp = sumLogVal / numList.size();

        return (float)Math.pow(10, numTemp);
    }

    public static float findHarmonicMean(List<Float> numList) {
        float sumTemp = 0.0f;
        for (Iterator iter = numList.iterator(); iter.hasNext(); ) {
            Float val = (Float) iter.next();
            sumTemp = sumTemp + (1 / val.floatValue());
        }

        return 1 / (sumTemp / numList.size());
    }

    public static float findMedian(List<Float> numList) {
        float medianVal = 0.0f;

        Collections.sort(numList);
        if ((numList.size() % 2) == 0) {
            // total number of items is even
            float item1 = (numList.get((numList.size() / 2) - 1)).floatValue();
            float item2 = (numList.get(numList.size() / 2)).floatValue();
            medianVal = (item1 + item2) / 2;
        }
        else {
            // total number of items is odd
            Float resultItem = numList.get(((numList.size() + 1) / 2) - 1);
            medianVal = resultItem.floatValue();
        }

        return medianVal;
    }

    /*public static float findMode(List<Float> numList) {
        float modeVal = 0.0f;

        modeVal = findArMean(numList) - 3 * (findArMean(numList) - findMedian(numList));

        return modeVal;
    }*/

    public static double[] findMode(List<Double> numList) {
        double[] sample = new double[numList.size()];
        for (int idx = 0; idx < numList.size(); idx++) {
            sample[idx] = (numList.get(idx)).doubleValue();
        }

        return StatUtils.mode(sample);
    }

    public static float findAverageDeviation(List<Float> numList) {

        float mean = findArMean(numList);
        float tempSum = 0.0f;
        for (Iterator iter = numList.iterator(); iter.hasNext(); ) {
            Float val = (Float) iter.next();
            tempSum = tempSum + Math.abs(val - mean);
        }

        return tempSum / numList.size();
    }

    public static double findStandardDeviation(List<Float> numList, boolean isSdOneLess) {

        float mean = findArMean(numList);
        float tempSum = 0.0f;
        for (Iterator iter = numList.iterator(); iter.hasNext(); ) {
            Float val = (Float) iter.next();
            tempSum = tempSum + (val - mean) * (val - mean);
        }
        int size = numList.size();
        if (size > 1 && isSdOneLess) {
            size--;
        }

        return Math.sqrt(tempSum / size);
    }

    /*public static double coeffOfSkewness(List<Float> numList, boolean isSdOneLess) {
        return (findArMean(numList) - findMode(numList)) / findStandardDeviation(numList, isSdOneLess);
    }*/

    public static double coefficientOfCorrelation(String[] list1, String[] list2, MainStatsCalcActivity mainActivity) {
        double coc = 0.0f;

        List<Float> fltList1 = convertList(list1, HelloStatsConstants.DATA_LIST_LABEL1, mainActivity);
        if (fltList1.size() == 1 && (fltList1.get(0)).isNaN()) {
            return HelloStatsConstants.INVALID_CORRELATION;
        }
        List<Float> fltList2 = convertList(list2, HelloStatsConstants.DATA_LIST_LABEL2, mainActivity);
        if (fltList2.size() == 1 && (fltList2.get(0)).isNaN()) {
            return HelloStatsConstants.INVALID_CORRELATION;
        }
        float mean1 = StatsAlgorithms.findArMean(fltList1);
        float mean2 = StatsAlgorithms.findArMean(fltList2);

        float sumXsq = 0.0f;
        float sumYsq = 0.0f;
        float sumXY = 0.0f;
        for (int idx = 0; idx < fltList1.size(); idx++) {
            sumXsq = sumXsq + ((fltList1.get(idx)).floatValue() - mean1) * ((fltList1.get(idx)).floatValue() - mean1);
            sumYsq = sumYsq + ((fltList2.get(idx)).floatValue() - mean2) * ((fltList2.get(idx)).floatValue() - mean2);
            sumXY = sumXY + ((fltList1.get(idx)).floatValue() - mean1) * ((fltList2.get(idx)).floatValue() - mean2);
        }

        coc = sumXY / Math.sqrt(sumXsq * sumYsq);

        return coc;
    }

    public static double coefficientOfCorrelation(List<NumPair> numPairList) {
        double coc = 0.0f;
        List<Float> fltList1 = new ArrayList<Float>();
        List<Float> fltList2 = new ArrayList<Float>();
        for (int idx = 0; idx < numPairList.size(); idx++) {
            NumPair numPair = numPairList.get(idx);
            fltList1.add(numPair.num1);
            fltList2.add(numPair.num2);
        }

        float mean1 = StatsAlgorithms.findArMean(fltList1);
        float mean2 = StatsAlgorithms.findArMean(fltList2);

        float sumXsq = 0.0f;
        float sumYsq = 0.0f;
        float sumXY = 0.0f;
        for (int idx = 0; idx < fltList1.size(); idx++) {
            sumXsq = sumXsq + ((fltList1.get(idx)).floatValue() - mean1) * ((fltList1.get(idx)).floatValue() - mean1);
            sumYsq = sumYsq + ((fltList2.get(idx)).floatValue() - mean2) * ((fltList2.get(idx)).floatValue() - mean2);
            sumXY = sumXY + ((fltList1.get(idx)).floatValue() - mean1) * ((fltList2.get(idx)).floatValue() - mean2);
        }

        coc = sumXY / Math.sqrt(sumXsq * sumYsq);

        return coc;
    }

    public static List<Float> convertList(String[] inpList, String listId, MainStatsCalcActivity mainActivity) {
        List<Float> newList = new ArrayList<Float>();
        boolean errConverting = false;
        for (int idx = 0; idx < inpList.length; idx++) {
            String val = inpList[idx];
            try {
                newList.add(Float.valueOf(val));
            }
            catch (NumberFormatException ex) {
                errConverting = true;
                mainActivity.showDialog("Incorrect input format",
                                        "The value '" + val + "' in " + listId + " is not a valid numeric value",
                                        "Close");
                break;
            }
        }
        if (errConverting) {
            newList.clear();
            newList.add(Float.NaN);
        }

        return newList;
    }

}
