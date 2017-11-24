package hu.webandmore.todo.ui.todo;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hu.webandmore.todo.R;
import hu.webandmore.todo.api.model.Todo;
import hu.webandmore.todo.ui.Presenter;

public class TodoPresenter extends Presenter<TodoScreen> {

    private Context context;
    private Paint p = new Paint();

    private DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    private static String userTodoRef;

    TodoPresenter(Context context) {
        this.context = context;
        userTodoRef = "todos_" + user.getUid();
    }

    private void deleteTodo(Todo todo) {
        mDatabaseRef.child(userTodoRef).child(todo.getCategory()).child(todo.getId()).removeValue();
        System.out.println("DELETE REF: " + mDatabaseRef.child(userTodoRef).child(todo.getCategory()).child(todo.getId()).toString());
    }

    public void showDeletePopup(final Todo todo, final int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.are_you_sure_delete);
        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deleteTodo(todo);
                screen.removeTodo(todo, position);
            }
        });

        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}
