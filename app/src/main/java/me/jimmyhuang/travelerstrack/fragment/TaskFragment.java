package me.jimmyhuang.travelerstrack.fragment;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.jimmyhuang.travelerstrack.ChildActivity;
import me.jimmyhuang.travelerstrack.MainActivity;
import me.jimmyhuang.travelerstrack.R;
import me.jimmyhuang.travelerstrack.adapter.TaskAdapter;
import me.jimmyhuang.travelerstrack.database.ActivityViewModel;
import me.jimmyhuang.travelerstrack.database.ActivityViewModelFactory;
import me.jimmyhuang.travelerstrack.database.TaskDatabase;
import me.jimmyhuang.travelerstrack.model.Task;
import me.jimmyhuang.travelerstrack.utility.AppExecutors;
import me.jimmyhuang.travelerstrack.widget.TaskWidgetProvider;

public class TaskFragment extends Fragment{

    public static final String ACTIVITY = "task_fragment";
    public static final String TASKS = "tasks";

    private TaskAdapter mAdapter;
    private List<Task> mTasks = new ArrayList<>();
    private String mActivity;
    private int mParent = 0;

    private TaskDatabase mDb;

    private MainActivity.WidgetCallback mWidgetCallback;

    public TaskFragment() {}

    @Override
    public void onSaveInstanceState(Bundle currentState) {
        currentState.putParcelableArrayList(TASKS,(ArrayList<Task>) mTasks);
    }

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getArguments().getString(ACTIVITY);
        if (mActivity.equals(ChildActivity.ACTIVITY)) {
            mParent = getArguments().getInt(ChildActivity.PARENT);
        }

        final View rootView = inflator.inflate(R.layout.fragment_task, container, false);


        RecyclerView recyclerView = rootView.findViewById(R.id.fragment_task_rv);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(container.getContext());
        recyclerView.setLayoutManager(layoutManager);

        if (savedInstanceState != null) {
            mTasks = savedInstanceState.getParcelableArrayList(TASKS);
        }

        if (mWidgetCallback == null) {
            mAdapter = new TaskAdapter(mTasks);
        } else {
            mAdapter = new TaskAdapter(mTasks, mWidgetCallback);
        }
        recyclerView.setAdapter(mAdapter);

        getViewModel();

        if (mWidgetCallback == null) {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
                    int position = viewHolder.getAdapterPosition();
                    final Task task = mTasks.get(position);
                    if (direction == ItemTouchHelper.LEFT) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.delete_message)
                                .setTitle(R.string.delete_title);
                        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        deleteTasks(task);
                                    }
                                });
                                Intent widgetIntent = new Intent(getContext(), TaskWidgetProvider.class);
                                widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                                int[] widgetIds = AppWidgetManager.getInstance(getActivity().getApplication()).getAppWidgetIds(new ComponentName(getActivity().getApplication(), TaskWidgetProvider.class));
                                widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                                getActivity().sendBroadcast(widgetIntent);
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                        dialog.show();
                    } else {
                        AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                task.setCompleted(!task.getCompleted());
                                mDb.taskDao().updateTask(task);
                            }
                        });
                    }
                }
            }).attachToRecyclerView(recyclerView);
        }

        mDb = TaskDatabase.getsInstance(getContext());

        return rootView;
    }

    private void deleteTasks(Task task) {
        List<Task> children = mDb.taskDao().loadChildren(task.getId());
        for (Task childTask : children) {
            deleteTasks(childTask);
        }
        mDb.taskDao().deleteTask(task);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getViewModel() {
        ActivityViewModel viewModel = ViewModelProviders.of(this, new ActivityViewModelFactory(this.getActivity().getApplication(), mParent)).get(ActivityViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(@Nullable List<Task> tasks) {
                setTasks(tasks);
            }
        });
    }

    public void setTasks(List<Task> tasks) {
        mTasks.clear();
        mTasks.addAll(tasks);
        mAdapter.notifyDataSetChanged();
    }

    public void setWidgetCallback(MainActivity.WidgetCallback callback) {
        mWidgetCallback = callback;
    }
}