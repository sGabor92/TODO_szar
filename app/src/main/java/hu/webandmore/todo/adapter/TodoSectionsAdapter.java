package hu.webandmore.todo.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import hu.webandmore.todo.R;
import hu.webandmore.todo.api.model.Priority;
import hu.webandmore.todo.api.model.Todo;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class TodoSectionsAdapter extends StatelessSection {

    private Context context;
    private String categoryTitle;
    private ArrayList<Todo> todos = new ArrayList<>();

    public TodoSectionsAdapter(Context context, String categoryTitle, ArrayList<Todo> todos) {
        super(new SectionParameters.Builder(R.layout.list_item_todo)
        .headerResourceId(R.layout.category_header)
        .build());

        this.context = context;
        this.categoryTitle = categoryTitle;
        this.todos = todos;

    }

    @Override
    public int getContentItemsTotal() {
        return todos.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

        String todoName = todos.get(position).getName();
        Priority priority = todos.get(position).getPriority();

        itemViewHolder.mTodoName.setText(todoName);

        if(priority == Priority.HIGH) {
            itemViewHolder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_high));
        } else if(priority == Priority.MEDIUM) {
            itemViewHolder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_medium));
        } else {
            itemViewHolder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_low));
        }

        itemViewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicking on item");
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.mCategoryTitle.setText(categoryTitle);
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView mCategoryTitle;

        HeaderViewHolder(View view) {
            super(view);

            mCategoryTitle = (TextView) view.findViewById(R.id.categoryHeader);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView mTodoName;
        private final LinearLayout mTodoBackground;

        ItemViewHolder(View itemView) {
            super(itemView);

            rootView = itemView;
            mTodoName = (TextView) itemView.findViewById(R.id.todoName);
            mTodoBackground = (LinearLayout) itemView.findViewById(R.id.todoBackground);
        }
    }
}
