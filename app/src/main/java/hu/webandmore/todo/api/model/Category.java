package hu.webandmore.todo.api.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Category {

    private int id;
    private String name;

    Category(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
