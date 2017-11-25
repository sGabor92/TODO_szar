package hu.webandmore.todo.utils;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.atomic.AtomicInteger;

import hu.webandmore.todo.ui.login.LoginActivity;

public class Util {

    private static FirebaseDatabase mDatabase;
    private final static AtomicInteger c = new AtomicInteger(0);

    public static void userLogout(Context context) {
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("logout", true);
        context.startActivity(intent);
    }

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public static int getID() {
        return c.incrementAndGet();
    }

}
