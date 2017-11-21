package hu.webandmore.todo.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import hu.webandmore.todo.R;
import hu.webandmore.todo.api.model.Priority;
import hu.webandmore.todo.api.model.Todo;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<Todo> todos = new ArrayList<>();

    public TodoAdapter(Context context, ArrayList<Todo> todos) {
        this.context = context;
        this.todos = todos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.todo = todos.get(position);

        holder.mTodoName.setText(holder.todo.getName());

        Log.i("PRIORITY", holder.todo.getName());
        Log.i("PRIORITY", holder.todo.getPriority().toString());

        if(holder.todo.getPriority() == Priority.HIGH) {
            holder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_high));
        } else if(holder.todo.getPriority() == Priority.MEDIUM) {
            holder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_medium));
        } else if(holder.todo.getPriority() == Priority.MEDIUM) {
            holder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_low));
        }
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(todos.get(position).getId());
    }

    public Todo getItem(int position) {
        return todos.get(position);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTodoName;
        LinearLayout mTodoBackground;
        public Todo todo;

        ViewHolder(View itemView) {
            super(itemView);
            mTodoName = (TextView) itemView.findViewById(R.id.todoName);
            mTodoBackground = (LinearLayout) itemView.findViewById(R.id.todoBackground);

        }
    }

}
