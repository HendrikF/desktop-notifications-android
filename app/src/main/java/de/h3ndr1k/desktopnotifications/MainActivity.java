package de.h3ndr1k.desktopnotifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import okhttp3.HttpUrl;

public class MainActivity extends Activity {
    private String TAG = this.getClass().getSimpleName();

    private View.OnClickListener onClickListener;

    private EditText etBaseUrl;
    private EditText etCode;
    private Switch swActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onClickListener = new ClickListener();

        findViewById(R.id.openSettings).setOnClickListener(onClickListener);
        findViewById(R.id.confirm).setOnClickListener(onClickListener);

        etBaseUrl = (EditText) findViewById(R.id.etBaseUrl);
        etCode = (EditText) findViewById(R.id.etCode);
        swActive = (Switch) findViewById(R.id.active);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        etBaseUrl.setText(sharedPreferences.getString(NLService.PREF_BASE_URL, "https://h3ndr1k.de/dn/"));
        etCode.setText(sharedPreferences.getString(NLService.PREF_CODE, ""));
        swActive.setChecked(sharedPreferences.getBoolean(NLService.PREF_ACTIVE, true));
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.openSettings){
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            } else if (view.getId() == R.id.confirm) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String baseUrl = etBaseUrl.getText().toString();
                if (HttpUrl.parse(baseUrl) == null) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("Not a valid Url!").setMessage("'" + baseUrl + "' is not a valid Url!").show();
                    return;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(NLService.PREF_BASE_URL, baseUrl);
                editor.putString(NLService.PREF_CODE, etCode.getText().toString());
                editor.putBoolean(NLService.PREF_ACTIVE, swActive.isChecked());
                editor.apply();
            }
        }
    }

}
