package name.mukul.statisticscalculator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;

/*
    We use this activity to show different kinds of HTML pages. For now these are of type :
    Help page, and T&C Information. Which HTML information to show with this activity, is determined
    by what data is set on the Intent.
 */
public class ShowHtmlViewActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webview = new WebView(this);
        setContentView(webview);
        // get the intent that started this activity, and use information set in it to show the appropriate HTML page
        Intent intent = getIntent();
        char htmlInfoType = intent.getCharExtra(HelloStatsConstants.HTMLVIEW_KEY_NAME, HelloStatsConstants.HTMLVIEW_HELP);
        String url = null;
        if (htmlInfoType == HelloStatsConstants.HTMLVIEW_HELP) {
            char htmlPageEntry = intent.getCharExtra("isLearn", HelloStatsConstants.HTMLVIEW_HELP);
            if (htmlPageEntry == 'L') {
                url = "statistics_fundamentals.html";
            }
            else if (htmlPageEntry == 'I') {
                url = "help2.html#modeOfInput";
            }
            else {
                url = HelloStatsConstants.HELP_START_PAGE;
            }
        }
        else if (htmlInfoType == HelloStatsConstants.HTMLVIEW_TANDC) {
            url = HelloStatsConstants.TANDC_PAGE;
        }
        webview.loadUrl(HelloStatsConstants.ASSETS_FOLDER_LOCATION + url);
    }

}
