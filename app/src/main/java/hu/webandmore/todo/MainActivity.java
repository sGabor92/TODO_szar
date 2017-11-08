package hu.webandmore.todo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoName = (TextView) findViewById(R.id.todoName);
        todoDescription = (TextView) findViewById(R.id.todoDescription);

        writeNewTodo("4", "Negyedik", "Ez a negyedik todom!");
        // TODO - vagy külön-külön todos_user.getUid vagy user modell és felüldefiniálása valahogy

    }

    @Override
    protected void onStart() {
        super.onStart();

        mTodoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Todo todo = ds.getValue(Todo.class);
                    if(todo.getId().contains(user.getUid())) {
                        System.out.println("NAME: " + todo.getName());
                        System.out.println("Description: " + todo.getDescription());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void writeNewTodo(String todoId, String name, String description) {
        String generatedId = "todo_" + todoId + "_" + user.getUid();

        Todo todo = new Todo();
        todo.setId(generatedId);
        todo.setName(name);
        todo.setDescription(description);

        mDatabaseRef.child("todos").child(generatedId).setValue(todo);
    }
}
