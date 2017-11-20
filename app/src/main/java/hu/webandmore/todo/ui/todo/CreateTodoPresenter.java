package hu.webandmore.todo.ui.todo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hu.webandmore.todo.api.model.Location;
import hu.webandmore.todo.api.model.Priority;
import hu.webandmore.todo.api.model.Todo;
import hu.webandmore.todo.ui.Presenter;

public class CreateTodoPresenter extends Presenter<CreateTodoScreen> {

    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    private static String userTodoRef;

    CreateTodoPresenter() {
        userTodoRef = "todos_" + user.getUid();
    }

    void writeNewTodo(String todoId, String name, String description,
                      String category, String priority, Location location, long deadline) {

        String generatedId = "todo_" + todoId;

        Todo todo = new Todo();
        todo.setId(generatedId);
        todo.setName(name);
        todo.setDescription(description);
        todo.setCategory(category);
        todo.setPriority(Priority.valueOf(priority.toUpperCase()));
        if(location != null) {
            todo.setLocation(location);
        }
        todo.setDeadline(deadline);

        mDatabaseRef.child(userTodoRef).child(category).child(generatedId).setValue(todo);
    }

}
