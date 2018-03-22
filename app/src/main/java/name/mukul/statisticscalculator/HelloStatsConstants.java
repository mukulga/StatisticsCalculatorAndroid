package name.mukul.statisticscalculator;

import android.graphics.Color;

/**
 * A class defining constants for the application.
 */
public class HelloStatsConstants {
    public static final String YES_BTN_MESG = "Yes";
    public static final String NO_BTN_MESG = "No";
    public static final String CANCEL_BTN_MESG = "Cancel";
    public static final String CLOSE_DIALOG_MESG = "Close";
    public static final String FROM_EMAIL_ADDRESS = "hellostats@mathematicaltools.co.in";
    public static final String NOT_A_NUMBER = "NaN";
    public static final String ERR_RESULT = "[Err]";
    public static final String ABOUT_STR = "This app allows you to do various statistical calculations on numerical data sets. " +
                                            "The app Help from menu, provides useful information about the usage.";
    public static final String FEEDBACK_STR = "Please provide your feedback about this app, to help make it better. Clicking on 'Yes' will take you to Play Store, where you can write your feedback & rate this app.";
    public static final String EMAIL_FORMAT_ERR_MESG = "The email address you have provided, doesn't conform to conventions of an email " +
                                                       "address. Please correct it.";
    public static final String ASSETS_FOLDER_LOCATION = "file:///android_asset/";
    public static final String HELP_START_PAGE = "help1.html";
    public static final String TANDC_PAGE = "tAndC.html";
    public static final String HTMLVIEW_KEY_NAME = "htmlType";
    public static final char HTMLVIEW_HELP = 'H';  // help
    public static final char HTMLVIEW_TANDC = 'T'; // terms and conditions
    public static final int MAX_FILE_READ_BUFFER_SIZE = 1024;
    public static final String DATA_LIST_LABEL1 = "List 1";
    public static final String DATA_LIST_LABEL2 = "List 2";
    public static final int REG_COEFFICIENT_PRECISION = 2;  // a fixed application decided precision, for regression equation. this is currently 2 decimal places.
    public static final double INVALID_CORRELATION = -1005.0;  // any arbitrary value, that cannot be a coefficient of correlation
    public static final String SHARE_APP_SUBJECT = "Hello Stats app";
    public static final String SHARE_APP_STRING1 = "Share using";
    public static final String SHARE_APP_STRING2 = "Hi There, Take a look at Hello Stats calculator App on Play store";

    public static final String SHARE_INTENT_MIMETYPE = "text/plain";
    public static final String EDITBOX_SIZE_SMALL = "S";
    public static final String EDITBOX_SIZE_MEDIUM = "M";
    public static final String EDITBOX_SIZE_LARGE = "L";
    public static final char AVERAGE_CALCULATION_EDIT_BOX = 'A';
    public static final char CORRELATION_EDIT_BOX = 'C';
    public static final int CUSTOM_ORANGE = Color.argb(150, 235, 118, 0);   // #96eb7600
}
