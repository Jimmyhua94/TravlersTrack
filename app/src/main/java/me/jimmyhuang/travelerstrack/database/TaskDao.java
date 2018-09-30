package me.jimmyhuang.travelerstrack.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import me.jimmyhuang.travelerstrack.model.Task;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM task WHERE id = :id")
    Task loadParent(int id);

    @Query("SELECT * FROM task WHERE parent = 0 ORDER BY `order`")
    List<Task> loadParents();

    @Query("SELECT * FROM task WHERE parent = 0 ORDER BY `order`")
    LiveData<List<Task>> loadParentsLv();

    @Query("SELECT * FROM task WHERE parent = :parent ORDER BY `order`")
    List<Task> loadChildren(int parent);

    @Query("SELECT * FROM task WHERE parent = :parent ORDER BY `order`")
    LiveData<List<Task>> loadChildrenLv(int parent);

    @Insert
    void insertTask(Task task);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("DELETE FROM task WHERE parent = :parent")
    void deleteChildren(int parent);
}
