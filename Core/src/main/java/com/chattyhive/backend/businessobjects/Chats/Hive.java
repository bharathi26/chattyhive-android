package com.chattyhive.backend.businessobjects.Chats;

import com.chattyhive.backend.Controller;
import com.chattyhive.backend.businessobjects.Image;
import com.chattyhive.backend.businessobjects.Users.User;
import com.chattyhive.backend.contentprovider.AvailableCommands;
import com.chattyhive.backend.contentprovider.DataProvider;
import com.chattyhive.backend.contentprovider.OSStorageProvider.HiveLocalStorageInterface;
import com.chattyhive.backend.contentprovider.formats.CHAT;
import com.chattyhive.backend.contentprovider.formats.COMMON;
import com.chattyhive.backend.contentprovider.formats.Format;
import com.chattyhive.backend.contentprovider.formats.HIVE;
import com.chattyhive.backend.contentprovider.formats.HIVE_ID;
import com.chattyhive.backend.contentprovider.formats.HIVE_USERS_FILTER;
import com.chattyhive.backend.contentprovider.formats.INTERVAL;
import com.chattyhive.backend.contentprovider.formats.USER_PROFILE;
import com.chattyhive.backend.contentprovider.formats.USER_PROFILE_LIST;
import com.chattyhive.backend.util.events.CommandCallbackEventArgs;
import com.chattyhive.backend.util.events.Event;
import com.chattyhive.backend.util.events.EventArgs;
import com.chattyhive.backend.util.events.EventHandler;
import com.chattyhive.backend.util.events.FormatReceivedEventArgs;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Jonathan on 6/03/14.
 * This class represents a hive. A hive is one of the most basic business objects.
 */

public class Hive {

    /**************************
       Static hive management
     **************************/
    protected static HiveLocalStorageInterface localStorage;
    private static TreeMap<String,Hive> Hives;

    public static Event<EventArgs> HiveListChanged;

    public static void Initialize(Controller controller, HiveLocalStorageInterface hiveLocalStorageInterface) {
        HiveListChanged = new Event<EventArgs>();

        if (Hive.Hives == null) {
            Hive.Hives = new TreeMap<String, Hive>();
        }

        Hive.localStorage = hiveLocalStorageInterface;

        DataProvider.GetDataProvider().onHiveProfileReceived.add(new EventHandler<FormatReceivedEventArgs>(Hive.class, "onFormatReceived", FormatReceivedEventArgs.class));

        //Local recovering of hives
        String[] hives = Hive.localStorage.RecoverHives();
        if (hives != null) {
            for (String hive : hives) {
                Format[] formats = Format.getFormat((new JsonParser()).parse(hive));
                for (Format format : formats)
                    if (format instanceof HIVE)
                        Hive.Hives.put(((HIVE) format).NAME_URL, new Hive((HIVE) format));
            }
            if ((Hives.size() > 0) && (HiveListChanged != null))
                HiveListChanged.fire(null, EventArgs.Empty());
        }
        //Remote recovering of hives.
        /* This will be recovered with local user profile.*/
    }

    public static Boolean HiveIsLoaded (Hive hive) {
        return ((hive.category != null) && (hive.creationDate != null) && (hive.nameUrl != null) && (hive.name != null));
    }

    /***********************************/
    /*        STATIC LIST SUPPORT      */
    /***********************************/

    public static Hive getHiveByIndex(int index) {
        return Hives.values().toArray(new Hive[Hives.size()])[index];
    }

    public static int getHiveCount() {
        return Hives.size();
    }

    public static Collection<Hive> getHives() {
        return Hives.values();
    }

    public static boolean isHiveJoined(String nameUrl) {
        if (Hive.Hives == null) throw new IllegalStateException("Hives must be initialized.");
        else if (nameUrl == null) throw new NullPointerException("NameUrl must not be null.");
        else if (nameUrl.isEmpty()) throw  new IllegalArgumentException("NameUrl must not be empty.");

        return Hive.Hives.containsKey(nameUrl);
    }
    /***********************************/
    /*        STATIC CALLBACKS         */
    /***********************************/

