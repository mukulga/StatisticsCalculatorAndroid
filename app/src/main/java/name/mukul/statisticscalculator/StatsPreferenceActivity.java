package name.mukul.statisticscalculator;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class StatsPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreferences);
    }

}
