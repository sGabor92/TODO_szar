package hu.webandmore.todo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import hu.webandmore.todo.api.model.Todo;
import hu.webandmore.todo.utils.Util;

public class MainActivity extends AppCompatActivity {

    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mTodoRef;

    TextView todoName;
    TextView todoDescription;
    Button logout;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    private static String userTodoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoName = (TextView) findViewById(R.id.todoName);
        todoDescription = (TextView) findViewById(R.id.todoDescription);
        logout = (Button) findViewById(R.id.logout);

        userTodoRef = "todos_" + user.getUid();

        mTodoRef = mDatabaseRef.child(userTodoRef);


        writeNewTodo("2", "Masodik", "Ez a masodik todom!");
        // TODO - vagy külön-külön todos_user.getUid vagy user modell és felüldefiniálása valahogy

    }

    @Override
    protected void onStart() {
        super.onStart();

        mTodoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
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
        String userOwnTodos = userTodoRef;
        String generatedId = "todo_" + todoId;

        Todo todo = new Todo();
        todo.setId(generatedId);
        todo.setName(name);
        todo.setDescription(description);

        mDatabaseRef.child(userOwnTodos).child(generatedId).setValue(todo);
        //mDatabaseRef.child(generatedId).setValue(todo);
    }

    public void logoutUser(View v) {
        Util.userLogout(this);
    }
}
