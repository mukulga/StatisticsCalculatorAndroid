package name.mukul.statisticscalculator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import name.mukul.statisticscalculator.db.StatsPersistenceDatabase;
import name.mukul.statisticscalculator.parser.coeffofcorr.NumPair;

public class FindButtonEvaluator {

    Context context;
    Boolean isPersist;
    Boolean isSdOneLess;
    int resultPrecision;
    Boolean isDisplayRegressionInfo;
    StatsPersistenceDatabase statsPersistDb = null;

    public int getResultPrecision() {
        return resultPrecision;
    }

    public Boolean getIsPersist() {
        return isPersist;
    }

    public Boolean getIsSdOneLess() {
        return isSdOneLess;
    }

    public Boolean getIsDisplayRegressionInfo() {
        return isDisplayRegressionInfo;
    }

    public void setIsPersist(Boolean persist) {
        this.isPersist = persist;
    }

    public void setSDOneLess(Boolean sdOneLess) {this.isSdOneLess = sdOneLess;}

    public void setResultPrecision(int resultPrecision) {
        this.resultPrecision = resultPrecision;
    }

    public void setIsDisplayRegressionInfo(Boolean isDisplayRegressionInfo) {this.isDisplayRegressionInfo = isDisplayRegressionInfo;}

    public StatsPersistenceDatabase getStatsPersistDb() {
        return statsPersistDb;
    }

    public void setStatsPersistDb(StatsPersistenceDatabase statsPersistDb) {
        this.statsPersistDb = statsPersistDb;
    }

    public FindButtonEvaluator(Context pContext) {
        context = pContext;
    }

