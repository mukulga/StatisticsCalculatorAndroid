package name.mukul.statisticscalculator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import name.mukul.statisticscalculator.db.StatsPersistenceDatabase;

/*
   The main activity of this application.
 */
public class MainStatsCalcActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private MainStatsCalcActivity thisClass;
    private static StatsPersistenceDatabase statsPersistDb = null;
    private static SharedPreferences prefs = null;

    private List<Float> numList;                                            // this list will store a list of numbers
    private static boolean isPersist;                                       // whether the calculations will be persisted in the DB
    private static boolean isSdLessOne;                                     // whether divide by n-1 or n, when calculating standard deviation
    private static int resValuePrecision = -1;
    private static boolean isDisplayRegressionInfo;
    private static boolean isFileInputPref;
    private static String emailAddress;
    private static final int SHOW_PREFERENCES = 0;
    private static final int SHOW_HTML_VIEW = 1;
    public static final int SHOW_FILE_SELECTION = 2;
    private int minIdxVal = 0;

    // the variables below control, how back button behaves
    private static boolean isMainView = true;
    private static boolean isInpNumberView = false;
    private static boolean isInpNumberKeyboardView = false;
    private static boolean isInpNumberListView = false;
    private static boolean isHistListView = false;

    private static String editBoxSizePref = HelloStatsConstants.EDITBOX_SIZE_SMALL;
    private static int desiredPixelWidth1;
    private static int desiredPixelWidth2;
    private static float scaledDensity;
    private static TextView modeOfInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        numList = new ArrayList<Float>();

        setContentView(R.layout.main_activity_layout);

        Button calcBtn1 = (Button) findViewById(R.id.calcBtn1);
        calcBtn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isInpNumberView = true;
                isMainView = false;
                setContentView(R.layout.input_number_select_layout);
                modeOfInput = (TextView) findViewById(R.id.inpMethodsHelp);
                setTouchEventHandlerForModeInputHelp(modeOfInput);
                setFileSelectionRadioBtnState();
            }
        });
        calcBtn1.setEnabled(true);
        Button calcBtn2 = (Button) findViewById(R.id.calcBtn2);
        calcBtn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isInpNumberListView = true;
                isMainView = false;
                setContentView(R.layout.input_numbers_list_layout);
                EditText numList1 = (EditText) findViewById(R.id.numList1);
                EditText numList2 = (EditText) findViewById(R.id.numList2);
                calculateEditBoxDisplayPixelWidth(HelloStatsConstants.CORRELATION_EDIT_BOX);
                numList1.setWidth(desiredPixelWidth2);
                numList2.setWidth(desiredPixelWidth2);
                setCoeffOfCorrScreenBtnState();
            }
        });
        calcBtn2.setEnabled(true);

        // register shared preference change listener
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        desiredPixelWidth1 = (int)(55 * scaledDensity);
        desiredPixelWidth2 = (int)(150 * scaledDensity);
        thisClass = this;
    }

    private void setFileSelectionRadioBtnState() {
        RadioButton radioOneAtTime = (RadioButton)findViewById(R.id.radio_one_at_time);
        RadioButton radioFromFile = (RadioButton)findViewById(R.id.radio_from_file);
        if (isFileInputPref) {
            radioFromFile.setChecked(true);
            radioOneAtTime.setChecked(false);
        }
        else {
            radioFromFile.setChecked(false);
            radioOneAtTime.setChecked(true);
        }
    }

    private void setCoeffOfCorrScreenBtnState() {
        Button findBtn = (Button)findViewById(R.id.findBtn);
        Button resetCorrBtn = (Button)findViewById(R.id.resetCorrBtn);
        Button fileInpCorrBtn = (Button)findViewById(R.id.fileInpCorrBtn);
        EditText numList1 = (EditText)findViewById(R.id.numList1);
        EditText numList2 = (EditText)findViewById(R.id.numList2);
        if (isFileInputPref) {
            findBtn.setEnabled(false);
            resetCorrBtn.setEnabled(false);
            numList1.setEnabled(false);
            numList2.setEnabled(false);
            fileInpCorrBtn.setEnabled(true);
        }
        else {
            findBtn.setEnabled(true);
            resetCorrBtn.setEnabled(true);
            numList1.setEnabled(true);
            numList2.setEnabled(true);
            fileInpCorrBtn.setEnabled(false);
        }
    }

    private void setTouchEventHandlerForModeInputHelp(TextView modeOfInput) {
        modeOfInput.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((TextView)v).setTextColor(Color.CYAN);
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((TextView)v).setTextColor(HelloStatsConstants.CUSTOM_ORANGE);
                    Intent htmlViewIntent = new Intent(thisClass, ShowHtmlViewActivity.class);
                    htmlViewIntent.putExtra(HelloStatsConstants.HTMLVIEW_KEY_NAME, HelloStatsConstants.HTMLVIEW_HELP);
                    htmlViewIntent.putExtra("isLearn", 'I');
                    startActivityForResult(htmlViewIntent, SHOW_HTML_VIEW);
                    return true;
                }
                return false;
            }
        });
    }

    /*
        The app allows user to resize input text boxes dynamically. This method handles this user setting.
     */
    private void calculateEditBoxDisplayPixelWidth(char pWhichEditBox) {
        switch (editBoxSizePref) {
            case HelloStatsConstants.EDITBOX_SIZE_SMALL:
                if (pWhichEditBox == HelloStatsConstants.CORRELATION_EDIT_BOX) {
                    desiredPixelWidth2 = (int) (150 * scaledDensity);
                }
                else {
                    desiredPixelWidth1 = (int) (55 * scaledDensity);
                }
                break;
            case HelloStatsConstants.EDITBOX_SIZE_MEDIUM:
                if (pWhichEditBox == HelloStatsConstants.CORRELATION_EDIT_BOX) {
                    desiredPixelWidth2 = (int) (225 * scaledDensity);
                }
                else {
                    desiredPixelWidth1 = (int) (82.5 * scaledDensity);
                }
                break;
            case HelloStatsConstants.EDITBOX_SIZE_LARGE:
                if (pWhichEditBox == HelloStatsConstants.CORRELATION_EDIT_BOX) {
                    desiredPixelWidth2 = (int) (300 * scaledDensity);
                }
                else {
                    desiredPixelWidth1 = (int) (110 * scaledDensity);
                }
                break;
            default:
                // NO OP
        }
    }

    public void openRadioButtonInputScreen(View view) {
        boolean isFirstRadioButtonChecked = ((RadioButton)findViewById(R.id.radio_one_at_time)).isChecked();
        if (isFirstRadioButtonChecked) {
            isInpNumberKeyboardView = true;
            isInpNumberView = false;
            setContentView(R.layout.input_number_layout);
            EditText inpNum = (EditText) findViewById(R.id.inpNum);
            calculateEditBoxDisplayPixelWidth(HelloStatsConstants.AVERAGE_CALCULATION_EDIT_BOX);
            inpNum.setWidth(desiredPixelWidth1);
        }
        else {
            Intent intent = new Intent(this, FileSelectionActivity.class);
            intent.putExtra("name.mukul.statisticscalculator.IS_PERSIST", Boolean.valueOf(isPersist));
            intent.putExtra("name.mukul.statisticscalculator.IS_SDOneLess", Boolean.valueOf(isSdLessOne));
            intent.putExtra("name.mukul.statisticscalculator.RESULT_PRECISION", resValuePrecision);
            intent.putExtra("name.mukul.statisticscalculator.COC_FROM_FILE_INP", false);
            startActivityForResult(intent, SHOW_FILE_SELECTION);
        }
    }

    /*
       There must be better way to handle this.
     */
    public void onBackPressed() {
        if (isInpNumberView) {
            isInpNumberView = false;
            isMainView = true;

            setContentView(R.layout.main_activity_layout);

            Button calcBtn1 = (Button) findViewById(R.id.calcBtn1);
            calcBtn1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    isInpNumberView = true;
                    isMainView = false;
                    setContentView(R.layout.input_number_select_layout);
                    modeOfInput = (TextView) findViewById(R.id.inpMethodsHelp);
                    setTouchEventHandlerForModeInputHelp(modeOfInput);
                    setFileSelectionRadioBtnState();
                }
            });
            calcBtn1.setEnabled(true);
            Button calcBtn2 = (Button) findViewById(R.id.calcBtn2);
            calcBtn2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    isInpNumberListView = true;
                    isMainView = false;
                    setContentView(R.layout.input_numbers_list_layout);
                    EditText numList1 = (EditText) findViewById(R.id.numList1);
                    EditText numList2 = (EditText) findViewById(R.id.numList2);
                    calculateEditBoxDisplayPixelWidth(HelloStatsConstants.CORRELATION_EDIT_BOX);
                    numList1.setWidth(desiredPixelWidth2);
                    numList2.setWidth(desiredPixelWidth2);
                    setCoeffOfCorrScreenBtnState();
                }
            });
            calcBtn2.setEnabled(true);
        }
        else if (isInpNumberKeyboardView) {
            isInpNumberKeyboardView = false;
            isInpNumberView = true;
            setContentView(R.layout.input_number_select_layout);
            modeOfInput = (TextView) findViewById(R.id.inpMethodsHelp);
            setTouchEventHandlerForModeInputHelp(modeOfInput);
            setFileSelectionRadioBtnState();
        }
        else if (isInpNumberListView) {
            isInpNumberListView = false;
            isMainView = true;

            setContentView(R.layout.main_activity_layout);

            Button calcBtn1 = (Button) findViewById(R.id.calcBtn1);
            calcBtn1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    isInpNumberView = true;
                    isMainView = false;
                    setContentView(R.layout.input_number_select_layout);
                    modeOfInput = (TextView) findViewById(R.id.inpMethodsHelp);
                    setTouchEventHandlerForModeInputHelp(modeOfInput);
                    setFileSelectionRadioBtnState();
                }
            });
            calcBtn1.setEnabled(true);
            Button calcBtn2 = (Button) findViewById(R.id.calcBtn2);
            calcBtn2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    isInpNumberListView = true;
                    isMainView = false;
                    setContentView(R.layout.input_numbers_list_layout);
                    EditText numList1 = (EditText) findViewById(R.id.numList1);
                    EditText numList2 = (EditText) findViewById(R.id.numList2);
                    calculateEditBoxDisplayPixelWidth(HelloStatsConstants.CORRELATION_EDIT_BOX);
                    numList1.setWidth(desiredPixelWidth2);
                    numList2.setWidth(desiredPixelWidth2);
                    setCoeffOfCorrScreenBtnState();
                }
            });
            calcBtn2.setEnabled(true);
        }
        else if (isHistListView) {
            isHistListView = false;
            isMainView = true;

            setContentView(R.layout.main_activity_layout);

            Button startBtn = (Button) findViewById(R.id.calcBtn1);
            startBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    isInpNumberView = true;
                    isMainView = false;
                    setContentView(R.layout.input_number_select_layout);
                    modeOfInput = (TextView) findViewById(R.id.inpMethodsHelp);
                    setTouchEventHandlerForModeInputHelp(modeOfInput);
                    setFileSelectionRadioBtnState();
                }
            });
        }
        else if (isMainView) {
            // give user a chance to decide, whether to exit the App or Stay
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Exiting App");
            builder.setMessage("Exit the Hello Stats App?");
            builder.setPositiveButton(HelloStatsConstants.YES_BTN_MESG, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // close the dialog
                    dialog.cancel();
                    finish();
                }
            });
            builder.setNegativeButton(HelloStatsConstants.NO_BTN_MESG, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // close the dialog
                    dialog.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void nextBtnAction(View view) {
        // extract float value from text box
        EditText inpNum = (EditText) findViewById(R.id.inpNum);
        CharSequence inpStr = inpNum.getText();
        try {
            Float inpFltValue = Float.valueOf(inpStr.toString());
            numList.add(inpFltValue);
        } catch (NumberFormatException ex) {
            showDialog("Incorrect input format",
                       "The value '" + inpStr + "' is not a valid numeric value",
                       HelloStatsConstants.CLOSE_DIALOG_MESG);
        }

        setContentView(R.layout.input_number_layout);
        inpNum = (EditText) findViewById(R.id.inpNum);
        calculateEditBoxDisplayPixelWidth(HelloStatsConstants.AVERAGE_CALCULATION_EDIT_BOX);
        inpNum.setWidth(desiredPixelWidth1);
    }

    public void findBtnAction(View view) {
        if (numList.size() > 0) {
            FindButtonEvaluator eval = new FindButtonEvaluator(this);
            eval.setIsPersist(isPersist);
            eval.setSDOneLess(isSdLessOne);
            eval.setStatsPersistDb(statsPersistDb);
            eval.setResultPrecision(resValuePrecision);
            eval.calcAndDisplayResult(numList, 'N'); // N = not a file input
            numList.clear();
        }
    }

    public void showAllBtnAction(View view) {
        StringBuffer valuesEntered = new StringBuffer();
        for (Iterator iter = numList.iterator(); iter.hasNext(); ) {
            Float val = (Float) iter.next();
            String strVal = val.toString();
            valuesEntered.append(strVal);
            if (iter.hasNext()) {
                valuesEntered.append(", ");
            }
        }
        String valuesEnteredStr = valuesEntered.toString();
        try {
            showDialog("The values entered so far are:", valuesEnteredStr.subSequence(0, valuesEnteredStr.length()), HelloStatsConstants.CLOSE_DIALOG_MESG);
        }
        catch (Exception ex) {
            // NO OP
        }
    }

    public void resetBtnAction(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Session data reset");
        builder.setMessage("You are about to delete from this session, the data entered so far. Select Yes or No.");
        builder.setPositiveButton(HelloStatsConstants.YES_BTN_MESG, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // reset data and close the dialog
                numList.clear();
                dialog.cancel();
                showDialog("The input is reset!", "You have to enter all values again", HelloStatsConstants.CLOSE_DIALOG_MESG);
            }
        });
        builder.setNegativeButton(HelloStatsConstants.NO_BTN_MESG, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // close the dialog
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void clrPreviousBtnAction(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deleting previous results");
        builder.setMessage("You are about to delete the previous results. Select Yes or No.");
        builder.setPositiveButton(HelloStatsConstants.YES_BTN_MESG, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // delete data from DB and close the dialog
                if (statsPersistDb == null) {
                    statsPersistDb = new StatsPersistenceDatabase(thisClass);
                }
                statsPersistDb.deleteAllRecords();
                dialog.cancel();
                showDialog("Deletion Successful", "The previous stored results are now deleted", HelloStatsConstants.CLOSE_DIALOG_MESG);
                menuPreviousResultsAction();
            }
        });
        builder.setNegativeButton(HelloStatsConstants.NO_BTN_MESG, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // close the dialog
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void findCorrelationBtnAction(View view) {
        EditText numList1 = (EditText) findViewById(R.id.numList1);
        String inpStr1 = String.valueOf(numList1.getText());

        EditText numList2 = (EditText) findViewById(R.id.numList2);
        String inpStr2 = String.valueOf(numList2.getText());

        String[] list1 = inpStr1.split(",");
        String[] list2 = inpStr2.split(",");
        if (list1.length != list2.length) {
            showDialog("Error", "The two lists are not of the same size.", HelloStatsConstants.CLOSE_DIALOG_MESG);
        }
        else {
            double coeffOfCorr = StatsAlgorithms.coefficientOfCorrelation(list1, list2, this);
            if (coeffOfCorr != HelloStatsConstants.INVALID_CORRELATION) {
                String cocStrValue = String.valueOf(coeffOfCorr);
                DecimalFormat dfConf = FindButtonEvaluator.getDecimalFormatConf(resValuePrecision);
                if (resValuePrecision != -1) {
                    cocStrValue = dfConf.format(coeffOfCorr);
                }
                if (HelloStatsConstants.NOT_A_NUMBER.equals(cocStrValue)) {
                    cocStrValue = HelloStatsConstants.ERR_RESULT;
                }
                // display least-squares regression equation, if the option is enabled
                if (isDisplayRegressionInfo) {
                    List<Float> fltList1 = StatsAlgorithms.convertList(list1, HelloStatsConstants.DATA_LIST_LABEL1, this);
                    List<Float> fltList2 = StatsAlgorithms.convertList(list2, HelloStatsConstants.DATA_LIST_LABEL2, this);
                    float arMean1 = StatsAlgorithms.findArMean(fltList1);
                    float arMean2 = StatsAlgorithms.findArMean(fltList2);
                    double sd1 = StatsAlgorithms.findStandardDeviation(fltList1, isSdLessOne);
                    double sd2 = StatsAlgorithms.findStandardDeviation(fltList2, isSdLessOne);
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
                        statsPersistDb = new StatsPersistenceDatabase(this);
                    }
                    // restore format before storing in DB
                    statsPersistDb.addNewRecord("", "", "", "", "", "", inpStr1, inpStr2, cocStrValue, "", "");
                    //statsPersistDb.closeDatabase();
                }
            }
        }
    }

    public void resetCorrBtnAction(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset input");
        builder.setMessage("Click on Yes to clear the input edit boxes. Click on No, to cancel this step.");
        builder.setPositiveButton(HelloStatsConstants.YES_BTN_MESG, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // reset code
                TextView numList1 = (TextView) findViewById(R.id.numList1);
                numList1.setText("");
                TextView numList2 = (TextView) findViewById(R.id.numList2);
                numList2.setText("");
                // close the dialog
                dialog.cancel();
            }
        });
        builder.setNegativeButton(HelloStatsConstants.NO_BTN_MESG, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // close the dialog
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void fileInpCorrBtnAction(View view) {
        Intent intent = new Intent(this, FileSelectionActivity.class);
        intent.putExtra("name.mukul.statisticscalculator.IS_PERSIST", Boolean.valueOf(isPersist));
        intent.putExtra("name.mukul.statisticscalculator.IS_SDOneLess", Boolean.valueOf(isSdLessOne));
        intent.putExtra("name.mukul.statisticscalculator.RESULT_PRECISION", resValuePrecision);
        intent.putExtra("name.mukul.statisticscalculator.IS_DISPLAY_REG_EQUATION", isDisplayRegressionInfo);
        intent.putExtra("name.mukul.statisticscalculator.COC_FROM_FILE_INP", true);
        startActivityForResult(intent, SHOW_FILE_SELECTION);
    }

    public void sendEmailBtnAction(View view) {
        if (!isNetworkConnected()) {
            showDialog("Not connected to data network", "Your device is not connected to a data network. Please enable a data network connection, if you want the App to send an email containing calculations stored in the app.", HelloStatsConstants.CLOSE_DIALOG_MESG);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sending email ...");
        builder.setMessage("Clicking the Send button will send an email, of the list of calculations saved in the App to the email address you have provided in App settings.");
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (emailAddress == null || "".equals(emailAddress.trim())) {
                    showDialog("Email error", "Email address in Application Settings is not configured.", HelloStatsConstants.CLOSE_DIALOG_MESG);
                }
                else {
                    String fileName = composeTextDocumentForEmail();
                    // send an email
                    File file = new File(getFilesDir(), fileName);
                    new SendEmailTask().execute(file);
                }
            }
        });
        builder.setNegativeButton(HelloStatsConstants.CANCEL_BTN_MESG, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // close the dialog
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String composeTextDocumentForEmail() {
        if (statsPersistDb == null) {
            statsPersistDb = new StatsPersistenceDatabase(this);
        }
        Cursor dataCursor = statsPersistDb.getDbRecords(null);
        int rowCount = 0;
        StringBuffer textBufForEmail = new StringBuffer();
        while (dataCursor.moveToNext()) {
            rowCount++;
            if (rowCount == 1) {
                textBufForEmail.append("Statistical calculations stored within the Hello Stats App\n\n");
            }

            String dataSetText = dataCursor.getString(1);
            String arMean = dataCursor.getString(2);
            String median = dataCursor.getString(3);
            String mode = dataCursor.getString(4);
            String avgDeviation = dataCursor.getString(5);
            String stdDeviation = dataCursor.getString(6);
            String corrDataSet1 = dataCursor.getString(7);
            String corrDataSet2 = dataCursor.getString(8);
            String coc = dataCursor.getString(9);
            String gMean = dataCursor.getString(10);
            String hMean = dataCursor.getString(11);
            String cos = dataCursor.getString(12);
            String mesgTxt = null;
            if ("".equals(arMean)) {
                mesgTxt = "Dataset 1: " + corrDataSet1 + "; Dataset 2: " + corrDataSet2 + "; Coefficient of correlation: " + coc;
            }
            else {
                mesgTxt = "Dataset: " + dataSetText + "; Arithmetic Mean: " + arMean + "; Geometric Mean: " + gMean + "; Harmonic Mean: " + hMean + "; Median: " + median + "; Mode: " + mode +
                        "; Average Deviation: " + avgDeviation + "; Standard Deviation: " + stdDeviation + "; Coefficient of skewness: " + cos;
            }
            textBufForEmail.append(rowCount + ") " + mesgTxt + "\n");
        }

        String fileName = "stats_calculations_" + (new Date()).toString().replace(" ", "") + ".txt";
        String string = textBufForEmail.toString();
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileName;
    }

    /*
        Check whether the device is connected to internet.
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_stats_calc, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent htmlViewIntent = null;

        switch (item.getItemId()) {
            case R.id.action_about:
                showDialog("Hello Stats Calculator",
                           HelloStatsConstants.ABOUT_STR,
                           HelloStatsConstants.CLOSE_DIALOG_MESG);
                break;
            case R.id.action_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType(HelloStatsConstants.SHARE_INTENT_MIMETYPE);
                String shareBody = HelloStatsConstants.SHARE_APP_STRING2 + ": https://play.google.com/store/apps/details?id=name.mukul.statisticscalculator";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, HelloStatsConstants.SHARE_APP_SUBJECT);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, HelloStatsConstants.SHARE_APP_STRING1));
                break;
            case R.id.action_preference:
                startActivityForResult(new Intent(this, StatsPreferenceActivity.class), SHOW_PREFERENCES);
                break;
            case R.id.action_history:
                isHistListView = true;
                isMainView = false;
                menuPreviousResultsAction();
                break;
            case R.id.action_help:
                htmlViewIntent = new Intent(this, ShowHtmlViewActivity.class);
                htmlViewIntent.putExtra(HelloStatsConstants.HTMLVIEW_KEY_NAME, HelloStatsConstants.HTMLVIEW_HELP);   // for showing Help
                startActivityForResult(htmlViewIntent, SHOW_HTML_VIEW);
                break;
            case R.id.action_tAndC:
                htmlViewIntent = new Intent(thisClass, ShowHtmlViewActivity.class);
                htmlViewIntent.putExtra(HelloStatsConstants.HTMLVIEW_KEY_NAME, HelloStatsConstants.HTMLVIEW_TANDC);   // for showing T&C page
                startActivityForResult(htmlViewIntent, SHOW_HTML_VIEW);
                break;
            case R.id.feedback:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Feedback");
                builder.setMessage(HelloStatsConstants.FEEDBACK_STR);
                builder.setPositiveButton(HelloStatsConstants.YES_BTN_MESG, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        // close the dialog
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton(HelloStatsConstants.NO_BTN_MESG, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // close the dialog
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            default:
                // NO OP
        }

        return super.onOptionsItemSelected(item);
    }

    /*
       Implementation of functionality for one of the menu options.
     */
    private void menuPreviousResultsAction() {
        setContentView(R.layout.history_ui_layout);
        ListView histListView = (ListView)findViewById(R.id.historyListView);

        ArrayList<String> listVals = new ArrayList<String>();
        // add data to the listVals ArrayList by a DB read
        if (statsPersistDb == null) {
            statsPersistDb = new StatsPersistenceDatabase(this);
        }
        Cursor dataCursor = statsPersistDb.getDbRecords(null);
        int rowCount = 0;
        while (dataCursor.moveToNext()) {
            // find the primary key's minimum value in table
            int idx = dataCursor.getInt(0);
            if (rowCount == 0 || idx < minIdxVal) {
                minIdxVal = idx;
            }
            rowCount++;
            String meanVal = dataCursor.getString(2);
            String dispText = String.valueOf(rowCount + "#" + ("".equals(meanVal) ? dataCursor.getString(9) : meanVal));
            listVals.add(dispText);
        }

        Button clrPrvBtn = (Button)findViewById(R.id.clrPrvBtn);
        ImageButton sendEmailBtn = (ImageButton)findViewById(R.id.sendEmailBtn);
        if (listVals.size() == 0) {
            clrPrvBtn.setEnabled(false);
            sendEmailBtn.setEnabled(false);
        }

        histListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // fetch the record from DB for the id that was clicked, and display the details in an Alert Dialog
                if (statsPersistDb == null) {
                    statsPersistDb = new StatsPersistenceDatabase(thisClass);
                }
                Cursor dataCursor = statsPersistDb.getDbRecords(Integer.valueOf((int)id + minIdxVal));
                if (dataCursor.moveToNext()) {
                    String dataSetText = dataCursor.getString(1);
                    String arMean = dataCursor.getString(2);
                    String median = dataCursor.getString(3);
                    String mode = dataCursor.getString(4);
                    String avgDeviation = dataCursor.getString(5);
                    String stdDeviation = dataCursor.getString(6);
                    String corrDataSet1 = dataCursor.getString(7);
                    String corrDataSet2 = dataCursor.getString(8);
                    String coc = dataCursor.getString(9);
                    String gMean = dataCursor.getString(10);
                    String hMean = dataCursor.getString(11);
                    //String cos = dataCursor.getString(12);
                    String mesgTxt = null;
                    if ("".equals(arMean)) {
                        mesgTxt = "Dataset 1: " + corrDataSet1 + "; Dataset 2: " + corrDataSet2 + "; Coefficient of correlation: " + coc;
                    }
                    else {
                        mesgTxt = "Dataset: " + dataSetText + "; Arithmetic Mean: " + arMean + "; Geometric Mean: " + gMean + "; Harmonic Mean: " + hMean + "; Median: " + median + "; Mode: " + mode +
                                  "; Average Deviation: " + avgDeviation + "; Standard Deviation: " + stdDeviation;
                    }

                    showDialog("Selected Calculation", mesgTxt, HelloStatsConstants.CLOSE_DIALOG_MESG);
                }
            }
        });

        // setup an ArrayAdapter to bind to the list view
        int layoutID = android.R.layout.simple_list_item_1;   // definition in the android runtime
        ArrayAdapter<String> arrAdapterInstance = new ArrayAdapter<String>(this, layoutID, listVals);
        histListView.setAdapter(arrAdapterInstance);
        arrAdapterInstance.notifyDataSetChanged();
        //statsPersistDb.closeDatabase();
    }

    /*
        A reusable method to show alert dialogs.
     */
    void showDialog(CharSequence title, CharSequence mesg, CharSequence posBtnMesg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("PERSIST_PREF".equals(key)) {
            isPersist = sharedPreferences.getBoolean(key, false);
        }
        else if ("EMAIL_PREF".equals(key)) {
            emailAddress = sharedPreferences.getString(key, "");
            if (!isValidEmail(emailAddress)) {
                showDialog("Email format incorrect", HelloStatsConstants.EMAIL_FORMAT_ERR_MESG, HelloStatsConstants.CLOSE_DIALOG_MESG);
            }
        }
        else if ("SD_DENOMINATOR_PREF".equals(key)) {
            isSdLessOne = sharedPreferences.getBoolean(key, false);
        }
        else if ("RESULT_ROUNDING_PREF".equals(key)) {
            resValuePrecision = (Integer.valueOf(sharedPreferences.getString(key, "-1"))).intValue();
        }
        else if ("REGRESSION_PREF".equals(key)) {
            isDisplayRegressionInfo = sharedPreferences.getBoolean(key, false);
        }
        else if ("EDITBOX_SIZE_PREF".equals(key)) {
            editBoxSizePref = sharedPreferences.getString(key, HelloStatsConstants.EDITBOX_SIZE_SMALL);
        }
        else if ("FILEINPUT_PREF".equals(key)) {
            isFileInputPref = sharedPreferences.getBoolean(key, false);
        }
    }

    private boolean isValidEmail(CharSequence emailAddress) {
        if (TextUtils.isEmpty(emailAddress)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
        }
    }

    /*
        A background task to send an email.
     */
    private class SendEmailTask extends AsyncTask<File, Void, Boolean> {

        protected Boolean doInBackground(File... file) {
            boolean result = EmailUtil.sendEmail(HelloStatsConstants.FROM_EMAIL_ADDRESS, emailAddress, file[0].getPath());
            return Boolean.valueOf(result);
        }

        protected void onPostExecute(Boolean result) {
            if (result.booleanValue()) {
                showDialog("Email successfully sent", "Please check your email, for the details you have requested from the App.", HelloStatsConstants.CLOSE_DIALOG_MESG);
            }
            else {
                showDialog("Email delivery failure", "There was an error sending the email from App.", HelloStatsConstants.CLOSE_DIALOG_MESG);
            }
        }
    }
}
