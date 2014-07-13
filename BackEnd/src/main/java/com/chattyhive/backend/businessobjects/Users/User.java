package com.chattyhive.backend.businessobjects.Users;

import com.chattyhive.backend.contentprovider.OSStorageProvider.UserLocalStorageInterface;
import com.chattyhive.backend.contentprovider.formats.Format;
import com.chattyhive.backend.contentprovider.formats.LOCAL_USER_PROFILE;
import com.chattyhive.backend.contentprovider.formats.PRIVATE_PROFILE;
import com.chattyhive.backend.contentprovider.formats.PROFILE_ID;
import com.chattyhive.backend.contentprovider.formats.PUBLIC_PROFILE;
import com.chattyhive.backend.util.events.FormatReceivedEventArgs;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Jonathan on 11/12/13.
 * Represents a user.
 */
public class User {

    /***********************************/
    /*     STATIC USER MANAGEMENT      */
    /***********************************/
    private static UserLocalStorageInterface userLocalStorage;

    private static TreeMap<String,User> knownUsers;
    private static User me;

    public void Initialize(UserLocalStorageInterface userLocalStorage) {
        User.userLocalStorage = userLocalStorage;
        User.knownUsers = new TreeMap<String, User>();

        //Load local stored users.
        String[] users = userLocalStorage.RecoverAllCompleteUserProfiles();
        for(String user : users) {
            Format[] formats = Format.getFormat((new JsonParser()).parse(user));
            for (Format format : formats) {
                User u = new User(format);

                u.unloadProfile(); //There is no need to keep in memory complete user profiles.

                User.knownUsers.put(u.getUserID(),u);
            }
        }

        String localUser = userLocalStorage.RecoverLocalUserProfile();
        Format[] formats = Format.getFormat((new JsonParser()).parse(localUser));
        for (Format format : formats) {
            me = new User(format);
        }
    }

    public static User getUser(String userID) {
        if (User.knownUsers == null) throw new IllegalStateException("Users must be initialized.");
        else if (userID == null) throw new NullPointerException("UserID must not be null.");
        else if (userID.isEmpty()) throw new IllegalArgumentException("UserID must not be empty.");

        if (User.knownUsers.containsKey(userID))
            return User.knownUsers.get(userID);
        else if ((me != null) && ((me.userPrivateProfile.getID().equalsIgnoreCase(userID)) || (me.userPublicProfile.getID().equalsIgnoreCase(userID)))) {
            return me;
        }
        else {
            User u = new User(userID);
            if (!u.isMe())
                User.knownUsers.put(userID,u);
            else
                me = u;
            return u;
        }
    }
    public static User getUser(PROFILE_ID profile_id) {
        if (User.knownUsers == null) throw new IllegalStateException("Users must be initialized.");
        else if (profile_id == null) throw new NullPointerException("PROFILE_ID must not be null.");
        else if (((profile_id.PUBLIC_NAME == null) || profile_id.PUBLIC_NAME.isEmpty()) && ((profile_id.USER_ID == null) || profile_id.USER_ID.isEmpty())) throw new IllegalArgumentException("PROFILE_ID must not be empty.");

        String userID = ((profile_id.PUBLIC_NAME == null) || profile_id.PUBLIC_NAME.isEmpty())?profile_id.USER_ID:profile_id.PUBLIC_NAME;

        return User.getUser(userID);
    }

    public static void unloadProfiles() {
        for (User user : User.knownUsers.values())
            user.unloadProfile();
    }

    public static User getMe() {
        return User.me;
    }
    public static void removeMe() {
        me = null;
    }

    /***********************************/
    /*        STATIC CALLBACKS         */
    /***********************************/

    public static void onFormatReceived(Object sender, FormatReceivedEventArgs args) {
        if (args.countReceivedFormats() > 0) {
            ArrayList<Format> formats = args.getReceivedFormats();
            for (Format format : formats) {
                if (format instanceof LOCAL_USER_PROFILE) {
                    if (User.me != null)
                        User.getMe().fromFormat(format);
                    else
                        User.me = new User(format);
                } else if (format instanceof PUBLIC_PROFILE) {
                    User.getUser(((PUBLIC_PROFILE) format).PUBLIC_NAME).fromFormat(format);
                } else if  (format instanceof PRIVATE_PROFILE) {
                    User.getUser(((PRIVATE_PROFILE) format).USER_ID).fromFormat(format);
                }
            }
        }
    }