    public void calcAndDisplayResult(List<Float> pNumList, char pInputMode) {

        // calculate arithmetic mean
        float arMeanVal = StatsAlgorithms.findArMean(pNumList);
        String arMeanStr = (Float.valueOf(arMeanVal)).toString();
        CharSequence arMeanChrSeq = arMeanStr.subSequence(0, arMeanStr.length());

        // calculate geometric mean
        float gMeanVal = StatsAlgorithms.findGeometricMean(pNumList);
        String gMeanStr = (Float.valueOf(gMeanVal)).toString();
        CharSequence gMeanChrSeq = gMeanStr.subSequence(0, gMeanStr.length());

        // calculate harmonic mean
        float hMeanVal = StatsAlgorithms.findHarmonicMean(pNumList);
        String hMeanStr = (Float.valueOf(hMeanVal)).toString();
        CharSequence hMeanChrSeq = hMeanStr.subSequence(0, hMeanStr.length());

        // calculate median
        CharSequence medianChrSeq = null;
        float medianVal = 0.0f;
        if (!pNumList.isEmpty()) {
            medianVal = StatsAlgorithms.findMedian(pNumList);
            String medianStr = (Float.valueOf(medianVal)).toString();
            medianChrSeq = medianStr.subSequence(0, medianStr.length());
        } else {
            medianChrSeq = HelloStatsConstants.NOT_A_NUMBER;
        }

        // calculate mode
        List<Double> modeSample = new ArrayList<Double>();
        for (int idx = 0; idx < pNumList.size(); idx++) {
            modeSample.add(Double.valueOf((pNumList.get(idx)).toString()));
        }
        double[] modeVal = StatsAlgorithms.findMode(modeSample);
        StringBuffer modeResultBuf = new StringBuffer();
        for (int idx = 0; idx < modeVal.length; idx++) {
            modeResultBuf.append(modeVal[idx]);
            if (idx != (modeVal.length - 1)) {
                modeResultBuf.append(" ");
            }
        }
        String modeStr = modeResultBuf.toString();
        CharSequence modeChrSeq = modeStr.subSequence(0, modeStr.length());

        // calculate average deviation
        float avDeviationVal = StatsAlgorithms.findAverageDeviation(pNumList);
        String avDevStr = (Float.valueOf(avDeviationVal)).toString();
        CharSequence avDevChrSeq = avDevStr.subSequence(0, avDevStr.length());

        // calculate standard deviation
        double sdDeviationVal = StatsAlgorithms.findStandardDeviation(pNumList, isSdOneLess);
        String sdDevStr = (Double.valueOf(sdDeviationVal)).toString();
        CharSequence sdDevChrSeq = sdDevStr.subSequence(0, sdDevStr.length());

        // calculate coefficient of skewness
        /*double cosVal = StatsAlgorithms.coeffOfSkewness(pNumList, isSdOneLess);
        String cosStr = (Double.valueOf(cosVal)).toString();
        CharSequence cosChrSeq = cosStr.subSequence(0, cosStr.length());
        if (HelloStatsConstants.NOT_A_NUMBER.equals(cosChrSeq)) {
            cosChrSeq = HelloStatsConstants.ERR_RESULT;
        }*/

        StringBuffer valuesEnteredStrBuf = new StringBuffer();
        for (Iterator<Float> iter = pNumList.iterator(); iter.hasNext(); ) {
            Float val = iter.next();
            valuesEnteredStrBuf.append(val);
            if (iter.hasNext()) {
                valuesEnteredStrBuf.append(", ");
            }
        }

        // if input mode is file, abbreviate the string valuesEnteredStrBuf for display and storage purposes.
        // because file input is expected to read much more numbers than input via keyboard. this will look displayable.
        String valuesEntered = null;
        if (pInputMode == 'F') {
            valuesEntered = valuesEnteredStrBuf.toString();
            int length = valuesEntered.length();
            if (length >= 40) {
                valuesEntered = valuesEntered.substring(0, 40) + "...";
            }
        }
        else {
            valuesEntered = valuesEnteredStrBuf.toString();
        }

        DecimalFormat dfConf = getDecimalFormatConf(resultPrecision);
        //cosStr = (String)cosChrSeq;
        StringBuffer modeStrOut = new StringBuffer();
        if (resultPrecision == -1) {
            showDialog("Statistical values for the dataset",
                    "Input data : " + valuesEntered + "\n\nArithmetic Mean: " + arMeanChrSeq + ", Geometric Mean: " + gMeanChrSeq + ", Harmonic Mean: " + hMeanChrSeq + ", Median: " +
                            medianChrSeq + ", Mode: [" + modeChrSeq + "], Average Deviation: " + avDevChrSeq + ", Standard Deviation: " +
                            sdDevChrSeq, HelloStatsConstants.CLOSE_DIALOG_MESG);
        } else {
            for (int idx = 0; idx < modeVal.length; idx++) {
                String valStr = dfConf.format(modeVal[idx]);
                modeStrOut.append(valStr);
                if (idx != (modeVal.length - 1)) {
                    modeStrOut.append(" ");
                }
            }
            showDialog("Statistical values for the dataset",
                    "Input data : " + valuesEntered + "\n\nArithmetic Mean: " + dfConf.format((double) arMeanVal) + ", Geometric Mean: " + dfConf.format((double) gMeanVal) + ", Harmonic Mean: " + dfConf.format((double) hMeanVal) + ", Median: " +
                            dfConf.format((double) medianVal) + ", Mode: [" + modeStrOut.toString() + "], Average Deviation: " + dfConf.format((double) avDeviationVal) + ", Standard Deviation: " +
                            dfConf.format(sdDeviationVal), HelloStatsConstants.CLOSE_DIALOG_MESG);
        }

        // persist the data, if the feature is enabled
        if (isPersist.booleanValue()) {
            if (statsPersistDb == null) {
                statsPersistDb = new StatsPersistenceDatabase(context);
            }
            if (resultPrecision == -1) {
                statsPersistDb.addNewRecord(valuesEntered, arMeanChrSeq.toString(), medianChrSeq.toString(), "["+modeChrSeq.toString()+"]",
                        avDevChrSeq.toString(), sdDevChrSeq.toString(), "", "", "", gMeanChrSeq.toString(), hMeanChrSeq.toString());
            }
            else {
                statsPersistDb.addNewRecord(valuesEntered, dfConf.format((double) arMeanVal), dfConf.format((double) medianVal), "["+modeStrOut.toString()+"]",
                        dfConf.format((double) avDeviationVal), dfConf.format(sdDeviationVal), "", "", "", dfConf.format((double) gMeanVal),
                        dfConf.format((double) hMeanVal));
            }
            //statsPersistDb.closeDatabase();
        }
    }

    public static DecimalFormat getDecimalFormatConf(int precisionLength) {
        StringBuffer precisionStr = new StringBuffer("#.");
        for (int idx = 0; idx < precisionLength; idx++) {
            precisionStr.append("#");
        }
        return new DecimalFormat(precisionStr.toString());
    }