    public static void onFormatReceived(Object sender, FormatReceivedEventArgs args) {
        if (args.countReceivedFormats() > 0) {
            ArrayList<Format> formats = args.getReceivedFormats();
            for (Format format : formats) {
                if (format instanceof HIVE) {
                    Hive.getHive(((HIVE) format).NAME_URL).fromFormat(format);
                } else if (format instanceof HIVE_ID) {
                    Hive.getHive(((HIVE_ID) format).NAME_URL).fromFormat(format);
                }
            }
        }
    }

    /*****************************************
     Constructor
     *****************************************/
    public Hive(HIVE data) {
        this.OnSubscribedUsersListUpdated = new Event<EventArgs>();
        this.category = data.CATEGORY;
        this.creationDate = data.CREATION_DATE;
        this.description = data.DESCRIPTION;

        this.name = data.NAME;
        this.nameUrl = data.NAME_URL;

        this.setImageURL(data.IMAGE_URL);

        this.publicChat = null;

        this.subscribedUsersCount = 0;
        this.tags = new String[0];
        this.chatLanguages = new String[0];

        if (data.TAGS != null) {
            this.tags = data.TAGS.toArray(new String[data.TAGS.size()]);
        }

        if (data.CHAT_LANGUAGES != null) {
            this.chatLanguages = data.CHAT_LANGUAGES.toArray(new String[data.CHAT_LANGUAGES.size()]);
        }

        if (data.SUBSCRIBED_USERS != null) {
            this.subscribedUsersCount = data.SUBSCRIBED_USERS;
        }

        if (data.PUBLIC_CHAT != null) {
            this.publicChat = Chat.getChat(data.PUBLIC_CHAT.CHANNEL_UNICODE, false);
            if (this.publicChat == null) {
                this.publicChat = new Chat(data.PUBLIC_CHAT,this);
            }
        }
    }
    private Hive(String nameUrl,Boolean internal) {
        this.OnSubscribedUsersListUpdated = new Event<EventArgs>();
        if (!internal) {
            this.name = nameUrl;
            this.creationDate = new Date();
        } else {
            String localHive = Hive.localStorage.RecoverHive(nameUrl);
            if ((localHive != null) && (!localHive.isEmpty())) {
                Format[] formats = Format.getFormat((new JsonParser()).parse(localHive));
                for (Format format : formats)
                    if (format instanceof HIVE) {
                        HIVE data = (HIVE) format;
                        if (data.NAME_URL.equals(nameUrl)) {
                            this.category = data.CATEGORY;
                            this.creationDate = data.CREATION_DATE;
                            this.description = data.DESCRIPTION;

                            this.setImageURL(data.IMAGE_URL);

                            this.name = data.NAME;
                            this.nameUrl = data.NAME_URL;

                            if (data.PUBLIC_CHAT != null) {
                                this.publicChat = Chat.getChat(data.PUBLIC_CHAT.CHANNEL_UNICODE, false);
                                if (this.publicChat == null) {
                                    this.publicChat = new Chat(data.PUBLIC_CHAT, this);
                                }
                            } else {
                                this.publicChat = Chat.getChat(String.format("presence-%s", this.nameUrl));
                            }
                            break;
                        }
                    }
            }
            if ((this.nameUrl == null) || (!this.nameUrl.equals(nameUrl))) {
                this.nameUrl = nameUrl;
                DataProvider.GetDataProvider().InvokeServerCommand(AvailableCommands.HiveInfo, this.toFormat(new HIVE_ID()));
            }
        }
    }

    public Hive(String name, String nameUrl) {
        this(name,false);
        this.nameUrl = nameUrl;
    }

    public Hive(String name) {
        this(name,false);
    }

    public static Hive getHive(String nameUrl) {
        if (Hive.Hives == null) throw new IllegalStateException("Hives must be initialized.");
        else if (nameUrl == null) throw new NullPointerException("NameUrl must not be null.");
        else if (nameUrl.isEmpty()) throw  new IllegalArgumentException("NameUrl must not be empty.");

        if (Hive.Hives.containsKey(nameUrl))
            return Hive.Hives.get(nameUrl);
        else {
            Hive h = new Hive(nameUrl,true);
            Hive.Hives.put(nameUrl,h);
            if (HiveListChanged != null)
                HiveListChanged.fire(h,EventArgs.Empty());
            return h;
        }
    }

