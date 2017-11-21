package hu.webandmore.todo.api.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Locale;

@IgnoreExtraProperties
public class Todo {

    private String id;
    private String name;
    private String description;
    private Priority priority;
    //private Category category;
    //private String priority;
    private String category;
    private long deadline;
    private Location location;
    private boolean isExpanded = false;

    private String myFormat = "yyyy-MM-dd : HH:mm";
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

    /*public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }*/

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

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getDeadlineString() {
        return sdf.format(deadline);
    }
}


