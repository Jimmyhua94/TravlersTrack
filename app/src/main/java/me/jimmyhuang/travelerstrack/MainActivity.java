package me.jimmyhuang.travelerstrack;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import me.jimmyhuang.travelerstrack.fragment.TaskFragment;
import me.jimmyhuang.travelerstrack.widget.TaskWidgetProvider;

public class MainActivity extends AppCompatActivity {

    public static final String ACTIVITY = "Main";
    private static final String PREFS_NAME = "me.jimmyhuang.travelerstrack.TaskWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private TaskFragment mTaskFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putString(TaskFragment.ACTIVITY, ACTIVITY);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish();
                return;
            }
        }

        if (savedInstanceState == null) {
            mTaskFragment = new TaskFragment();
            mTaskFragment.setArguments(fragmentBundle);

            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .add(R.id.main_activity_fragment_container, mTaskFragment)
                    .commit();
        } else {
            mTaskFragment = (TaskFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main_activity_fragment_container);
        }

        if (mAppWidgetId != 0) {
            mTaskFragment.setWidgetCallback(mWidgetCallback);
        }


        FloatingActionButton addFab = findViewById(R.id.add_task_fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddTaskActivity.class);
                intent.putExtra(AddTaskActivity.ACTIVITY, ACTIVITY);
                startActivity(intent);
            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        setSupportActionBar((Toolbar) findViewById(R.id.main_tb));
    }

    public static void saveParentPref(Context context, int appWidgetId, int id) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId, id);
        prefs.apply();
    }

    public static int loadParentPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int parentId = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, 0);
        return parentId;
    }

    public static void deleteParentPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    public interface WidgetCallback{
        void setWidget(int parentId);
    }

    WidgetCallback mWidgetCallback = new WidgetCallback() {
        @Override
        public void setWidget(int parentId) {
            final Context context = MainActivity.this;

            saveParentPref(context, mAppWidgetId, parentId);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            TaskWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };
}