    private EventHandler<CommandCallbackEventArgs> createHiveCallback;

    public void createHive(EventHandler<CommandCallbackEventArgs> callback) {
        this.createHiveCallback = callback;
        Controller.GetRunningController().getDataProvider().RunCommand(AvailableCommands.CreateHive,new EventHandler<CommandCallbackEventArgs>(this,"OnHiveCreated",CommandCallbackEventArgs.class),this.toFormat(new HIVE()));
    }

    public void OnHiveCreated(Object sender, CommandCallbackEventArgs eventArgs) {
        HIVE_ID hive_id = null;
        CHAT chat = null;
        Boolean joinOK = false;

        ArrayList<Format> received = eventArgs.getReceivedFormats();
        for (Format format : received) {
            if ((format instanceof COMMON) && (((COMMON) format).STATUS.equalsIgnoreCase("OK")))
                joinOK = true;
            else if (format instanceof HIVE_ID)
                hive_id = (HIVE_ID)format;
            else if (format instanceof CHAT)
                chat = (CHAT)format;
        }


        if ((joinOK) && (hive_id != null) && (chat != null)) {
            String hiveNameURL = hive_id.NAME_URL;
            this.nameUrl = hiveNameURL;
            this.publicChat = new Chat(chat, this);
            this.subscribedUsersCount = 1;
            Hive.Hives.put(hiveNameURL, this);

            if (HiveListChanged != null)
                HiveListChanged.fire(this, EventArgs.Empty());

            if (this.createHiveCallback != null)
                this.createHiveCallback.Run(this,eventArgs);

            //Local storage
            Hive.localStorage.StoreHive(this.nameUrl, this.toJson(new HIVE()).toString());
        }
    }

    /*****************************************
     users list
     *****************************************/
    public enum HiveUsersType {
        OUTSTANDING,
        LOCATION,
        RECENTLY_ONLINE,
        NEW
    }
    private HiveUsersType lastRequestedUserList;
    public Event<EventArgs> OnSubscribedUsersListUpdated;
    public void requestUsers(int start, int count, HiveUsersType listType) {
        if ((this.lastRequestedUserList == null) || (this.lastRequestedUserList.compareTo(listType) != 0)) {
            this.subscribedUsers = new ArrayList<User>();
            this.lastRequestedUserList = listType;
        }
        HIVE_USERS_FILTER hive_users_filter = new HIVE_USERS_FILTER();
        hive_users_filter.RESULT_INTERVAL = new INTERVAL();

        hive_users_filter.TYPE = this.lastRequestedUserList.name();
        hive_users_filter.RESULT_INTERVAL.START_INDEX = (start > 0)?String.valueOf(start):"FIRST";
        hive_users_filter.RESULT_INTERVAL.COUNT = count;

        Controller.GetRunningController().getDataProvider().RunCommand(AvailableCommands.HiveUsers,new EventHandler<CommandCallbackEventArgs>(this,"requestUsersCallback",CommandCallbackEventArgs.class),hive_users_filter,this.toFormat(new HIVE_ID()));
    }
    public void requestUsersCallback (Object sender, CommandCallbackEventArgs eventArgs) {
        USER_PROFILE_LIST user_profile_list = null;
        Boolean requestOK = false;

        ArrayList<Format> received = eventArgs.getReceivedFormats();
        for (Format format : received) {
            if ((format instanceof COMMON) && (((COMMON) format).STATUS.equalsIgnoreCase("OK")))
                requestOK = true;
            else if (format instanceof USER_PROFILE_LIST)
                user_profile_list = (USER_PROFILE_LIST)format;
        }


        if ((requestOK) && (user_profile_list != null)) {
            if (user_profile_list.LIST != null) {
                boolean listChanged = false;
                for (USER_PROFILE user_profile : user_profile_list.LIST) {
                    try {
                        String userID = ((user_profile.USER_BASIC_PRIVATE_PROFILE != null) && (user_profile.USER_BASIC_PRIVATE_PROFILE.USER_ID != null)&& (!user_profile.USER_BASIC_PRIVATE_PROFILE.USER_ID.isEmpty()))?user_profile.USER_BASIC_PRIVATE_PROFILE.USER_ID:
                                ((user_profile.USER_PRIVATE_PROFILE != null) && (user_profile.USER_PRIVATE_PROFILE.USER_ID != null)&& (!user_profile.USER_PRIVATE_PROFILE.USER_ID.isEmpty()))?user_profile.USER_PRIVATE_PROFILE.USER_ID:
                                        ((user_profile.USER_BASIC_PUBLIC_PROFILE != null) && (user_profile.USER_BASIC_PUBLIC_PROFILE.USER_ID != null)&& (!user_profile.USER_BASIC_PUBLIC_PROFILE.USER_ID.isEmpty()))?user_profile.USER_BASIC_PUBLIC_PROFILE.USER_ID:
                                                ((user_profile.USER_PUBLIC_PROFILE != null) && (user_profile.USER_PUBLIC_PROFILE.USER_ID != null)&& (!user_profile.USER_PUBLIC_PROFILE.USER_ID.isEmpty()))?user_profile.USER_PUBLIC_PROFILE.USER_ID:null;
                        if (userID != null) {
                            User u = Controller.GetRunningController().getUser(userID,user_profile);
                            listChanged = ((!this.subscribedUsers.contains(u)) && this.subscribedUsers.add(u)) || listChanged;
                        }
                    } catch (Exception e) { }
                }
            }
        } else {
            if (this.subscribedUsers == null)
                this.subscribedUsers = new ArrayList<User>();
        }

        if (OnSubscribedUsersListUpdated != null)
            this.OnSubscribedUsersListUpdated.fire(this, EventArgs.Empty());
    }

