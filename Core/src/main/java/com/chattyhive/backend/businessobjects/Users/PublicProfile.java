package com.chattyhive.backend.businessobjects.Users;

import com.chattyhive.backend.contentprovider.formats.BASIC_PUBLIC_PROFILE;
import com.chattyhive.backend.contentprovider.formats.Format;
import com.chattyhive.backend.contentprovider.formats.PUBLIC_PROFILE;

/**
 * Created by Jonathan on 08/07/2014.
 */
public class PublicProfile extends Profile {

    String publicName;
    String color;

    Boolean showSex;
    Boolean showAge;
    Boolean showLocation;

    public String getPublicName() {
        return this.publicName;
    }
    public void setPublicName(String value) {
        this.publicName = value;
    }

    public String getColor() {
        if (this.color == null)
            this.color = "#808080";
        return this.color;
    }
    public void setColor(String value) {
        this.color = value;
    }

    public Boolean getShowSex() {
        return (this.showSex!=null)?this.showSex:false;
    }
    public void setShowSex(Boolean value) {
        this.showSex = value;
    }

    public Boolean getShowAge() {
        return (this.showAge!=null)?this.showAge:false;
    }
    public void setShowAge(Boolean value) {
        this.showAge = value;
    }

    public Boolean getShowLocation() {
        return (this.showLocation!=null)?this.showLocation:false;
    }
    public void setShowLocation(Boolean value) {
        this.showLocation = value;
    }

    public PublicProfile(Format format) {
        this();
        if (!this.fromFormat(format)) {
            throw new IllegalArgumentException("Format not valid.");
        }
    }

    public PublicProfile(){
        this.loadedProfileLevel = ProfileLevel.None;
    }

    @Override
    public void unloadProfile(ProfileLevel profileLevel) {
        if (profileLevel.ordinal() >= this.loadedProfileLevel.ordinal()) return;

        if (profileLevel.ordinal() <= ProfileLevel.Extended.ordinal()) {

        }

        if (profileLevel.ordinal() <= ProfileLevel.Basic.ordinal()) {
            this.showSex = null;
            this.showAge = null;
            this.showLocation = null;
            this.language = null;
            this.location = null;
            this.birthdate = null;
            this.sex = null;
        }

        if (profileLevel.ordinal() <= ProfileLevel.None.ordinal()) {
            this.publicName = null;
            this.setImageURL(null);
            this.statusMessage = null;
            this.color = null;
        }
    }

    @Override
    public String getShowingName() {
        return this.publicName;
    }

    @Override
    public Format toFormat(Format format) {
        if (format instanceof PUBLIC_PROFILE) {
            ((PUBLIC_PROFILE) format).USER_ID = this.userID;
            ((PUBLIC_PROFILE) format).BIRTHDATE = this.birthdate;
            ((PUBLIC_PROFILE) format).LOCATION = this.location;
            ((PUBLIC_PROFILE) format).LANGUAGE = this.language;
            ((PUBLIC_PROFILE) format).PUBLIC_SHOW_AGE = this.showAge;
            ((PUBLIC_PROFILE) format).PUBLIC_SHOW_LOCATION = this.showLocation;
            ((PUBLIC_PROFILE) format).PUBLIC_SHOW_SEX = this.showSex;
            ((PUBLIC_PROFILE) format).SEX = this.sex;
            return format;
        } else if (format instanceof BASIC_PUBLIC_PROFILE) {
            ((BASIC_PUBLIC_PROFILE) format).USER_ID = this.userID;
            ((BASIC_PUBLIC_PROFILE) format).PUBLIC_NAME = this.publicName;
            ((BASIC_PUBLIC_PROFILE) format).IMAGE_URL = this.imageURL;
            ((BASIC_PUBLIC_PROFILE) format).USER_COLOR = this.color;
            ((BASIC_PUBLIC_PROFILE) format).STATUS_MESSAGE = this.statusMessage;
            return format;
        }

        throw new IllegalArgumentException("Expected PUBLIC_PROFILE or BASIC_PUBLIC_PROFILE format");
    }

    @Override
    public Boolean fromFormat(Format format) {
        if (format instanceof PUBLIC_PROFILE) {
            this.userID = ((PUBLIC_PROFILE) format).USER_ID;
            this.birthdate = ((PUBLIC_PROFILE) format).BIRTHDATE;
            this.language = ((PUBLIC_PROFILE) format).LANGUAGE;
            this.location = ((PUBLIC_PROFILE) format).LOCATION;
            this.showSex = ((PUBLIC_PROFILE) format).PUBLIC_SHOW_SEX;
            this.showAge = ((PUBLIC_PROFILE) format).PUBLIC_SHOW_AGE;
            this.showLocation = ((PUBLIC_PROFILE) format).PUBLIC_SHOW_LOCATION;
            this.sex = ((PUBLIC_PROFILE) format).SEX;
            if (this.loadedProfileLevel.ordinal() < ProfileLevel.Extended.ordinal())
                this.loadedProfileLevel = ProfileLevel.Extended;
            return true;
        } else if (format instanceof BASIC_PUBLIC_PROFILE) {
            this.userID = ((BASIC_PUBLIC_PROFILE) format).USER_ID;
            this.publicName = ((BASIC_PUBLIC_PROFILE) format).PUBLIC_NAME;
            this.color = ((BASIC_PUBLIC_PROFILE) format).USER_COLOR;
            this.setImageURL(((BASIC_PUBLIC_PROFILE) format).IMAGE_URL);
            this.statusMessage = ((BASIC_PUBLIC_PROFILE) format).STATUS_MESSAGE;
            if (this.loadedProfileLevel.ordinal() < ProfileLevel.Basic.ordinal())
                this.loadedProfileLevel = ProfileLevel.Basic;
            return true;
        }
        return false;
    }
}
