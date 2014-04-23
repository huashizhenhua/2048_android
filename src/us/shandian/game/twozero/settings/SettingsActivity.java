package us.shandian.game.twozero.settings;

import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.content.Context;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.Bundle;

import us.shandian.game.twozero.R;
import us.shandian.game.twozero.InputListener;
import us.shandian.game.twozero.MainActivity;
import us.shandian.game.twozero.MainView;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener
{
    private ListPreference mSensitivity;
    private ListPreference mVariety;
    private CheckBoxPreference mInverse;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        addPreferencesFromResource(R.xml.settings);
        
        mSensitivity = (ListPreference) findPreference(SettingsProvider.KEY_SENSITIVITY);
        mVariety = (ListPreference) findPreference(SettingsProvider.KEY_VARIETY);
        mInverse = (CheckBoxPreference) findPreference(SettingsProvider.KEY_INVERSE_MODE);
        
        mSensitivity.setOnPreferenceChangeListener(this);
        mVariety.setOnPreferenceChangeListener(this);
        mInverse.setOnPreferenceChangeListener(this);
        
        // Initialize values
        int sensitivity = SettingsProvider.getInt(SettingsProvider.KEY_SENSITIVITY, 1);
        mSensitivity.setValueIndex(sensitivity);
        String[] sensitivitySummaries = getResources().getStringArray(R.array.settings_sensitivity_entries);
        mSensitivity.setSummary(sensitivitySummaries[sensitivity]);
        
        int variety = SettingsProvider.getInt(SettingsProvider.KEY_VARIETY, 0);
        mVariety.setValueIndex(variety);
        String[] varietySummaries = getResources().getStringArray(R.array.settings_variety_entries);
        mVariety.setSummary(varietySummaries[variety]);
        
        mInverse.setChecked(SettingsProvider.getBoolean(SettingsProvider.KEY_INVERSE_MODE, false));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSensitivity) {
            int sensitivity = Integer.valueOf((String) newValue);
            String[] sensitivitySummaries = getResources().getStringArray(R.array.settings_sensitivity_entries);
            mSensitivity.setSummary(sensitivitySummaries[sensitivity]);
            SettingsProvider.putInt(SettingsProvider.KEY_SENSITIVITY, sensitivity);
            InputListener.loadSensitivity();
            return true;
        } else if (preference == mVariety) {
            int variety = Integer.valueOf((String) newValue);
            String[] varietySummaries = getResources().getStringArray(R.array.settings_variety_entries);
            mVariety.setSummary(varietySummaries[variety]);
            SettingsProvider.putInt(SettingsProvider.KEY_VARIETY, variety);
            
            // Variety switch, must clear saved state and call MainActivity not to save
            clearState();
            
            Toast.makeText(this, R.string.msg_restart, 1000).show();
            return true;
        } else if (preference == mInverse) {
            boolean inverse = (boolean) newValue;
            SettingsProvider.putBoolean(SettingsProvider.KEY_INVERSE_MODE, inverse);
            MainView.inverseMode = inverse;
            return true;
        } else  {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void clearState() {
        getSharedPreferences("state", Context.MODE_WORLD_READABLE)
                 .edit()
                 .remove("size")
                 .commit();
        MainActivity.save = false;
    }
}
