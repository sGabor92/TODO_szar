package hu.webandmore.todo.ui.todo;

import android.support.v7.widget.RecyclerView;

import hu.webandmore.todo.api.model.Todo;

public interface TodoScreen {
    RecyclerView getTodosRecyclerView();
    void removeTodo(int position);
    void restoreTodo(int position);
    Todo getItem(int position);
}