    private ArrayList<User> contextUsers;
    protected boolean setContextUsers(List<USER_PROFILE> newUsersList) {
        boolean result = false;
        ArrayList<User> newUsers = new ArrayList<User>();

        if (this.contextUsers == null)
            this.contextUsers = new ArrayList<User>();

        if (newUsersList != null) {
            boolean listChanged = false;
            for (USER_PROFILE user_profile : newUsersList) {
                try {
                    User u = new User(user_profile, Controller.GetRunningController());
                    listChanged = newUsers.add(u) || listChanged;
                    result = (!this.contextUsers.contains(u)) || result;
                } catch (Exception e) { }
            }
            if (listChanged && result) {
                this.contextUsers.clear();
                this.contextUsers.addAll(newUsers);
            } else {
                result = false;
            }
        }


        return result;
    }
    protected List<User> getContextUsers() {
        if (this.contextUsers != null)
            return Collections.unmodifiableList(this.contextUsers);
        else
            return null;
    }

    /*****************************************
     context (category, ...)
     *****************************************/
    protected String category;
    protected Date creationDate;
    protected String description;
    protected String name;
    protected String nameUrl;
    protected Chat publicChat;
    protected Integer subscribedUsersCount;
    protected String[] chatLanguages;
    protected String[] tags;
    protected ArrayList<User> subscribedUsers;

    protected String imageURL;
    protected Image hiveImage;

    public String getImageURL() {
        return this.imageURL;
    }
    public void setImageURL(String value) {
        this.imageURL = value;
        if (this.hiveImage != null)
            this.hiveImage.freeMemory();
        if (value != null)
            this.hiveImage = new Image(value);
        else
            this.hiveImage = null;
    }
    public Image getHiveImage() {
        return this.hiveImage;
    }

    public void setCategory (String value) { this.category = value; }
    public String getCategory() { return this.category; }

    public Date getCreationDate() { return this.creationDate; }

    public void setDescription (String value) { this.description = value; }
    public String getDescription() { return this.description; }

    public String getName() { return this.name; }
    public void setName(String name) {
        if ((this.nameUrl == null) || (this.nameUrl.isEmpty()))
            this.name = name;
        else
            throw new UnsupportedOperationException("It is not allowed to change a hive's name if hive is already created.");
    }

    public String getNameUrl() { return this.nameUrl; }

