package hu.webandmore.todo.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hu.webandmore.todo.R;
import hu.webandmore.todo.ui.login.LoginActivity;

public class RegisterActivity extends AppCompatActivity implements RegisterScreen {

    RegisterPresenter registerPresenter;

    @BindView(R.id.email)
    EditText mEmail;
    @BindView(R.id.password)
    EditText mPassword;
    @BindView(R.id.password_confirmation)
    EditText mPasswordConfirmation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerPresenter = new RegisterPresenter(this);

        ButterKnife.bind(this);

    }

    @OnClick(R.id.signIn)
    public void switchToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.register)
    public void register() {
        mPassword.setError(null);
        mPasswordConfirmation.setError(null);
        mEmail.setError(null);

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String passwordConfirmation = mPasswordConfirmation.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!registerPresenter.isPasswordValid(password)) {
            mPassword.setError(getString(R.string.password_too_short), null);
            focusView = mPassword;
            cancel = true;
        }

        if(!registerPresenter.isPasswordConfirmed(password, passwordConfirmation)){
            mPasswordConfirmation.setError(getString(R.string.passwords_do_not_match), null);
            focusView = mPasswordConfirmation;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.invalid_email_address), null);
            focusView = mEmail;
            cancel = true;
        } else if (!registerPresenter.isEmailValid(email)) {
            mEmail.setError(getString(R.string.invalid_email_address), null);
            focusView = mEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            registerPresenter.signUp(this, email, password);
        }
    }

    @Override
    public void showError(String errorMsg) {

    }
}
