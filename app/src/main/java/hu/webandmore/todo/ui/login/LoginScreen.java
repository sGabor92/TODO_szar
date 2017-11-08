package hu.webandmore.todo.ui.login;

interface LoginScreen {

    void checkLogin();
    void setEmail(String email);
    String getEmail();
    String getPassword();
    void setPassword(String password);
    void attemptLogin();
    void showError(String errorMsg);
    void userLoggedIn(String token, boolean userHasDevice);

}
