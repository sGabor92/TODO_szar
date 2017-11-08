package hu.webandmore.todo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hu.webandmore.todo.api.model.Todo;

public class MainActivity extends AppCompatActivity {

    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mTodoRef = mDatabaseRef.child("todos");

    TextView todoName;
    TextView todoDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoName = (TextView) findViewById(R.id.todoName);
        todoDescription = (TextView) findViewById(R.id.todoDescription);

        writeNewTodo("2", "Második", "Ez a második todom!");

    }

    @Override
    protected void onStart() {
        super.onStart();

        mTodoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Todo todo = ds.getValue(Todo.class);
                    System.out.println("NAME: " + todo.getName());
                    System.out.println("Description: " + todo.getDescription());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void writeNewTodo(String todoId, String name, String description) {
        String generatedId = "todo_" + todoId;

        Todo todo = new Todo();
        todo.setId(generatedId);
        todo.setName(name);
        todo.setDescription(description);

        mDatabaseRef.child("todos").child(generatedId).setValue(todo);
    }
}