    /***********************************/
    /***********************************/
    /***********************************/
    /***********************************/

    /***********************************/
    /*      DINAMYC MANAGEMENT         */
    /***********************************/

    /***********************************/
    /*          SYNC FIELDS            */
    /***********************************/
    private Boolean loading;

    public Boolean isLoading() {
        return this.loading;
    }

    /***********************************/
    /*      GENERAL USER FIELDS        */
    /***********************************/
    private Boolean isMe = false;
    private String email; //Only for local user;

    private String userID;
    private String color;
    private String showingName;
    private String imageURL;

    public Boolean isMe() {
        return this.isMe;
    }

    public String getEmail() {
        return this.email;
    }
    public void setEmail(String value) {
        this.email = value;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getColor() {
        return this.color;
    }

    public String getShowingName() {
        return this.showingName;
    }

    public String getImageURL() {
        return this.imageURL;
    }
    /***********************************/

    /***********************************/
    /*         USER PROFILES           */
    /***********************************/
    private Boolean isPrivate;
    private PublicProfile userPublicProfile; //Public profile for any user.
    private PrivateProfile userPrivateProfile; //Private profile for any user;

    public Boolean isPrivate() {
        return this.isPrivate;
    }
    public PublicProfile getUserPublicProfile() {
        return this.userPublicProfile;
    }
    public PrivateProfile getUserPrivateProfile() {
        return this.userPrivateProfile;
    }
    public Profile getUserProfile() {
        if (this.isPrivate)
            return this.userPrivateProfile;
        else
            return this.userPublicProfile;
    }

    public Boolean loadProfile() {
        //TODO: Load profile from local storage
        if (!this.isMe()) {
            Format[] formats = Format.getFormat((new JsonParser()).parse(User.userLocalStorage.RecoverCompleteUserProfile(this.getUserID())));
            for (Format format : formats)
                if (format instanceof PUBLIC_PROFILE) {
                    PublicProfile profile = new PublicProfile(format);
                    this.isMe = false;
                    this.userID = profile.getID();
                    this.color = profile.getColor();
                    this.showingName = profile.getShowingName();
                    this.imageURL = profile.getImageURL();
                    this.userPublicProfile = profile;
                } else if (format instanceof PRIVATE_PROFILE) {
                    PrivateProfile profile = new PrivateProfile(format);
                    this.isMe = false;
                    this.userID = profile.getID();
                    this.color = profile.getColor();
                    this.showingName = profile.getShowingName();
                    this.imageURL = profile.getImageURL();
                    this.userPrivateProfile = profile;
                }
        } else {
            Format[] formats = Format.getFormat((new JsonParser()).parse(User.userLocalStorage.RecoverLocalUserProfile()));
            for(Format format : formats)
                if (format instanceof LOCAL_USER_PROFILE) {
                    this.isMe = true;
                    this.email = ((LOCAL_USER_PROFILE) format).EMAIL;
                    this.userPrivateProfile = new PrivateProfile(((LOCAL_USER_PROFILE) format).USER_PRIVATE_PROFILE);
                    this.userPublicProfile = new PublicProfile(((LOCAL_USER_PROFILE) format).USER_PUBLIC_PROFILE);

                    this.userID = this.userPrivateProfile.getID();
                    this.color = this.userPrivateProfile.getColor();
                    this.showingName = this.userPrivateProfile.getShowingName();
                    this.imageURL = this.userPrivateProfile.getImageURL();
                }
        }

        if ((this.userPublicProfile != null) || (this.userPrivateProfile != null))
            return true;


        //TODO: Load profile from server
        return false;
    }
    public void unloadProfile() {
        if (this.isMe()) return;
        if (this.isPrivate())
            this.userPrivateProfile = null;
        else if (!this.isPrivate())
            this.userPublicProfile = null;
    }
    /***********************************/


    /*************************************/
    /*         CONSTRCUCTORS             */
    /*************************************/
    /**
     * Public constructor.
     * @param userID a string with the user's ID.
     */
    public User (String userID) {

        this.fromJson((new JsonParser()).parse(User.userLocalStorage.RecoverCompleteUserProfile(userID)));

        if ((this.userID == null) || (!this.userID.equals(userID)))
            this.fromJson((new JsonParser()).parse(User.userLocalStorage.RecoverLocalUserProfile()));

        if ((this.userID == null) || (!this.userID.equals(userID))) {
            this.userID = userID;
            this.loading = true;
            //TODO: Implement server information recovering
        }
    }

    private User () {
        this.isMe = false;
    }

    public User (Format format) {
        if (!this.fromFormat(format))
            throw new IllegalArgumentException("LOCAL_USER_PROFILE, PUBLIC_PROFILE or PRIVATE_PROFILE expected.");
    }
    /***********************************/

    /*************************************/
    /*         PARSE METHODS             */
    /*************************************/
    public Format toFormat(Format format) {
        if ((format instanceof LOCAL_USER_PROFILE) && (!this.isMe())) throw new IllegalArgumentException("Can`t convert general user to LOCAL_USER_PROFILE");
        else if ((format instanceof PUBLIC_PROFILE) && (!this.isMe()) && (this.isPrivate())) throw  new IllegalArgumentException("Can't convert private profile to public profile");
        else if ((format instanceof PRIVATE_PROFILE) && (!this.isMe()) && (!this.isPrivate())) throw  new IllegalArgumentException("Can't convert public profile to private profile");

        if (format instanceof LOCAL_USER_PROFILE) {
            ((LOCAL_USER_PROFILE) format).EMAIL = this.email;
            ((LOCAL_USER_PROFILE) format).USER_PUBLIC_PROFILE = ((PUBLIC_PROFILE)this.userPublicProfile.toFormat(new PUBLIC_PROFILE()));
            ((LOCAL_USER_PROFILE) format).USER_PRIVATE_PROFILE = ((PRIVATE_PROFILE)this.userPrivateProfile.toFormat(new PRIVATE_PROFILE()));
        } else if (format instanceof PUBLIC_PROFILE) {
            format = this.userPublicProfile.toFormat(format);
        } else if (format instanceof PRIVATE_PROFILE) {
            format = this.userPrivateProfile.toFormat(format);
        }

        return format;
    }
    public Boolean fromFormat(Format format) {
        if (format instanceof PUBLIC_PROFILE) {
            PublicProfile profile = new PublicProfile(format);
            this.isMe = false;
            this.userID = profile.getID();
            this.color = profile.getColor();
            this.showingName = profile.getShowingName();
            this.imageURL = profile.getImageURL();
            this.userPublicProfile = profile;
            this.isPrivate = false;

            return true;
        } else if (format instanceof PRIVATE_PROFILE) {
            PrivateProfile profile = new PrivateProfile(format);
            this.isMe = false;
            this.userID = profile.getID();
            this.color = profile.getColor();
            this.showingName = profile.getShowingName();
            this.imageURL = profile.getImageURL();
            this.userPrivateProfile = profile;
            this.isPrivate = true;

            return true;
        } else if (format instanceof LOCAL_USER_PROFILE) {
            this.isMe = true;
            this.email = ((LOCAL_USER_PROFILE) format).EMAIL;
            this.userPrivateProfile = new PrivateProfile(((LOCAL_USER_PROFILE) format).USER_PRIVATE_PROFILE);
            this.userPublicProfile = new PublicProfile(((LOCAL_USER_PROFILE) format).USER_PUBLIC_PROFILE);
            this.isPrivate = true;

            this.userID = this.userPrivateProfile.getID();
            this.color = this.userPrivateProfile.getColor();
            this.showingName = this.userPrivateProfile.getShowingName();
            this.imageURL = this.userPrivateProfile.getImageURL();

            return true;
        }

        return false;
    }

    public JsonElement toJson(Format format) {
        return this.toFormat(format).toJSON();
    }
    public void fromJson(JsonElement jsonElement) {
        Format[] formats = Format.getFormat(jsonElement);
        for (Format format : formats)
            if (this.fromFormat(format)) return;

        throw  new IllegalArgumentException("Expected LOCAL_USER_PROFILE, PUBLIC_PROFILE or PRIVATE_PROFILE formats.");
    }
}
