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
import ru.randgor.dromtz.data.ElementIssuesList;
import ru.randgor.dromtz.data.Issue;
import ru.randgor.dromtz.data.Repository;
import ru.randgor.dromtz.databinding.RecyclerIssueRowBinding;
import ru.randgor.dromtz.databinding.RecyclerRepoRowBinding;
import ru.randgor.dromtz.databinding.RecyclerRetryBinding;

public class IssuesListAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private final ArrayList<ElementIssuesList> rows;

    private final OnItemClickListener onItemClickListener;

    private final Context appContext;

    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_HEAD = 1;
    public static final int VIEW_TYPE_NORMAL = 2;
    public static final int VIEW_TYPE_RETRY = 3;

    public boolean isLoaderVisible = false;
    public boolean isRetryVisible = false;
    public int retryStatus = 200;

    private int loaderIndex;


    public IssuesListAdapter(ArrayList<ElementIssuesList> rows, OnItemClickListener onItemClickListener, Context context) {
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
            case VIEW_TYPE_HEAD:
                RecyclerRepoRowBinding repoBinding = RecyclerRepoRowBinding.inflate(inflater, parent, false);
                return new HeadViewHolder(repoBinding);
            case VIEW_TYPE_NORMAL:
                RecyclerIssueRowBinding issueBinding = RecyclerIssueRowBinding.inflate(inflater, parent, false);
                return new SimpleViewHolder(issueBinding);
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

    public ElementIssuesList getItem(int position) {
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
        rows.add(new ElementIssuesList());
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
        ElementIssuesList item = getItem(loaderIndex);
        if (item != null) {
            rows.remove(loaderIndex);
            notifyItemRemoved(loaderIndex);
        }
    }

    class HeadViewHolder extends BaseViewHolder implements RecyclerView.OnClickListener {
        RecyclerRepoRowBinding binding;
        OnItemClickListener monItemClickListener;

        HeadViewHolder(RecyclerRepoRowBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
            monItemClickListener = onItemClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            monItemClickListener.onItemClicked(getAdapterPosition());
        }

        @Override
        public void onBind(int position) {
            super.onBind(position);

            ElementIssuesList row = rows.get(position);

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

    class SimpleViewHolder extends BaseViewHolder implements RecyclerView.OnClickListener {
        RecyclerIssueRowBinding binding;
        OnItemClickListener monItemClickListener;

        SimpleViewHolder(RecyclerIssueRowBinding binding) {
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

            ElementIssuesList row = rows.get(position);

            Issue issue = row.getIssue();

            if (issue == null) {
                Log.e("repair", "Not yet implemented");
                return;
            }

            binding.recyclerPhoto.setImageResource(issue.state.equals("closed")? R.drawable.ic_baseline_radio_button_24: R.drawable.ic_baseline_radio_button_checked_24);
            binding.recyclerHeader.setText("Issue #" + issue.number + " by " + issue.user.login);
            binding.recyclerDescription.setText(issue.title);
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
                message = appContext.getString(R.string.not_found_issue);
            else if (retryStatus == 403)
                message = appContext.getString(R.string.api_limitations);
            else if (retryStatus == 422)
                message = appContext.getString(R.string.no_repo_access);
            else if (retryStatus / 100 == 4)
                message = appContext.getString(R.string.query_error);
            else if (retryStatus / 100 == 5)
                message = appContext.getString(R.string.server_error);

            binding.reloadMessage.setText(message);
        }
    }
}