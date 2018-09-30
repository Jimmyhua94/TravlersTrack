package me.jimmyhuang.travelerstrack.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

import me.jimmyhuang.travelerstrack.model.Task;

public class ActivityViewModel extends AndroidViewModel {

    private LiveData<List<Task>> mTasks;

    public ActivityViewModel(@NonNull Application application, int id) {
        super(application);
        TaskDatabase database = TaskDatabase.getsInstance(this.getApplication());
        if (id != 0) {
            mTasks = database.taskDao().loadChildrenLv(id);
        } else {
            mTasks = database.taskDao().loadParentsLv();
        }
    }

    public LiveData<List<Task>> getTasks() {
        return mTasks;
    }
}
