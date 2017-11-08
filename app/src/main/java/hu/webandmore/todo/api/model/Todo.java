package hu.webandmore.todo.api.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Todo {

    private String id;
    private String name;
    private String description;
    private Priority priority;
    private Category category;
    private long deadline;
    private Location location;

    public Todo() {
    }

    public Todo(String id, String name, String description, Priority priority, Category category,
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
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
}


