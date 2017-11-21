package hu.webandmore.todo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import hu.webandmore.todo.R;
import hu.webandmore.todo.api.model.Priority;
import hu.webandmore.todo.api.model.Todo;
import hu.webandmore.todo.ui.todo.CreateTodoActivity;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class TodoSectionsAdapter extends StatelessSection {

    private Context context;
    private String categoryTitle;
    private ArrayList<Todo> todos = new ArrayList<>();
    private SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter;

    public TodoSectionsAdapter(Context context,
                               SectionedRecyclerViewAdapter sectionedRecyclerViewAdapter,
                               String categoryTitle, ArrayList<Todo> todos) {
        super(new SectionParameters.Builder(R.layout.list_item_todo)
                .headerResourceId(R.layout.category_header)
                .build());

        this.context = context;
        this.sectionedRecyclerViewAdapter = sectionedRecyclerViewAdapter;
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
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

        String todoName = todos.get(position).getName();
        Priority priority = todos.get(position).getPriority();
        String description = todos.get(position).getDescription();
        String deadline = todos.get(position).getDeadlineString();
        if (todos.get(position).getLocation() != null) {
            String location = todos.get(position).getLocation().getAddress();
            itemViewHolder.mTodoLocation.setText(location);
        } else {
            itemViewHolder.mTodoLocation.setVisibility(View.GONE);
        }

        itemViewHolder.mTodoName.setText(todoName);
        itemViewHolder.mTodoDetails.setText(description);
        if (todos.get(position).getDeadline() != 0) {
            itemViewHolder.mTodoDeadline.setText(deadline);
        } else {
            itemViewHolder.mTodoDeadline.setVisibility(View.GONE);
        }

        if (priority == Priority.HIGH) {
            itemViewHolder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_high));
        } else if (priority == Priority.MEDIUM) {
            itemViewHolder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_medium));
        } else {
            itemViewHolder.mTodoBackground.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_priority_low));
        }

        itemViewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicking on item");
                if (todos.get(position).isExpanded()) {
                    itemViewHolder.mTodoDetailsLayout.setVisibility(View.GONE);
                    todos.get(position).setExpanded(false);
                } else {
                    itemViewHolder.mTodoDetailsLayout.setVisibility(View.VISIBLE);
                    todos.get(position).setExpanded(true);
                }

                sectionedRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        itemViewHolder.mEditTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicking on edit!");
                Intent intent = new Intent(context, CreateTodoActivity.class);
                intent.putExtra("isEdited", true);
                intent.putExtra("id", todos.get(position).getId());
                context.startActivity(intent);
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
        private final LinearLayout mTodoDetailsLayout;
        private final TextView mTodoDetails;
        private final TextView mTodoDeadline;
        private final TextView mTodoLocation;
        private final ImageButton mEditTodo;

        ItemViewHolder(View itemView) {
            super(itemView);

            rootView = itemView;
            mTodoName = (TextView) itemView.findViewById(R.id.todoName);
            mTodoBackground = (LinearLayout) itemView.findViewById(R.id.todoBackground);
            mTodoDetailsLayout = (LinearLayout) itemView.findViewById(R.id.detailsLayout);
            mTodoDetails = (TextView) itemView.findViewById(R.id.todoDescription);
            mTodoDeadline = (TextView) itemView.findViewById(R.id.todoDeadline);
            mTodoLocation = (TextView) itemView.findViewById(R.id.todoLocation);
            mEditTodo = (ImageButton) itemView.findViewById(R.id.editTodo);
        }
    }
}
