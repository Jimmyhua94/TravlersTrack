package me.jimmyhuang.travelerstrack.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import me.jimmyhuang.travelerstrack.AddTaskActivity;
import me.jimmyhuang.travelerstrack.ChildActivity;
import me.jimmyhuang.travelerstrack.MainActivity;
import me.jimmyhuang.travelerstrack.R;
import me.jimmyhuang.travelerstrack.database.TaskDatabase;
import me.jimmyhuang.travelerstrack.model.Task;
import me.jimmyhuang.travelerstrack.utility.AppExecutors;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> mTasks;

    private RecyclerView mRecyclerView;

    private Context mContext;

    private TaskClickListener mOnClickListener = new TaskClickListener();
    private TaskLongClickListener mOnLongClickListener = new TaskLongClickListener();

    private MainActivity.WidgetCallback mWidgetCallback;

    public TaskAdapter(List<Task> tasks) { mTasks = tasks; }

    public TaskAdapter(List<Task> tasks, MainActivity.WidgetCallback callback) {
        mTasks = tasks;
        mWidgetCallback = callback;
    }

    @NonNull
    @Override
    public TaskAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.item_task, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);

        mRecyclerView = rv;

        mContext = rv.getContext();
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.TaskViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox mTaskCheckBox;
        private final TextView mTaskHeader;
        private final TextView mTaskDescription;
        private TaskDatabase mDb;

        private TaskViewHolder(View itemView) {
            super(itemView);

            mTaskCheckBox = itemView.findViewById(R.id.task_cb);
            mTaskHeader = itemView.findViewById(R.id.task_header_tv);
            mTaskDescription = itemView.findViewById(R.id.task_description_tv);
            itemView.setOnClickListener(mOnClickListener);
            itemView.setOnLongClickListener(mOnLongClickListener);
            mDb = TaskDatabase.getsInstance(mContext);

            mTaskCheckBox.setOnClickListener(new completeClickListener());
        }

        private void bind(int position) {
            Task task = mTasks.get(position);
            if (task != null) {
                String header = task.getHeader();
                mTaskHeader.setText(header);

                String description = task.getDescription();
                mTaskDescription.setText(description);

                mTaskCheckBox.setChecked(task.getCompleted());
            }
        }

        private class completeClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                final Task task = mTasks.get(position);

                if (mWidgetCallback == null) {
                    AppExecutors.getsInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            task.setCompleted(mTaskCheckBox.isChecked());
                            mDb.taskDao().updateTask(task);
                        }
                    });
                }
            }
        }
    }

    private class TaskClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildLayoutPosition(v);
            Task task = mTasks.get(position);

            if (mWidgetCallback == null) {
                Intent intent = new Intent(mContext, ChildActivity.class);
                intent.putExtra(ChildActivity.PARENT, task);
                mContext.startActivity(intent);
            } else {
                mWidgetCallback.setWidget(task.getId());
            }
        }
    }

    private class TaskLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            int position = mRecyclerView.getChildLayoutPosition(v);
            Task task = mTasks.get(position);

            if (mWidgetCallback == null) {
                Intent intent = new Intent(mContext, AddTaskActivity.class);
                intent.putExtra(AddTaskActivity.TASK, task);
                mContext.startActivity(intent);
            }

            return true;
        }
    }
}
