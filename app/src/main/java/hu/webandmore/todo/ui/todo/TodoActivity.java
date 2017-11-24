package hu.webandmore.todo.ui.todo;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import butterknife.OnClick;
import hu.webandmore.todo.BuildConfig;
import hu.webandmore.todo.Manifest;
import hu.webandmore.todo.R;
import hu.webandmore.todo.adapter.TodoSectionsAdapter;
import hu.webandmore.todo.api.model.Todo;
import hu.webandmore.todo.geo.GeofenceErrorMessages;
import hu.webandmore.todo.geo.GeofenceTransitionsIntentService;
import hu.webandmore.todo.utils.Util;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class TodoActivity extends AppCompatActivity implements TodoScreen, OnCompleteListener<Void> {

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

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.ADD;

    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";
    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    private boolean geofencingOn = false;

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

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        if(getGeofencesAdded()) {
            mGeofencingOn.setVisibility(View.GONE);
            mGeofencingOff.setVisibility(View.VISIBLE);
        }

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
    public void removeTodo(Todo todo, int position) {
        todoSectionsAdapter.removeItem(todo, position);
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);
        Log.i(TAG, "GEOFENCING LIST SIZE: " + mGeofenceList.size());

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    @OnClick(R.id.geofencingOn)
    public void addGeofencesHandler(View view) {
        geofencingOn = true;
        mGeofencingOn.setVisibility(View.GONE);
        mGeofencingOff.setVisibility(View.VISIBLE);
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            requestPermissions();
            return;
        }
        addGeofences();
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.permission_denied));
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
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

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.permission_denied));
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     *
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            Log.i(TAG, "OnComplete successful");
            updateGeofencesAdded(!getGeofencesAdded());

            int messageId = getGeofencesAdded() ? R.string.geofences_added :
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

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    @Override
    public void addElementToGeofenceList(String address, double latitude, double longitude) {

        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(address)

                // Set the circular region of this geofence.
                .setCircularRegion(
                        latitude,
                        longitude,
                        10000 // Geofences radius in meter
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(5000) //Expiration in milliseconds

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build());
        Log.i(TAG, address + ":" + latitude + "," + longitude);
        Log.i(TAG, "LIST SIZE: " + mGeofenceList.size());

    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
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

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
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
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(TodoActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                performPendingGeofenceTask();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
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

    /**
     * Stores whether geofences were added ore removed in {@link SharedPreferences};
     *
     * @param added Whether geofences were added or removed.
     */
    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                GEOFENCES_ADDED_KEY, false);
    }

    @OnClick(R.id.logout)
    public void logout(){
        Util.userLogout(this);
    }

}
