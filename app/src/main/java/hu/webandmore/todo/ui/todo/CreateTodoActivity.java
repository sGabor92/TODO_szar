package hu.webandmore.todo.ui.todo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.webandmore.todo.R;
import hu.webandmore.todo.api.model.Category;
import hu.webandmore.todo.api.model.Location;
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

    @BindView(R.id.todoDeadline)
    EditText mTodoDeadline;

    PlaceAutocompleteFragment autocompleteFragment;

    CreateTodoPresenter createTodoPresenter;

    private Location location;
    private Calendar myCalendar = Calendar.getInstance();
    private String myFormat = "yyyy-MM-dd : HH:mm";
    private SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

    int mHour;
    int mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_todo);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        createTodoPresenter = new CreateTodoPresenter();

        String[] categories = getResources().getStringArray(R.array.todo_category_spinner_array);
        ArrayAdapter<CharSequence> categoryAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, categories);

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTodoCategorySpinner.setAdapter(categoryAdapter);

        String[] priorities = getResources().getStringArray(R.array.todo_priority_spinner_array);
        ArrayAdapter<CharSequence> priorityAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, priorities);

        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTodoPrioritySpinner.setAdapter(priorityAdapter);

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.location);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                location = new Location();
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                location.setAddress(place.getAddress().toString());
                Log.i("PROFILE PLACE", "Place: " + location.getLatitude() + ":::" +
                        location.getLongitude() + ":::" + location.getAddress());
            }

            @Override
            public void onError(Status status) {
                Log.i("PROFILE PLACE", "An error occurred: " + status);
            }
        });

        mTodoDeadline.setOnClickListener(editTodoDeadlineListener);

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

        long deadline = 0L;
        if (!TextUtils.isEmpty(mTodoDeadline.getText())) {
            deadline = myCalendar.getTimeInMillis() / 1000;
        }

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
            createTodoPresenter.writeNewTodo(todoIdString, name, description, category,
                    priority, location, deadline);
            PrefUtils.saveIdPrefs(this, PrefUtils.PREFS_TODO_ID_KEY, todoId++);
        }
    }

    @Override
    public void showError(String errorMsg) {

    }

    final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimePicker();
        }

    };

    View.OnClickListener editTodoDeadlineListener = new View.OnClickListener() {
        public void onClick(View v) {
            new DatePickerDialog(v.getContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    };

    private void updateLabel(EditText editText) {
        editText.setText(sdf.format(myCalendar.getTime()));
    }

    private void showTimePicker() {
        mHour = myCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = myCalendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;
                        myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        myCalendar.set(Calendar.MINUTE, minute);

                        updateLabel(mTodoDeadline);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

}
