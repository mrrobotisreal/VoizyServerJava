package io.winapps.voizy.models.posts;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetPostMediaResponse {
    @JsonProperty("images")
    private List<String> images;

    @JsonProperty("videos")
    private List<String> videos;

    // Getters and setters
    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }
}
