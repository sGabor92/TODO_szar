package hu.webandmore.todo.ui.todo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.webandmore.todo.R;
import hu.webandmore.todo.api.model.Category;
import hu.webandmore.todo.utils.PrefUtils;

public class CreateTodoActivity extends AppCompatActivity implements CreateTodoScreen {

    @BindView(R.id.todoName)
    EditText mTodoName;

    @BindView(R.id.todoDescription)
    EditText mTodoDescription;

    @BindView(R.id.todoCategory)
    Spinner mTodoCategorySpinner;

    @BindView(R.id.todoPriority)
    Spinner mTodoPrioritySpinner;

    CreateTodoPresenter createTodoPresenter;

    private ArrayAdapter<CharSequence> categoryAdapter;
    private ArrayAdapter<CharSequence> priorityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_todo);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        createTodoPresenter = new CreateTodoPresenter();

        String[] categories = getResources().getStringArray(R.array.todo_category_spinner_array);
        categoryAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, categories);

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTodoCategorySpinner.setAdapter(categoryAdapter);

        String[] priorities = getResources().getStringArray(R.array.todo_priority_spinner_array);
        priorityAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, priorities);

        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTodoPrioritySpinner.setAdapter(priorityAdapter);
    }

    @OnClick(R.id.cancel)
    public void cancel() {
        finish();
    }

    @OnClick(R.id.addNewTodo)
    public void addNewTodo() {

        int todoId = PrefUtils.getIdFromPrefs(
                this,
                PrefUtils.PREFS_TODO_ID_KEY,
                0);

        String todoIdString = String.valueOf(todoId++);
        String name = mTodoName.getText().toString();
        String description = mTodoDescription.getText().toString();
        String category = mTodoCategorySpinner.getSelectedItem().toString();
        String priority = mTodoPrioritySpinner.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            mTodoName.setError(getString(R.string.required_field), null);
            focusView = mTodoName;
            cancel = true;
        } else if (TextUtils.isEmpty(description)) {
            mTodoDescription.setError(getString(R.string.required_field), null);
            focusView = mTodoDescription;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            createTodoPresenter.writeNewTodo(todoIdString, name, description, category, priority);
            PrefUtils.saveIdPrefs(this, PrefUtils.PREFS_TODO_ID_KEY, todoId++);
        }
    }


    @Override
    public void showError(String errorMsg) {

    }
}
