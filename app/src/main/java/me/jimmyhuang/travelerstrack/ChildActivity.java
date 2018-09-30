package me.jimmyhuang.travelerstrack;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import me.jimmyhuang.travelerstrack.fragment.TaskFragment;
import me.jimmyhuang.travelerstrack.model.Task;
import me.jimmyhuang.travelerstrack.utility.NetworkUtil;

import static java.lang.Float.parseFloat;
import static me.jimmyhuang.travelerstrack.utility.JsonUtil.getWeatherTemp;

public class ChildActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String> {

    public static final String ACTIVITY = "child";
    public static final String PARENT = "parent";
    private static final String MAP = "map";
    private static final int LOADER = 21;
    private static final String LOADER_URL = "loader_url";

    private TaskFragment mTaskFragment;
    private MapView mMap;
    private GoogleMap mGMap;

    private Task mParentTask;
    private List<String> mGeocode;

    private TextView mTempTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        mTempTv = findViewById(R.id.temp_tv);

        Intent intent = getIntent();
        mParentTask = intent.getParcelableExtra(PARENT);

        String geocode = mParentTask.getGeoCode();
        String location = mParentTask.getLocation();
        if (geocode != null && !geocode.isEmpty()) {
            TextView location_tv = findViewById(R.id.location_tv);
            location_tv.setText(location);
            mGeocode = Arrays.asList(geocode.split(","));
        }

        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putString(TaskFragment.ACTIVITY, ACTIVITY);
        fragmentBundle.putInt(PARENT, mParentTask.getId());

        Bundle mapBundle = null;

        if (savedInstanceState == null) {
            mTaskFragment = new TaskFragment();
            mTaskFragment.setArguments(fragmentBundle);

            FragmentManager fragmentManager = getSupportFragmentManager();

            fragmentManager.beginTransaction()
                    .add(R.id.child_activity_fragment_container, mTaskFragment)
                    .commit();
        } else {
            mapBundle = savedInstanceState.getBundle(MAP);
            mTaskFragment = (TaskFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.child_activity_fragment_container);
        }

        mMap = findViewById(R.id.child_activity_mv);
        if (mGeocode != null && mGeocode.size() == 2) {
            mMap.onCreate(mapBundle);
            mMap.getMapAsync(this);

            LoaderManager loaderManager = getSupportLoaderManager();

            Bundle loaderBundle = new Bundle();
            loaderBundle.putString(LOADER_URL, NetworkUtil.buildWeatherUrl(mGeocode.get(0), mGeocode.get(1)).toString());

            Loader<String> weatherLoader = loaderManager.getLoader(LOADER);

            if (weatherLoader == null) {
                loaderManager.initLoader(LOADER, loaderBundle, this).onContentChanged();
            } else {
                loaderManager.restartLoader(LOADER, loaderBundle, this).onContentChanged();
            }
        } else {
            ViewGroup vg = (ViewGroup)(mMap.getParent());
            vg.removeView(mMap);
            vg.removeView(findViewById(R.id.location_container));
            mMap = null;
        }

        FloatingActionButton addFab = findViewById(R.id.add_task_fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AddTaskActivity.class);
                intent.putExtra(AddTaskActivity.ACTIVITY, ACTIVITY);
                intent.putExtra(PARENT, mParentTask.getId());
                startActivity(intent);
            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        setSupportActionBar((Toolbar) findViewById(R.id.child_tb));
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        if (mMap != null) {
            Bundle mapBundle = state.getBundle(MAP);
            if (mapBundle == null) {
                mapBundle = new Bundle();
                state.putBundle(MAP, mapBundle);
            }

            mMap.onSaveInstanceState(mapBundle);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mMap != null) {
            mGMap = googleMap;
            LatLng location = new LatLng(parseFloat(mGeocode.get(0)), parseFloat(mGeocode.get(1)));
            mGMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            mGMap.addMarker(new MarkerOptions()
                    .position(location));
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String>(getApplicationContext()) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    return;
                }
                if (takeContentChanged()) {
                    forceLoad();
                }
            }
            @Nullable
            @Override
            public String loadInBackground() {
                String weatherUrlString = args.getString(LOADER_URL);
                if (weatherUrlString == null || TextUtils.isEmpty(weatherUrlString)) {
                    return null;
                }
                try {
                    URL weatherUrl = new URL(weatherUrlString);
                    return NetworkUtil.getResponseFromHttpUrl(weatherUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            protected void onStopLoading() {
                cancelLoad();
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if (data != null && !data.equals("")) {
            mTempTv.setText(String.valueOf(getWeatherTemp(data)) + '°');
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}