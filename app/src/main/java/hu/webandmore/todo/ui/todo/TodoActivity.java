package hu.webandmore.todo.ui.todo;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.webandmore.todo.BuildConfig;
import hu.webandmore.todo.Manifest;
import hu.webandmore.todo.notification.NotificationOpenedHandler;
import hu.webandmore.todo.R;
import hu.webandmore.todo.adapter.TodoSectionsAdapter;
import hu.webandmore.todo.api.model.Todo;
import hu.webandmore.todo.geo.GeofenceErrorMessages;
import hu.webandmore.todo.geo.GeofenceTransitionsIntentService;
import hu.webandmore.todo.utils.Util;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class TodoActivity extends AppCompatActivity implements TodoScreen,
        OnCompleteListener<Void> {

    private static final String TAG = "TodoActivity";

    @BindView(R.id.todoRecyclerView)
    RecyclerView mTodorecyclerView;

    DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mTodoRef;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    private static String userTodoRef;

    private LinearLayoutManager llmTodos;

    private SectionedRecyclerViewAdapter sectionAdapter;
    private TodoSectionsAdapter todoSectionsAdapter;

    @BindView(R.id.geofencingOn)
    ImageButton mGeofencingOn;

    @BindView(R.id.geofencingOff)
    ImageButton mGeofencingOff;

    private TodoPresenter todoPresenter;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }
    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.ADD;

    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";
    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    private boolean geofencingOn = false;

    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationOpenedHandler())
                .init();

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

        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        if (getGeofencesAdded()) {
            mGeofencingOn.setVisibility(View.GONE);
            mGeofencingOff.setVisibility(View.VISIBLE);
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "Location Change Lat Lng " +
                            location.getLatitude() + " " + location.getLongitude());
                }
            }
        };

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();

        mTodoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sectionAdapter.removeAllSections();
                sectionAdapter.notifyDataSetChanged();
                mGeofenceList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ArrayList<Todo> todosByCategory = new ArrayList<>();
                    Todo todo = new Todo();
                    for (DataSnapshot dsChild : ds.getChildren()) {
                        todo = dsChild.getValue(Todo.class);
                        assert todo != null;
                        if (todo.getLocation() != null) {
                            addElementToGeofenceList(todo.getLocation().getAddress(),
                                    todo.getLocation().getLatitude(),
                                    todo.getLocation().getLongitude());
                        }
                        todosByCategory.add(todo);
                    }
                    todoSectionsAdapter = new TodoSectionsAdapter(
                            getApplicationContext(), todoPresenter, sectionAdapter, ds.getKey(), todosByCategory);
                    sectionAdapter.addSection(todo.getCategory(), todoSectionsAdapter);
                }
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    Log.i(TAG, "Calling performPendingGeofenceTask");
                    performPendingGeofenceTask();
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void removeTodo(Todo todo, int position) {
        todoSectionsAdapter.removeItem(todo, position);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        Log.i(TAG, "GEOFENCING LIST SIZE: " + mGeofenceList.size());
        return builder.build();
    }

    @OnClick(R.id.geofencingOn)
    public void addGeofencesHandler(View view) {
        geofencingOn = true;
        mGeofencingOn.setVisibility(View.GONE);
        mGeofencingOff.setVisibility(View.VISIBLE);
        //startLocationMonitor();
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            requestPermissions();
            return;
        }
        addGeofences();
    }

    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.permission_denied));
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    @OnClick(R.id.geofencingOff)
    public void removeGeofencesHandler(View view) {
        geofencingOn = false;
        mGeofencingOff.setVisibility(View.GONE);
        mGeofencingOn.setVisibility(View.VISIBLE);
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            requestPermissions();
            return;
        }
        removeGeofences();
    }

    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.permission_denied));
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            Log.i(TAG, "OnComplete successful");
            updateGeofencesAdded(!getGeofencesAdded());

            int messageId = !getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;

            Log.i(TAG, "Message: " + getString(messageId));
            showSnackbar(getString(messageId));
        } else {
            Log.i(TAG, "OnComplete  unsuccessful");
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void addElementToGeofenceList(String address, double latitude, double longitude) {
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId(address)
                .setCircularRegion(
                        latitude,
                        longitude,
                        100 // Geofences radius in meter
                )
                .setExpirationDuration(5000) //Expiration in milliseconds
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
        Log.i(TAG, address + ":" + latitude + "," + longitude);
        Log.i(TAG, "LIST SIZE: " + mGeofenceList.size());

    }

    private void performPendingGeofenceTask() {
        Log.i(TAG, "PerformPendingGeofenceTask: " + mGeofenceList.size());
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            Log.i(TAG, "addGeofences");
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            Log.i(TAG, "removeGeofences");
            removeGeofences();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.location_permission, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(TodoActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            ActivityCompat.requestPermissions(TodoActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                performPendingGeofenceTask();
            } else {
                showSnackbar(R.string.permission_denied, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                mPendingGeofenceTask = PendingGeofenceTask.NONE;
            }
        }
    }

    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                GEOFENCES_ADDED_KEY, false);
    }

    @OnClick(R.id.logout)
    public void logout() {
        Util.userLogout(this);
    }

}
