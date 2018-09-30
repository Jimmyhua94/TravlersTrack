package me.jimmyhuang.travelerstrack.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import me.jimmyhuang.travelerstrack.database.TaskListTypeConverter;

@Entity
public class Task implements Parcelable{

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int order;
    private int parent;
    @TypeConverters(TaskListTypeConverter.class)
    private List<Task> children;
    private boolean completed;
    private String header;
    private String description;
    private String location;
    private String geoCode;

    public Task(String header, String description) {
        this.header = header;
        this.description = description;
        completed = false;
    }

    private Task(Parcel in) {
        id = in.readInt();
        order = in.readInt();
        parent = in.readInt();
        children = in.createTypedArrayList(Task.CREATOR);
        completed = in.readByte() != 0;
        header = in.readString();
        description = in.readString();
        location = in.readString();
        geoCode = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(order);
        dest.writeInt(parent);
        dest.writeTypedList(children);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeString(header);
        dest.writeString(description);
        dest.writeString(location);
        dest.writeString(geoCode);
    }

    public final static Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel parcel) {
            return new Task(parcel);
        }

        @Override
        public Task[] newArray(int i) {
            return new Task[i];
        }
    };

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public int getParent() { return parent; }
    public void setParent(int parent) { this.parent = parent; }

    public List<Task> getChildren() { return children; }
    public void setChildren(List<Task> children) { this.children = children; }

    public boolean getCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getGeoCode() { return geoCode; }
    public void setGeoCode(String geoCode) { this.geoCode = geoCode; }
}
