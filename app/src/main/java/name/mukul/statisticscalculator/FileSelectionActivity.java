package name.mukul.statisticscalculator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import name.mukul.statisticscalculator.db.StatsPersistenceDatabase;
import name.mukul.statisticscalculator.parser.coeffofcorr.NumPair;
import name.mukul.statisticscalculator.parser.coeffofcorr.NumPairParser;
import name.mukul.statisticscalculator.parser.csv.CSVParser;
import name.mukul.statisticscalculator.parser.csv.ParseException;
import name.mukul.statisticscalculator.parser.csv.TokenMgrError;

/*
   The activity to show file selection dialog.
 */
public class FileSelectionActivity extends AppCompatActivity {

    Button buttonOpenDialog;
    Button buttonUp;
    TextView textFolder;

    static final int FILE_SELECTION_DIALOG_ID = 0;
    ListView dialogListView;
    Boolean isPersist;
    Boolean isSdLessOne;
    Boolean isCoeffOfCorrFromFileInp;
    int resultPrecision = -1;
    Boolean isDisplayRegEquation;
    StatsPersistenceDatabase statsPersistDb = null;

    File root;
    File curFolder;

    private List<String> fileList = new ArrayList<String>();
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        isPersist = (Boolean)intent.getSerializableExtra("name.mukul.statisticscalculator.IS_PERSIST");
        isSdLessOne = (Boolean)intent.getSerializableExtra("name.mukul.statisticscalculator.IS_SDOneLess");
        resultPrecision = intent.getIntExtra("name.mukul.statisticscalculator.RESULT_PRECISION", -1);
        isDisplayRegEquation = (Boolean)intent.getSerializableExtra("name.mukul.statisticscalculator.IS_DISPLAY_REG_EQUATION");
        isCoeffOfCorrFromFileInp = (Boolean)intent.getSerializableExtra("name.mukul.statisticscalculator.COC_FROM_FILE_INP");
        if (statsPersistDb == null) {
            statsPersistDb = new StatsPersistenceDatabase(this);
        }
        setContentView(R.layout.file_system_dialog);

        buttonOpenDialog = (Button) findViewById(R.id.opendialog);
        buttonOpenDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(FILE_SELECTION_DIALOG_ID);
            }
        });

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        curFolder = root;
        context = this;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch(id) {
            case FILE_SELECTION_DIALOG_ID:
                dialog = new Dialog(FileSelectionActivity.this);
                dialog.setContentView(R.layout.dialog_layout);
                dialog.setTitle("File Browser");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

                textFolder = (TextView)dialog.findViewById(R.id.folder);
                buttonUp = (Button) dialog.findViewById(R.id.up);
                buttonUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listDir(curFolder.getParentFile());
                    }
                });

                dialogListView = (ListView) dialog.findViewById(R.id.dialoglist);
                dialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        File selected = new File(fileList.get(position));
                        if (selected.isDirectory()) {
                            listDir(selected);
                        }
                        else {
                            try {
                                if (!isCoeffOfCorrFromFileInp.booleanValue()) {
                                    // parse the file and populate 'List<Float> numList'
                                    CSVParser csvParser = new CSVParser(new FileInputStream(selected.toString()));
                                    List<Float> numList = csvParser.Input();
                                    if (numList != null) {
                                        FindButtonEvaluator eval = new FindButtonEvaluator(context);
                                        eval.setIsPersist(isPersist);
                                        eval.setSDOneLess(isSdLessOne);
                                        eval.setStatsPersistDb(statsPersistDb);
                                        eval.setResultPrecision(resultPrecision);
                                        eval.calcAndDisplayResult(numList, 'F');  // F = a file input
                                        numList.clear();
                                    }
                                }
                                else {
                                    NumPairParser numPairParser = new NumPairParser(new FileInputStream(selected.toString()));
                                    List<NumPair> numPairList = numPairParser.Input();
                                    FindButtonEvaluator eval = new FindButtonEvaluator(context);
                                    eval.setIsPersist(isPersist);
                                    eval.setSDOneLess(isSdLessOne);
                                    eval.setStatsPersistDb(statsPersistDb);
                                    eval.setResultPrecision(resultPrecision);
                                    eval.setIsDisplayRegressionInfo(isDisplayRegEquation);
                                    eval.calcCoeffOfCorrFromFileAndDisplayResult(numPairList);
                                    numPairList.clear();
                                }
                            }
                            catch(FileNotFoundException ex){
                                showDialog("Error reading file", ex.getMessage(), HelloStatsConstants.CLOSE_DIALOG_MESG);
                            }
                            catch (ParseException ex) {
                                showDialog("Invalid data in file", ex.getMessage(), HelloStatsConstants.CLOSE_DIALOG_MESG);
                            }
                            catch (name.mukul.statisticscalculator.parser.coeffofcorr.ParseException ex) {
                                showDialog("Invalid data in file", ex.getMessage(), HelloStatsConstants.CLOSE_DIALOG_MESG);
                            }
                            catch (TokenMgrError er) {
                                showDialog("Invalid data in file", er.getMessage(), HelloStatsConstants.CLOSE_DIALOG_MESG);
                            }

                            dismissDialog(FILE_SELECTION_DIALOG_ID);
                        }
                    }
                });

                break;
        }

        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch(id) {
            case FILE_SELECTION_DIALOG_ID:
                listDir(curFolder);
                break;
        }
    }

    void listDir(File dir) {
        if (dir.equals(root)) {
            buttonUp.setEnabled(false);
        } else {
            buttonUp.setEnabled(true);
        }

        curFolder = dir;
        textFolder.setText(dir.getPath());

        File[] files = dir.listFiles();
        fileList.clear();

        for (File file : files) {
            fileList.add(file.getPath());
        }

        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, fileList);
        dialogListView.setAdapter(directoryList);
    }

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
}
