package hu.webandmore.todo.ui.login;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hu.webandmore.todo.ui.Presenter;

public class LoginPresenter extends Presenter<LoginScreen> {

    private static String TAG = "LoginPresenter";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Activity activity;

    LoginPresenter(Activity activity){
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
    }

    void hasLogin(final LoginScreen screen){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    screen.afterLogin();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    void signIn(final LoginScreen screen, String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            if(task.getException().getMessage() != null) {
                                screen.showError("Error during login: "
                                        + task.getException().getMessage());
                            }
                        } else {
                            screen.afterLogin();
                        }
                    }
                });

    }

    void attachListener(){
        mAuth.addAuthStateListener(mAuthListener);
    }

    void detachListener(){
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    boolean isEmailValid(String email) {
        return email.contains("@");
    }

    boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

}
