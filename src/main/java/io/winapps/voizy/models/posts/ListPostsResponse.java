package io.winapps.voizy.models.posts;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListPostsResponse {
    @JsonProperty("posts")
    private List<ListPost> posts;

    @JsonProperty("limit")
    private long limit;

    @JsonProperty("page")
    private long page;

    @JsonProperty("totalPosts")
    private long totalPosts;

    @JsonProperty("totalPages")
    private long totalPages;

    public List<ListPost> getPosts() {
        return posts;
    }

    public void setPosts(List<ListPost> posts) {
        this.posts = posts;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }
}
