package hu.webandmore.todo.ui.register;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterPresenter {
    private static String TAG = "RegisterPresenter";

    private FirebaseAuth mAuth;

    private Activity activity;

    RegisterPresenter(Activity activity){
        mAuth = FirebaseAuth.getInstance();
        this.activity = activity;
    }

    void signUp(final RegisterScreen screen, String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            if(task.getException().getMessage() != null) {
                                screen.showError("Error while signing up" + task.getException().getMessage());
                            }
                        }else{
                            //Successful registration -> GOTO: main activity
                        }
                    }
                });

    }

    boolean isEmailValid(String email) {
        return email.contains("@");
    }

    boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    boolean isPasswordConfirmed(String password1, String password2){
        return password1.equals(password2);
    }

}
