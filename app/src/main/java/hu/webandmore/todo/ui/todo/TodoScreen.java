package hu.webandmore.todo.ui.todo;

import hu.webandmore.todo.api.model.Todo;

public interface TodoScreen {
    void removeTodo(Todo todo, int position);
}
