package de.h3ndr1k.desktopnotifications;

import android.app.Notification;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import de.h3ndr1k.desktopnotifications.api.DNotification;
import de.h3ndr1k.desktopnotifications.api.NotificationBackend;
import de.h3ndr1k.desktopnotifications.api.StatusResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NLService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();

    private PreferenceChangeListener preferenceChangeListener;
    public static final String PREF_BASE_URL = "de.h3ndr1k.dn.BASE_URL";
    public static final String PREF_CODE = "de.h3ndr1k.dn.CODE";
    public static final String PREF_ACTIVE = "de.h3ndr1k.dn.ACTIVE";

    NotificationBackend backend = null;
    String code = "";

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();
        preferenceChangeListener = new PreferenceChangeListener();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        updateConfig();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private void updateConfig() {
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_ACTIVE, false)) {
            backend = null;
            code = "";
            Log.i(TAG, "config updated - disabled");
        } else {
            String baseUrl = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_BASE_URL, "");
            if (!baseUrl.equals("")) {
                backend = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build().create(NotificationBackend.class);
                code = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_CODE, "");
                Log.i(TAG, "config updated - success");
            } else {
                Log.i(TAG, "config updated - invalid url");
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        Log.i(TAG, "onNotificationPosted()");
        if (backend == null) {
            Log.i(TAG, "ignoring - missing backend");
            return;
        }
        PackageManager pm = getPackageManager();
        Bundle extras = notification.getNotification().extras;
        String title = extras.getCharSequence(Notification.EXTRA_TITLE, "").toString();
        String text = extras.getCharSequence(Notification.EXTRA_TEXT, "").toString();
        long postTime = notification.getPostTime();
        String color = Integer.toHexString(notification.getNotification().color);
        String infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT, "").toString();
        String pkg = notification.getPackageName();
        Drawable appIcon = null;
        String appLabel = null;
        String appIconEncoded = null;
        try {
            appIcon = pm.getApplicationIcon(pkg);
            appLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(TAG, "Package not found", e);
        }
        if (appIcon != null) {
            try {
                Bitmap appIconBm = ((BitmapDrawable) appIcon).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                appIconBm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                appIconEncoded = new String(Base64.encode(stream.toByteArray(), 0));
                appIconEncoded = "data:image/png;base64," + appIconEncoded.replaceAll("\\n", "");
            } catch (ClassCastException e) {
                // This method will fail for VectorDrawables - They cannot be converted to bitmaps
                Log.i(TAG, "Could not get app icon", e);
            }
        }

        DNotification dNotification = new DNotification();
        dNotification.title = title;
        dNotification.text = text;
        dNotification.postTime = postTime;
        dNotification.color = color;
        dNotification.infoText = infoText;
        dNotification.pkg = pkg;
        dNotification.appLabel = appLabel;
        dNotification.appIcon = appIconEncoded;

        backend.notify(code, dNotification).enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(Call<StatusResponse> call, Response<StatusResponse> response) {
                Log.i(TAG, "onResponse()");
            }

            @Override
            public void onFailure(Call<StatusResponse> call, Throwable throwable) {
                Log.e(TAG, "onFailure()", throwable);
            }
        });
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "onNotificationRemoved()");
    }

    @Override
    public void onListenerConnected() {
        Log.i(TAG, "onListenerConnected()");
    }

    @Override
    public void onListenerDisconnected() {
        Log.i(TAG, "onListenerDisconnected()");
    }

    class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateConfig();
        }
    }

}
