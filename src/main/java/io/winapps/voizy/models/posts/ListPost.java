package io.winapps.voizy.models.posts;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ListPost {
    @JsonProperty("postID")
    private long postId;

    @JsonProperty("userID")
    private long userId;

    @JsonProperty("toUserID")
    private long toUserId;

    @JsonProperty("originalPostID")
    private Long originalPostId;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("preferredName")
    private String preferredName;

    @JsonProperty("username")
    private String username;

    @JsonProperty("impressions")
    private long impressions;

    @JsonProperty("views")
    private long views;

    @JsonProperty("contentText")
    private String contentText;

    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonProperty("locationName")
    private String locationName;

    @JsonProperty("locationLat")
    private Double locationLat;

    @JsonProperty("locationLong")
    private Double locationLong;

    @JsonProperty("isPoll")
    private Boolean isPoll;

    @JsonProperty("pollQuestion")
    private String pollQuestion;

    @JsonProperty("pollDurationType")
    private String pollDurationType;

    @JsonProperty("pollDurationLength")
    private Long pollDurationLength;

    @JsonProperty("userReaction")
    private String userReaction;

    @JsonProperty("totalReactions")
    private long totalReactions;

    @JsonProperty("totalComments")
    private long totalComments;

    @JsonProperty("totalPostShares")
    private long totalPostShares;

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getImpressions() {
        return impressions;
    }

    public void setImpressions(long impressions) {
        this.impressions = impressions;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(Double locationLat) {
        this.locationLat = locationLat;
    }

    public Double getLocationLong() {
        return locationLong;
    }

    public void setLocationLong(Double locationLong) {
        this.locationLong = locationLong;
    }

    public Boolean getIsPoll() {
        return isPoll;
    }

    public void setIsPoll(Boolean isPoll) {
        this.isPoll = isPoll;
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

    public Long getPollDurationLength() {
        return pollDurationLength;
    }

    public void setPollDurationLength(Long pollDurationLength) {
        this.pollDurationLength = pollDurationLength;
    }

    public String getUserReaction() {
        return userReaction;
    }

    public void setUserReaction(String userReaction) {
        this.userReaction = userReaction;
    }

    public long getTotalReactions() {
        return totalReactions;
    }

    public void setTotalReactions(long totalReactions) {
        this.totalReactions = totalReactions;
    }

    public long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(long totalComments) {
        this.totalComments = totalComments;
    }

    public long getTotalPostShares() {
        return totalPostShares;
    }

    public void setTotalPostShares(long totalPostShares) {
        this.totalPostShares = totalPostShares;
    }
}
