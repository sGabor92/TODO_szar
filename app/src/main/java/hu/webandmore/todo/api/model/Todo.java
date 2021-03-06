package hu.webandmore.todo.api.model;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Locale;

@IgnoreExtraProperties
public class Todo {

    private String id;
    private String name;
    private String description;
    private Priority priority;
    private String category;
    private long deadline;
    private Location location;
    @Exclude
    private boolean isExpanded = false;
    @Exclude
    private String myFormat = "yyyy-MM-dd : HH:mm";
    @Exclude
    private SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

    public Todo() {
    }

    public Todo(String id, String name, String description, Priority priority, String category,
         long deadline, Location location) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.deadline = deadline;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Exclude
    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    @Exclude
    public String getDeadlineString() {
        return sdf.format(deadline);
    }
}


