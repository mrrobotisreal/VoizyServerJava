package io.winapps.voizy.models.posts;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreatePostRequest {
    @JsonProperty("userID")
    private long userId;

    @JsonProperty("toUserID")
    private long toUserId;

    @JsonProperty("originalPostID")
    private Long originalPostId;

    @JsonProperty("contentText")
    private String contentText;

    @JsonProperty("locationName")
    private String locationName;

    @JsonProperty("locationLat")
    private double locationLat;

    @JsonProperty("locationLong")
    private double locationLong;

    @JsonProperty("images")
    private List<String> images;

    @JsonProperty("hashtags")
    private List<String> hashtags;

    @JsonProperty("isPoll")
    private boolean isPoll;

    @JsonProperty("pollQuestion")
    private String pollQuestion;

    @JsonProperty("pollDurationType")
    private String pollDurationType;

    @JsonProperty("pollDurationLength")
    private long pollDurationLength;

    @JsonProperty("pollOptions")
    private List<String> pollOptions;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getToUserId() {
        return toUserId;
    }

    public void setToUserId(long toUserId) {
        this.toUserId = toUserId;
    }

    public Long getOriginalPostId() {
        return originalPostId;
    }

    public void setOriginalPostId(Long originalPostId) {
        this.originalPostId = originalPostId;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public double getLocationLong() {
        return locationLong;
    }

    public void setLocationLong(double locationLong) {
        this.locationLong = locationLong;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    public boolean isPoll() {
        return isPoll;
    }

    public void setPoll(boolean poll) {
        isPoll = poll;
    }

    public String getPollQuestion() {
        return pollQuestion;
    }

    public void setPollQuestion(String pollQuestion) {
        this.pollQuestion = pollQuestion;
    }

    public String getPollDurationType() {
        return pollDurationType;
    }

    public void setPollDurationType(String pollDurationType) {
        this.pollDurationType = pollDurationType;
    }

    public long getPollDurationLength() {
        return pollDurationLength;
    }

    public void setPollDurationLength(long pollDurationLength) {
        this.pollDurationLength = pollDurationLength;
    }

    public List<String> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(List<String> pollOptions) {
        this.pollOptions = pollOptions;
    }
}
