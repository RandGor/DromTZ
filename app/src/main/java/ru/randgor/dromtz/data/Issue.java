package ru.randgor.dromtz.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Issue {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("node_id")
    @Expose
    public String nodeId;
    @SerializedName("html_url")
    @Expose
    public String htmlUrl;
    @SerializedName("url")
    @Expose
    public String url;
    @SerializedName("events_url")
    @Expose
    public String eventsUrl;
    @SerializedName("comments_url")
    @Expose
    public String commentsUrl;
    @SerializedName("labels_url")
    @Expose
    public String labelsUrl;
    @SerializedName("created_at")
    @Expose
    public String createdAt;
    @SerializedName("updated_at")
    @Expose
    public String updatedAt;
    @SerializedName("score")
    @Expose
    public Integer score;
    @SerializedName("repository_url")
    @Expose
    public String repositoryUrl;
    @SerializedName("number")
    @Expose
    public Integer number;
    @SerializedName("title")
    @Expose
    public String title;
    @SerializedName("user")
    @Expose
    public User user;
    @SerializedName("labels")
    @Expose
    public List<Object> labels = null;
    @SerializedName("state")
    @Expose
    public String state;
    @SerializedName("locked")
    @Expose
    public Boolean locked;
    @SerializedName("assignee")
    @Expose
    public Object assignee;
    @SerializedName("assignees")
    @Expose
    public List<Object> assignees = null;
    @SerializedName("milestone")
    @Expose
    public Object milestone;
    @SerializedName("comments")
    @Expose
    public Integer comments;
    @SerializedName("closed_at")
    @Expose
    public Object closedAt;
    @SerializedName("author_association")
    @Expose
    public String authorAssociation;
    @SerializedName("active_lock_reason")
    @Expose
    public Object activeLockReason;
    @SerializedName("body")
    @Expose
    public String body;
    @SerializedName("performed_via_github_app")
    @Expose
    public Object performedViaGithubApp;

}

