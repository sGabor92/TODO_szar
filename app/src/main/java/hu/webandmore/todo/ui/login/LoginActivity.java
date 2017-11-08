package hu.webandmore.todo.ui.login;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import hu.webandmore.todo.R;

public class LoginActivity extends AppCompatActivity implements LoginScreen{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    @Override
    public void checkLogin() {
        
    }

    @Override
    public void setEmail(String email) {

    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void setPassword(String password) {

    }

    @Override
    public void attemptLogin() {

    }

    @Override
    public void showError(String errorMsg) {

    }

    @Override
    public void userLoggedIn(String token, boolean userHasDevice) {

    }
}
