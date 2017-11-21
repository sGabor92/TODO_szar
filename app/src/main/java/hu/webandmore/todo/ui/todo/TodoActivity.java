package hu.webandmore.todo.ui.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import hu.webandmore.todo.R;
import hu.webandmore.todo.adapter.TodoAdapter;
import hu.webandmore.todo.adapter.TodoSectionsAdapter;
import hu.webandmore.todo.api.model.Todo;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class TodoActivity extends AppCompatActivity implements TodoScreen {

    @BindView(R.id.todoRecyclerView)
    RecyclerView mTodorecyclerView;

    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mTodoRef;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    private static String userTodoRef;

    private LinearLayoutManager llmTodos;
    private TodoAdapter todoAdapter;

    private SectionedRecyclerViewAdapter sectionAdapter;
    private TodoSectionsAdapter todoSectionsAdapter;

    private TodoPresenter todoPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        todoPresenter = new TodoPresenter(this);

        sectionAdapter = new SectionedRecyclerViewAdapter();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addNewTodo);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CreateTodoActivity.class);
                startActivity(intent);
            }
        });

        userTodoRef = "todos_" + user.getUid();
        mTodoRef = mDatabaseRef.child(userTodoRef);

        llmTodos = new LinearLayoutManager(this);
        llmTodos.setOrientation(LinearLayoutManager.VERTICAL);

        todoPresenter.attachScreen(this);

        todoPresenter.initSwipe();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTodoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sectionAdapter.removeAllSections();
                sectionAdapter.notifyDataSetChanged();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ArrayList<Todo> todosByCategory = new ArrayList<>();
                    Todo todo = new Todo();
                    for (DataSnapshot dsChild : ds.getChildren()) {
                        todo = dsChild.getValue(Todo.class);
                        todosByCategory.add(todo);
                    }
                    todoSectionsAdapter = new TodoSectionsAdapter(
                            getApplicationContext(), sectionAdapter, ds.getKey(), todosByCategory);
                    sectionAdapter.addSection(todo.getCategory() ,todoSectionsAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTodorecyclerView.setLayoutManager(llmTodos);
        mTodorecyclerView.setAdapter(sectionAdapter);
    }

    @Override
    public RecyclerView getTodosRecyclerView() {
        if(mTodorecyclerView == null) {
            System.out.println("NULL!!!");
        }
        return mTodorecyclerView;
    }

    @Override
    public void removeTodo(int position) {
        Log.i("TODOACTIVITY", "Delete todo");
        int relativePosition = sectionAdapter.getPositionInSection(position);
        todoSectionsAdapter.removeItem(relativePosition);
    }

    @Override
    public void restoreTodo(int position) {
        Log.i("TODOACTIVITY", "Restore todo: " + position);
        int relativePosition = sectionAdapter.getPositionInSection(position);
        /*Todo removedTodo = todoSectionsAdapter.getItem(relativePosition);
        todoSectionsAdapter.removeItem(relativePosition);
        todoSectionsAdapter.restoreItem(relativePosition, removedTodo);*/
    }

    @Override
    public Todo getItem(int position) {
        return todoSectionsAdapter.getItem(position);
    }
}
