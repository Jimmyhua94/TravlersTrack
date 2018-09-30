package me.jimmyhuang.travelerstrack.database;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class ActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Application mApplication;
    private int mId = 0;

    public ActivityViewModelFactory(Application application) {
        mApplication = application;
    }

    public ActivityViewModelFactory(Application application, int id) {
        mApplication = application;
        mId = id;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new ActivityViewModel(mApplication, mId);
    }
}
