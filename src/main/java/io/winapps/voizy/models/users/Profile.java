package io.winapps.voizy.models.users;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Profile {
    @JsonProperty("userID")
    private long userID;

    @JsonProperty("profileID")
    private long profileID;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("preferredName")
    private String preferredName;

    @JsonProperty("birthDate")
    @JsonFormat(pattern = "MMM dd, yyyy")
    private LocalDateTime birthDate;

    @JsonProperty("cityOfResidence")
    private String cityOfResidence;

    @JsonProperty("placeOfWork")
    private String placeOfWork;

    @JsonProperty("dateJoined")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateJoined;

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getProfileID() { return profileID; }

    public void setProfileID(long profileID) { this.profileID = profileID; }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPreferredName() { return preferredName; }

    public void setPreferredName(String preferredName) { this.preferredName = preferredName; }

    public LocalDateTime getBirthDate() { return birthDate; }

    public void setBirthDate(LocalDateTime birthDate) { this.birthDate = birthDate; }

    public String getCityOfResidence() { return cityOfResidence; }

    public void setCityOfResidence(String cityOfResidence) { this.cityOfResidence = cityOfResidence; }

    public String getPlaceOfWork() { return placeOfWork; }

    public void setPlaceOfWork(String placeOfWork) { this.placeOfWork = placeOfWork; }

    public LocalDateTime getDateJoined() { return dateJoined; }

    public void setDateJoined(LocalDateTime dateJoined) { this.dateJoined = dateJoined; }
}
