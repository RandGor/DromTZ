package ru.randgor.dromtz.helpers;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.randgor.dromtz.R;
import ru.randgor.dromtz.data.Repository;
import ru.randgor.dromtz.data.ElementRepoList;
import ru.randgor.dromtz.databinding.RecyclerRepoRowBinding;
import ru.randgor.dromtz.databinding.RecyclerRetryBinding;

public class RepoListAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private final ArrayList<ElementRepoList> rows;

    private final OnItemClickListener onItemClickListener;

    private final Context appContext;

    public static final String ENTITY_NAME = "repository";

    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_NORMAL = 1;
    public static final int VIEW_TYPE_RETRY = 2;

    public boolean isLoaderVisible = false;
    public boolean isRetryVisible = false;
    public int retryStatus = 200;

    private int loaderIndex;


    public RepoListAdapter(ArrayList<ElementRepoList> rows, OnItemClickListener onItemClickListener, Context context) {
        this.rows = rows;
        this.onItemClickListener = onItemClickListener;
        this.appContext = context;
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(inflater.inflate(R.layout.recycler_loading, parent, false));
            case VIEW_TYPE_NORMAL:
                RecyclerRepoRowBinding repoBinding = RecyclerRepoRowBinding.inflate(inflater, parent, false);
                return new SimpleViewHolder(repoBinding);
            case VIEW_TYPE_RETRY:
                RecyclerRetryBinding retryBinding = RecyclerRetryBinding.inflate(inflater, parent, false);
                return new RetryHolder(retryBinding);
            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        viewHolder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).getType();
    }

    public void clear() {
        rows.clear();
        notifyDataSetChanged();
    }

    public ElementRepoList getItem(int position) {
        return rows.get(position);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public void addLoading() {
        if (isLoaderVisible)
            return;
        isLoaderVisible = true;
        rows.add(new ElementRepoList());
        loaderIndex = rows.size() - 1;
        notifyItemInserted(loaderIndex);
    }

    public void changeLoadingToRetry(int status) {
        if (!isLoaderVisible || isRetryVisible)
            return;
        retryStatus = status;

        isLoaderVisible = false;
        isRetryVisible = true;

        if (rows.isEmpty())
            return;

        rows.get(loaderIndex).setType(VIEW_TYPE_RETRY);
        notifyItemChanged(loaderIndex);
    }

    public void changeRetryToLoading() {
        if (isLoaderVisible || !isRetryVisible)
            return;

        isLoaderVisible = true;
        isRetryVisible = false;

        if (rows.isEmpty())
            return;

        rows.get(loaderIndex).setType(VIEW_TYPE_LOADING);
        notifyItemChanged(loaderIndex);
    }

    public void removeLoading() {
        if (!isLoaderVisible)
            return;
        isLoaderVisible = false;
        ElementRepoList item = getItem(loaderIndex);
        if (item != null) {
            rows.remove(loaderIndex);
            notifyItemRemoved(loaderIndex);
        }
    }

    class SimpleViewHolder extends BaseViewHolder implements RecyclerView.OnClickListener {
        RecyclerRepoRowBinding binding;
        OnItemClickListener monItemClickListener;

        SimpleViewHolder(RecyclerRepoRowBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
            monItemClickListener = onItemClickListener;

            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            monItemClickListener.onItemClicked(getAdapterPosition());
        }

        @Override
        public void onBind(int position) {
            super.onBind(position);

            ElementRepoList row = rows.get(position);

            Repository repository = row.getRepository();

            if (repository == null) {
                Log.e("repair", "Not yet implemented");
                return;
            }

            Picasso.with(appContext).load(repository.owner.avatarUrl).fit().centerInside().into(binding.recyclerPhoto);
            binding.recyclerOwner.setText(repository.owner.login);
            binding.recyclerHeader.setText(repository.fullName);
            binding.recyclerDescription.setText(repository.description);
        }
    }

    class ProgressHolder extends BaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
        }
    }

    class RetryHolder extends BaseViewHolder implements RecyclerView.OnClickListener {
        RecyclerRetryBinding binding;
        OnItemClickListener monItemClickListener;

        RetryHolder(RecyclerRetryBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
            monItemClickListener = onItemClickListener;

            binding.getRoot().setOnClickListener(this);
            binding.reloadButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            monItemClickListener.onItemClicked(getAdapterPosition());
        }

        @Override
        public void onBind(int position) {
            super.onBind(position);

            String message = appContext.getString(R.string.no_internet);

            if (retryStatus == 0)
                message = appContext.getString(R.string.not_found_repo);
            else if (retryStatus == 403 || retryStatus == 422)
                message = appContext.getString(R.string.api_limitations);
            else if (retryStatus / 100 == 4)
                message = appContext.getString(R.string.query_error);
            else if (retryStatus / 100 == 5)
                message = appContext.getString(R.string.server_error);

            binding.reloadMessage.setText(message);
        }
    }
}