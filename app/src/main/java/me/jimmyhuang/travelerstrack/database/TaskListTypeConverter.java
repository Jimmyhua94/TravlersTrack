package me.jimmyhuang.travelerstrack.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import me.jimmyhuang.travelerstrack.model.Task;

// https://guides.codepath.com/android/leveraging-the-gson-library
public class TaskListTypeConverter {

    @TypeConverter
    public static String taskToString(List<Task> tasks) {
        return new Gson().toJson(tasks);
    }

    @TypeConverter
    public static List<Task> stringToTask(String tasks) {
        return new Gson().fromJson(tasks, new TypeToken<List<Task>>(){}.getType());
    }
}