    public Chat getPublicChat() { return this.publicChat; }
    public void setPublicChat(Chat value) { this.publicChat = value; }

    public String[] getChatLanguages() {
        return this.chatLanguages;
    }
    public void setChatLanguages(String[] chatLanguages) {
        this.chatLanguages = chatLanguages;
    }

    public String[] getTags() {
        return this.tags;
    }
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public List<User> getSubscribedUsers() {
        if (this.subscribedUsers != null)
            return Collections.unmodifiableList(this.subscribedUsers);
        else
            return null;
    }

    public int getSubscribedUsersCount() {
        if (this.subscribedUsersCount != null)
            return this.subscribedUsersCount;
        else
            return 0;
    }
    public int incSubscribedUsers(int quantity) {
        if (this.subscribedUsersCount == null)
            this.subscribedUsersCount = 0;
        return this.subscribedUsersCount += quantity;
    }

    /*************************************/
    /*         PARSE METHODS             */
    /*************************************/
    public Format toFormat(Format format) {
        if (format instanceof HIVE) {
            ((HIVE) format).NAME_URL = this.nameUrl;
            ((HIVE) format).NAME = this.name;
            ((HIVE) format).CATEGORY = this.category;
            ((HIVE) format).CREATION_DATE = this.creationDate;
            ((HIVE) format).DESCRIPTION = this.description;
            ((HIVE) format).IMAGE_URL = this.imageURL;
            ((HIVE) format).SUBSCRIBED_USERS = this.subscribedUsersCount;

            if (this.tags != null) {
                ((HIVE) format).TAGS = new ArrayList<String>(Arrays.asList(this.tags));
            }

            if (this.chatLanguages != null) {
                ((HIVE) format).CHAT_LANGUAGES = new ArrayList<String>(Arrays.asList(this.chatLanguages));
            }

            if (this.publicChat != null)
                ((HIVE) format).PUBLIC_CHAT = (CHAT)this.publicChat.toFormat(new CHAT());
            else
                ((HIVE) format).PUBLIC_CHAT = null;
        } else if (format instanceof HIVE_ID) {
            ((HIVE_ID) format).NAME_URL = this.nameUrl;
        }

        return format;
    }
    public Boolean fromFormat(Format format) {
        if (format instanceof HIVE) {
            this.name = ((HIVE) format).NAME;
            this.nameUrl = ((HIVE) format).NAME_URL;
            this.category = ((HIVE) format).CATEGORY;
            this.description = ((HIVE) format).DESCRIPTION;
            this.creationDate = ((HIVE) format).CREATION_DATE;
            this.setImageURL(((HIVE) format).IMAGE_URL);
            this.publicChat = null;
            this.subscribedUsersCount = 0;
            this.tags = new String[0];
            this.chatLanguages = new String[0];

            if (((HIVE) format).TAGS != null) {
                this.tags = ((HIVE) format).TAGS.toArray(new String[((HIVE) format).TAGS.size()]);
            }

            if (((HIVE) format).CHAT_LANGUAGES != null) {
                this.chatLanguages = ((HIVE) format).CHAT_LANGUAGES.toArray(new String[((HIVE) format).CHAT_LANGUAGES.size()]);
            }

            if (((HIVE) format).SUBSCRIBED_USERS != null) {
                this.subscribedUsersCount = ((HIVE) format).SUBSCRIBED_USERS;
            }

            if (((HIVE) format).PUBLIC_CHAT != null) {
                this.publicChat = Chat.getChat(((HIVE) format).PUBLIC_CHAT);
                if (this.publicChat == null) {
                    this.publicChat = new Chat(((HIVE) format).PUBLIC_CHAT,this);
                }
            } else {
                this.publicChat = Chat.getChat(String.format("presence-%s", this.nameUrl));
                this.publicChat.parentHive = this;
                this.publicChat.chatKind = ChatKind.HIVE;
            }
            return true;
        } else if (format instanceof HIVE_ID) {
            this.nameUrl = ((HIVE_ID) format).NAME_URL;

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

        throw  new IllegalArgumentException("Expected HIVE or HIVE_ID formats.");
    }
}
