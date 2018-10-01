package me.jimmyhuang.travelerstrack.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import me.jimmyhuang.travelerstrack.ChildActivity;
import me.jimmyhuang.travelerstrack.MainActivity;
import me.jimmyhuang.travelerstrack.R;
import me.jimmyhuang.travelerstrack.database.TaskDatabase;
import me.jimmyhuang.travelerstrack.model.Task;
import me.jimmyhuang.travelerstrack.utility.AppExecutors;


public class TaskWidgetListProvider implements RemoteViewsService.RemoteViewsFactory {

    private List<Task> mWidgetItemList = new ArrayList<>();
    private Context mContext;
    private int mAppWidgetId;
    private int mParentId;
    private Task mParent;
    private boolean lock = false;

    public TaskWidgetListProvider(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mParentId = MainActivity.loadParentPref(context, mAppWidgetId);
    }

    @Override
    public void onCreate() {
        AppWidgetManager.getInstance(mContext).notifyAppWidgetViewDataChanged(mAppWidgetId, R.id.task_widget_lv);

    }

    @Override
    public void onDataSetChanged() {
        // Not on main thread
        TaskDatabase db = TaskDatabase.getsInstance(mContext);
        mWidgetItemList = db.taskDao().loadChildren(mParentId);
        mParent = db.taskDao().loadParent(mParentId);
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return mWidgetItemList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews( mContext.getPackageName(),
                R.layout.task_widget_list);
        if (mWidgetItemList.size() > 0) {
            Task task = mWidgetItemList.get(position);
            remoteView.setTextViewText(R.id.task_widget_list_item_tv, task.getHeader());

            Bundle extras = new Bundle();
            extras.putParcelable(ChildActivity.PARENT, mParent);
            Intent intent = new Intent();
            intent.putExtras(extras);
            remoteView.setOnClickFillInIntent(R.id.task_widget_list_item_container, intent);
        }
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return mWidgetItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
