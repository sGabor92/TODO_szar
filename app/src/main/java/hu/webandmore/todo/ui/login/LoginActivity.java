package hu.webandmore.todo.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.webandmore.todo.MainActivity;
import hu.webandmore.todo.R;
import hu.webandmore.todo.ui.register.RegisterActivity;

public class LoginActivity extends AppCompatActivity implements LoginScreen {

    private static String TAG = "LoginActivity";

    @BindView(R.id.email)
    EditText mEmail;

    @BindView(R.id.password)
    EditText mPassword;

    LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        loginPresenter = new LoginPresenter(this);

        loginPresenter.hasLogin(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        loginPresenter.attachScreen(this);
        loginPresenter.attachListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        loginPresenter.detachListener();
        loginPresenter.detachScreen();

    }

    @OnClick(R.id.login)
    public void login() {
        mPassword.setError(null);
        mEmail.setError(null);

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!loginPresenter.isPasswordValid(password)) {
            mPassword.setError(getString(R.string.password_too_short), null);
            focusView = mPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.invalid_email_address), null);
            focusView = mEmail;
            cancel = true;
        } else if (!loginPresenter.isEmailValid(email)) {
            mEmail.setError(getString(R.string.invalid_email_address), null);
            focusView = mEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loginPresenter.signIn(this, email, password);
        }
    }

    @OnClick(R.id.signUp)
    public void switchToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void showError(String errorMsg) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void afterLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