    void showDialog(CharSequence title, CharSequence mesg, CharSequence posBtnMesg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(mesg);
        builder.setPositiveButton(posBtnMesg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // close the dialog
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void calcCoeffOfCorrFromFileAndDisplayResult(List<NumPair> numPairList) {
        double coeffOfCorr = StatsAlgorithms.coefficientOfCorrelation(numPairList);
        if (coeffOfCorr != HelloStatsConstants.INVALID_CORRELATION) {
            String cocStrValue = String.valueOf(coeffOfCorr);
            DecimalFormat dfConf = FindButtonEvaluator.getDecimalFormatConf(resultPrecision);
            if (resultPrecision != -1) {
                cocStrValue = dfConf.format(coeffOfCorr);
            }
            if (HelloStatsConstants.NOT_A_NUMBER.equals(cocStrValue)) {
                cocStrValue = HelloStatsConstants.ERR_RESULT;
            }

            List<Float> fltList1 = new ArrayList<Float>();
            List<Float> fltList2 = new ArrayList<Float>();
            StringBuffer inpStrBuff1 = new StringBuffer();
            StringBuffer inpStrBuff2 = new StringBuffer();
            for (int idx = 0; idx < numPairList.size(); idx++) {
                NumPair numPair = numPairList.get(idx);
                fltList1.add(numPair.num1);
                fltList2.add(numPair.num2);
            }
            // convert the two Float lists into, two csv String objects
            String inpStr1 = "";
            String inpStr2 = "";
            for (int idx = 0; idx < fltList1.size(); idx++) {
                if (idx != (fltList1.size() - 1)) {
                    inpStrBuff1.append((fltList1.get(idx)).toString()+",");
                }
                else {
                    inpStrBuff1.append((fltList1.get(idx)).toString());
                    inpStr1 = inpStrBuff1.toString();
                }
            }
            for (int idx = 0; idx < fltList2.size(); idx++) {
                if (idx != (fltList2.size() - 1)) {
                    inpStrBuff2.append((fltList2.get(idx)).toString()+",");
                }
                else {
                    inpStrBuff2.append((fltList2.get(idx)).toString());
                    inpStr2 = inpStrBuff2.toString();
                }
            }

            // reformat the strings if the strings are large
            int length = inpStr1.length();
            if (length >= 40) {
                inpStr1 = inpStr1.substring(0, 40) + "...";
            }
            length = inpStr2.length();
            if (length >= 40) {
                inpStr2 = inpStr2.substring(0, 40) + "...";
            }

            if (isDisplayRegressionInfo) {
                // display least-squares regression equation, if the option is enabled
                float arMean1 = StatsAlgorithms.findArMean(fltList1);
                float arMean2 = StatsAlgorithms.findArMean(fltList2);
                double sd1 = StatsAlgorithms.findStandardDeviation(fltList1, isSdOneLess);
                double sd2 = StatsAlgorithms.findStandardDeviation(fltList2, isSdOneLess);
                // find equation coefficients and display the result
                double b = coeffOfCorr * (sd2 / sd1);
                double a = (double) arMean2 - b * (double) arMean1;
                dfConf = FindButtonEvaluator.getDecimalFormatConf(HelloStatsConstants.REG_COEFFICIENT_PRECISION);
                String bStr = dfConf.format(b);
                if (HelloStatsConstants.NOT_A_NUMBER.equals(bStr)) {
                    bStr = HelloStatsConstants.ERR_RESULT;
                }
                String aStr = dfConf.format(a);
                if (HelloStatsConstants.NOT_A_NUMBER.equals(aStr)) {
                    aStr = HelloStatsConstants.ERR_RESULT;
                }
                String regressionRetails = "Least-squares regression equation:\n";
                regressionRetails = regressionRetails + "y = a + bx\n";
                regressionRetails = regressionRetails + "a = " + aStr + "\n";
                regressionRetails = regressionRetails + "b = " + bStr;
                showDialog("Correlation and regression", "Input list 1 : " + inpStr1.replace(",", ", ") + "\nInput list 2 : " + inpStr2.replace(",", ", ") + "\n\nThe coefficient of correlation is " + cocStrValue + ".\n\n" + regressionRetails, HelloStatsConstants.CLOSE_DIALOG_MESG);
            } else {
                showDialog("Correlation", "Input list 1 : " + inpStr1.replace(",", ", ") + "\nInput list 2 : " + inpStr2.replace(",", ", ") + "\n\nThe coefficient of correlation is " + cocStrValue + ".", HelloStatsConstants.CLOSE_DIALOG_MESG);
            }
            // persist the data, if the feature is enabled
            if (isPersist) {
                if (statsPersistDb == null) {
                    statsPersistDb = new StatsPersistenceDatabase(context);
                }
                // restore format before storing in DB
                statsPersistDb.addNewRecord("", "", "", "", "", "", inpStr1, inpStr2, cocStrValue, "", "");
                //statsPersistDb.closeDatabase();
            }
        }
    }
}
