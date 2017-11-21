package hu.webandmore.todo.ui.todo;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

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

    void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT) {
                    /*AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle(R.string.are_you_sure_delete);
                    alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            /*Todo todo = screen.getItem(position);
                            System.out.println("TODO ID: " + todo.getId());
                            //deleteTodo(todo, position);*/
                        /*}
                    });*/

                    /*alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //screen.restoreTodo(position);
                        }
                    });
                    alert.show();*/
                    /*System.out.println("POZ: " + position);
                    Todo todo = screen.getItem(position);
                    System.out.println("TODO ID: " + todo.getId());*/
                }
            }


            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_delete_accent);
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;
                    Paint p2 = new Paint();
                    p2.setTextSize(60);
                    p2.setTextAlign(Paint.Align.CENTER);
                    p2.setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.colorAccent));

                    if (dX < 0) {
                        p.setColor(ContextCompat.getColor(context.getApplicationContext(), R.color.colorWhite));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                        c.drawRect(background, p);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                        if(icon != null) {
                            c.drawBitmap(icon, null, icon_dest, p);
                        }
                        c.drawText(context.getString(R.string.delete), (float) itemView.getRight() - 100, (float) itemView.getTop() + width + 60, p2);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(screen.getTodosRecyclerView());
    }

    private void deleteTodo(Todo todo, int position) {
        mDatabaseRef.child(userTodoRef).child(todo.getCategory()).child(todo.getId()).removeValue();
        screen.removeTodo(position);
        System.out.println("DELETE REF: " + mDatabaseRef.child(userTodoRef).child(todo.getCategory()).child(todo.getId()).toString());
    }
}
