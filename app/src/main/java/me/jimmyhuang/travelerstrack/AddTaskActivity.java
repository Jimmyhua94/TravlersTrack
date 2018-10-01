package me.jimmyhuang.travelerstrack;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import me.jimmyhuang.travelerstrack.database.TaskDatabase;
import me.jimmyhuang.travelerstrack.model.Task;
import me.jimmyhuang.travelerstrack.utility.AppExecutors;
import me.jimmyhuang.travelerstrack.widget.TaskWidgetListProvider;
import me.jimmyhuang.travelerstrack.widget.TaskWidgetProvider;

public class AddTaskActivity extends AppCompatActivity {

    public static final String ACTIVITY = "add_task";
    public static final String TASK = "task";
    private static final String INPUT_ERROR = "add_task_error";

    private Task mTask;
    private int mParent;
    private TaskDatabase mDb;

    private Context mContext = this;

    private EditText mHeader;
    private EditText mDescription;
    private EditText mOrder;
    private PlaceAutocompleteFragment mPlaceAutoComplete;
    private Button mButton;

    private String mGeocode;
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mParent = intent.getIntExtra(ChildActivity.PARENT, 0);
        mTask = intent.getParcelableExtra(TASK);

        setContentView(R.layout.activity_add_task);

        mHeader = findViewById(R.id.add_task_header_et);
        mDescription = findViewById(R.id.add_task_description_et);
        mOrder = findViewById(R.id.add_task_order_et);
        mButton = findViewById(R.id.add_task_button);

        mPlaceAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        mPlaceAutoComplete.setHint(getResources().getString(R.string.add_location));
        mPlaceAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mGeocode = String.valueOf(place.getLatLng().latitude) + ',' + String.valueOf(place.getLatLng().longitude);
                mLocation = place.getName().toString();
            }

            @Override
            public void onError(Status status) {
            }
        });

        if (mTask != null) {
            mPlaceAutoComplete.setText(mTask.getLocation());
            mHeader.setText(mTask.getHeader());
            mDescription.setText(mTask.getDescription());
            mOrder.setText(String.valueOf(mTask.getOrder()));
        }

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String header = mHeader.getText().toString();
                String description = mDescription.getText().toString();
                String orderString = mOrder.getText().toString();
                int order = 0;
                if (!orderString.isEmpty()) {
                    order = Integer.parseInt(orderString);
                }

                if (!header.isEmpty() && !description.isEmpty()) {
                    mDb = TaskDatabase.getsInstance(v.getContext());
                    if (mTask != null) {
                        if (mGeocode != null && !mGeocode.isEmpty()) {
                            mTask.setGeoCode(mGeocode);
                            mTask.setLocation(mLocation);
                        }
                        mTask.setHeader(header);
                        mTask.setDescription(description);
                        mTask.setOrder(order);
                        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.taskDao().updateTask(mTask);
                                finish();
                            }
                        });
                    } else {
                        final Task task = new Task(header, description);
                        task.setOrder(order);
                        if (mParent != 0) {
                            task.setParent(mParent);
                        }
                        if (mGeocode != null && !mGeocode.isEmpty()) {
                            task.setGeoCode(mGeocode);
                            task.setLocation(mLocation);
                        }

                        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.taskDao().insertTask(task);
                                finish();
                            }
                        });
                    }

                    Intent widgetIntent = new Intent(mContext, TaskWidgetProvider.class);
                    widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int[] widgetIds = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), TaskWidgetProvider.class));
                    widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                    sendBroadcast(widgetIntent);
                } else {
                    Log.i(INPUT_ERROR, "Header " + mHeader + " Description " + mDescription);
                    Toast.makeText(mContext, getResources().getString(R.string.add_task_required), Toast.LENGTH_LONG).show();
                }
            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);

        setSupportActionBar((Toolbar) findViewById(R.id.task_tb));
    }
}
