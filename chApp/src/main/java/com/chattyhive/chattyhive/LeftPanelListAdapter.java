package com.chattyhive.chattyhive;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chattyhive.backend.businessobjects.Chats.Chat;
import com.chattyhive.backend.businessobjects.Chats.Hive;
import com.chattyhive.backend.businessobjects.Chats.Messages.Message;
import com.chattyhive.backend.businessobjects.Image;
import com.chattyhive.backend.businessobjects.Users.ProfileLevel;
import com.chattyhive.backend.businessobjects.Users.ProfileType;
import com.chattyhive.backend.businessobjects.Users.User;
import com.chattyhive.backend.util.events.Event;
import com.chattyhive.backend.util.events.EventArgs;
import com.chattyhive.backend.util.events.EventHandler;
import com.chattyhive.backend.util.formatters.DateFormatter;
import com.chattyhive.backend.util.formatters.TimestampFormatter;
import com.chattyhive.chattyhive.framework.Util.StaticMethods;
import com.chattyhive.chattyhive.util.Category;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Jonathan on 13/03/14.
 */

public class LeftPanelListAdapter extends BaseAdapter {

    private Context context;
    private ListView listView;
    private LayoutInflater inflater;
    private int visibleList;
    private View.OnClickListener clickListener;
    public Event<EventArgs> ListSizeChanged;
    public ArrayList<Chat> chatList;
    public ArrayList<User> friendList;

    public void SetVisibleList(int LeftPanel_ListKind) {
        this.visibleList = LeftPanel_ListKind;
        this.OnAddItem(this, EventArgs.Empty());
    }

    public int GetVisibleList() {
        return this.visibleList;
    }

    public void SetOnClickListener(View.OnClickListener listener) {
        this.clickListener = listener;
        notifyDataSetChanged();
    }

    public void OnAddItem(Object sender, EventArgs args) {  //TODO: This is only a patch. Hive and Chat collections must be updated on UIThread.
        ((Activity) this.context).runOnUiThread(new Runnable() {
            public void run() {
                chatList = null;
                friendList = null;
                 if (visibleList == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Chats)) {
                    while (chatList == null)
                        try {
                            CaptureChats();
                        } catch (Exception e) {
                            // e.printStackTrace();
                            chatList = null;
                        }
                } else if (visibleList == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Mates)) {
                    while (friendList == null)
                        try {
                            CaptureFriends();
                        } catch (Exception e) {
                            //  e.printStackTrace();
                            friendList = null;
                        }
                }

                notifyDataSetChanged();
                if ((ListSizeChanged != null) && (ListSizeChanged.count() > 0))
                    ListSizeChanged.fire(this, EventArgs.Empty());
            }
        });
    }

    private void CaptureChats() {
        TreeSet<Chat> list = new TreeSet<Chat>(new Comparator<Chat>() {
            @Override
            public int compare(Chat lhs, Chat rhs) { // lhs < rhs => return < 0 | lhs = rhs => return = 0 | lhs > rhs => return > 0
                int res = 0;
                if ((lhs == null) && (rhs != null))
                    res = 1;
                else if ((lhs != null) && (rhs == null))
                    res = -1;
                else if (lhs == null) //&& (rhs == null)) <- Which is always true
                    res = 0;
                else {
                    Date lhsDate = null;
                    Date rhsDate = null;

                    if ((lhs.getConversation() != null) && (lhs.getConversation().getLastMessage() != null))
                        lhsDate = lhs.getConversation().getLastMessage().getOrdinationTimeStamp();
                    else
                        lhsDate = lhs.getCreationDate();

                    if ((rhs.getConversation() != null) && (rhs.getConversation().getLastMessage() != null))
                        rhsDate = rhs.getConversation().getLastMessage().getOrdinationTimeStamp();
                    else
                        rhsDate = rhs.getCreationDate();

                    if ((lhsDate == null) && (rhsDate != null))
                        res = 1;
                    else if ((lhsDate != null) && (rhsDate == null))
                        res = -1;
                    else if (lhsDate != null) //&& (rhsDate != null)) <- Which is always true
                        res = rhsDate.compareTo(lhsDate);
                    else {
                        lhsDate = lhs.getCreationDate();
                        rhsDate = rhs.getCreationDate();

                        if ((lhsDate == null) && (rhsDate != null))
                            res = 1;
                        else if ((lhsDate != null) && (rhsDate == null))
                            res = -1;
                        else if (lhsDate != null) //&& (rhsDate != null)) <- Which is always true
                            res = rhsDate.compareTo(lhsDate);
                        else {
                            res = 0;
                        }
                    }
                }

                return res;
            }
        });
        list.addAll(Chat.getChats());
        chatList = new ArrayList<Chat>(list);
    }



    private void CaptureFriends() {
        TreeSet<User> list = new TreeSet<User>(new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) { // lhs < rhs => return < 0 | lhs = rhs => return = 0 | lhs > rhs => return > 0
                int res = 0;
                if ((lhs == null) && (rhs != null))
                    res = 1;
                else if ((lhs != null) && (rhs == null))
                    res = -1;
                else if (lhs == null) //&& (rhs == null)) <- Which is always true
                    res = 0;
                else {
                    String lhsName = null;
                    String rhsName = null;


                    if (lhs.getUserPrivateProfile() != null)
                        lhsName = lhs.getUserPrivateProfile().getShowingName();

                    if (rhs.getUserPrivateProfile() != null)
                        rhsName = rhs.getUserPrivateProfile().getShowingName();

                    if ((lhsName == null) && (rhsName != null))
                        res = 1;
                    else if ((lhsName != null) && (rhsName == null))
                        res = -1;
                    else if (lhsName != null) //&& (rhsName != null)) <- Which is always true
                        res = lhsName.compareToIgnoreCase(rhsName);
                    else {
                        if (lhs.getUserPublicProfile() != null)
                            lhsName = lhs.getUserPublicProfile().getShowingName();
                        if (rhs.getUserPublicProfile() != null)
                            rhsName = rhs.getUserPublicProfile().getShowingName();

                        if ((lhsName == null) && (rhsName != null))
                            res = 1;
                        else if ((lhsName != null) && (rhsName == null))
                            res = -1;
                        else if (lhsName != null) //&& (rhsName != null)) <- Which is always true
                            res = lhsName.compareTo(rhsName);
                        else {
                            res = 0;
                        }
                    }
                }

                return res;
            }
        });
        User me = ((Main)context).controller.getMe();
        if (me != null)
            list.addAll(me.getFriends());

        friendList = new ArrayList<User>(list);
    }

    public LeftPanelListAdapter (Context activityContext) {
        super();
        this.context = activityContext;
        this.ListSizeChanged = new Event<EventArgs>();
        this.inflater = ((Activity) this.context).getLayoutInflater();
        this.listView = ((ListView) ((Activity) this.context).findViewById(R.id.left_panel_element_list));
        //this.listView.setAdapter(this);

        if (visibleList == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Chats)) {
            friendList = null;
            CaptureChats();
        } else if (visibleList == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Mates)) {
            chatList = null;
            CaptureFriends();
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return visibleList;
    }

    @Override
    public int getViewTypeCount() {
        return this.context.getResources().getInteger(R.integer.LeftPanel_ListKind_Count);
    }

    @Override
    public int getCount() {
        int result = 0;
       if (this.visibleList == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Chats)) {
            result = chatList.size();
        } else if (this.visibleList == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Mates)) {
            result = friendList.size();
        }
        return result;
    }

    @Override
    public Object getItem(int position) {
        if (this.visibleList == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Chats)) {
            return chatList.get(position);
        } else if (this.visibleList == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Mates)) {
            return friendList.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        int type = visibleList;

        if (type == context.getResources().getInteger(R.integer.LeftPanel_ListKind_None)) {
            return null;
        }
        if (convertView == null) {
            TypedValue alpha = new TypedValue();
            if (type == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Chats)) {
                holder = new ChatViewHolder();
                convertView = this.inflater.inflate(R.layout.left_panel_chat_list_item, parent, false);
                ((ChatViewHolder) holder).chatItem = (RelativeLayout) convertView.findViewById((R.id.left_panel_chat_list_item_top_view));
                ((ChatViewHolder) holder).chatName = (TextView) convertView.findViewById(R.id.left_panel_chat_list_item_chat_name);
                ((ChatViewHolder) holder).chatLastMessage = (TextView) convertView.findViewById(R.id.left_panel_chat_list_item_last_message);
                ((ChatViewHolder) holder).chatImage = (ImageView) convertView.findViewById(R.id.left_panel_chat_list_item_big_img);
                ((ChatViewHolder) holder).chatHiveImage = (ImageView) convertView.findViewById(R.id.left_panel_chat_list_item_little_img);
                ((ChatViewHolder) holder).chatLastMessageTimestamp = (TextView) convertView.findViewById(R.id.left_panel_chat_list_item_timestamp);
                ((ChatViewHolder) holder).chatPendingMessagesNumber = (TextView) convertView.findViewById(R.id.left_panel_chat_list_item_number_messages);
                ((ChatViewHolder) holder).chatTypeImage = (ImageView) convertView.findViewById(R.id.left_panel_chat_list_item_item_type_img);
                ((ChatViewHolder) holder).chatItem.setOnClickListener(clickListener);

                //set the alpha values
                convertView.getContext().getResources().getValue(R.color.left_panel_chat_list_item_item_type_img_alpha, alpha, true);
                StaticMethods.SetAlpha(((ChatViewHolder) holder).chatTypeImage, alpha.getFloat());
            } else if (type == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Mates)) {
                convertView = this.inflater.inflate(R.layout.left_panel_friend_list_item,parent,false);
                holder = new FriendViewHolder(convertView);
            }

            if (convertView != null)
                convertView.setTag(R.id.LeftPanel_ListViewHolder, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.LeftPanel_ListViewHolder);
        }

        Object item = this.getItem(position);

        if (item == null) {
            Log.w("LeftPanelListAdapter", "In getView: item is NULL");
            return null;
        }

        if (type == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Chats)) {
            String GroupName = "";
            SpannableString LastMessage = new SpannableString("");
            Message lastMessage = null;
            String LastMessageTimestamp = "";

            try {
                lastMessage = ((Chat) item).getConversation().getLastMessage();
                Date timeStamp = lastMessage.getOrdinationTimeStamp();
                Date fiveMinutesAgo = new Date((new Date()).getTime() - 5 * 60 * 1000);
                Date today = DateFormatter.toDate(DateFormatter.toString(new Date()));
                Calendar yesterday = Calendar.getInstance();
                yesterday.setTime(today);
                yesterday.roll(Calendar.DATE, false);
                if (timeStamp.after(fiveMinutesAgo))
                    LastMessageTimestamp = this.context.getString(R.string.left_panel_imprecise_time_now);
                else if (timeStamp.after(today))
                    LastMessageTimestamp = TimestampFormatter.toLocaleString(timeStamp);
                else if (timeStamp.after(yesterday.getTime()))
                    LastMessageTimestamp = this.context.getString(R.string.left_panel_imprecise_time_yesterday);
                else
                    LastMessageTimestamp = DateFormatter.toShortHumanReadableString(timeStamp);
            } catch (Exception e) {
                //Log.w("ChatItem","Unable to recover last message: "+e.getMessage());
            }
            if (((Chat) item).getChatKind() == null) return null;

            ((ChatViewHolder) holder).chatHiveImage.setImageResource(R.drawable.default_hive_image);

            switch (((Chat) item).getChatKind()) {
                case PUBLIC_SINGLE:
                    ((ChatViewHolder) holder).chatHiveImage.setVisibility(View.VISIBLE);
                    ((ChatViewHolder) holder).chatTypeImage.setImageResource(R.drawable.pestanha_chats_arroba);
                    ((ChatViewHolder) holder).chatImage.setImageResource(R.drawable.chats_users_online);
                    try {
                        ((Chat) item).getParentHive().getHiveImage().OnImageLoaded.add(new EventHandler<EventArgs>(holder, "loadHiveImage", EventArgs.class));
                        ((Chat) item).getParentHive().getHiveImage().loadImage(Image.ImageSize.small, 0);
                        ((ChatViewHolder) holder).hiveName = context.getResources().getString(R.string.hivename_identifier_character).concat(((Chat) item).getParentHive().getName());
                    } catch (Exception e) {
                    }
                    for (User user : ((Chat) item).getMembers())
                        if (!user.isMe()) {
                            ((ChatViewHolder) holder).user = user;
                            if ((user.getUserPublicProfile() != null) && (user.getUserPublicProfile().getShowingName() != null)) {
                                GroupName = context.getResources().getString(R.string.public_username_identifier_character).concat(user.getUserPublicProfile().getShowingName());
                                try {
                                    user.getUserPublicProfile().getProfileImage().OnImageLoaded.add(new EventHandler<EventArgs>(holder, "loadChatImage", EventArgs.class));
                                    user.getUserPublicProfile().getProfileImage().loadImage(Image.ImageSize.medium, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else
                                user.UserLoaded.add(new EventHandler<EventArgs>(this, "OnAddItem", EventArgs.class));
                        }

                    if (lastMessage != null) {
                        String lastMessageString = "";
                        Drawable typeIcon = null;

                        if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase(this.context.getString(R.string.default_left_panel_image_content_type))) {
                            lastMessageString = "   ".concat(this.context.getString(R.string.default_left_panel_image_text));
                            typeIcon = this.context.getResources().getDrawable(R.drawable.default_left_panel_image_icon);
                        } else { //if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase("TEXT")) {
                            lastMessageString = " ".concat(lastMessage.getMessageContent().getContent());
                        }
                        
                        Drawable directionImg = this.context.getResources().getDrawable( (lastMessage.getUser().isMe()) ? R.drawable.default_left_panel_last_message_outgoing_icon : R.drawable.default_left_panel_last_message_incoming_icon );
                        directionImg.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                        if (typeIcon != null) {
                            typeIcon.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                            typeIcon.setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);
                        }

                        LastMessage = new SpannableString(lastMessageString);

                        if (typeIcon != null) {
                            LastMessage.setSpan(new ImageSpan(typeIcon,ImageSpan.ALIGN_BOTTOM),1,2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        LastMessage.setSpan(new ImageSpan(directionImg,ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    ((ChatViewHolder) holder).chatLastMessageTimestamp.setVisibility(View.VISIBLE);
                    ((ChatViewHolder) holder).chatPendingMessagesNumber.setVisibility(View.INVISIBLE);


                    ((ChatViewHolder) holder).profileType = Profile.ProfileType.Public;
                    ((ChatViewHolder) holder).chatImage.setOnClickListener(((ChatViewHolder) holder).thumbnailClickListener);
                    ((ChatViewHolder) holder).chatImage.setClickable(true);

                    break;
                case PUBLIC_GROUP:
                    ((ChatViewHolder) holder).chatHiveImage.setVisibility(View.VISIBLE);
                    ((ChatViewHolder) holder).chatTypeImage.setImageResource(R.drawable.pestanha_hives_show_more_users);
                    ((ChatViewHolder) holder).chatImage.setImageResource(R.drawable.chats_users_online);
                    try {
                        ((Chat) item).getParentHive().getHiveImage().OnImageLoaded.add(new EventHandler<EventArgs>(holder, "loadHiveImage", EventArgs.class));
                        ((Chat) item).getParentHive().getHiveImage().loadImage(Image.ImageSize.small, 0);
                    } catch (Exception e) {
                    }
                    if ((((Chat) item).getName() != null) && (!((Chat) item).getName().isEmpty()))
                        GroupName = ((Chat) item).getName();
                    else
                        for (User user : ((Chat) item).getMembers())
                            if (!user.isMe()) {
                                if ((user.getUserPublicProfile() != null) && (user.getUserPublicProfile().getShowingName() != null))
                                    GroupName += ((GroupName.isEmpty()) ? "" : ", ").concat(context.getResources().getString(R.string.public_username_identifier_character).concat(user.getUserPublicProfile().getShowingName()));
                                else
                                    user.UserLoaded.add(new EventHandler<EventArgs>(this, "OnAddItem", EventArgs.class));
                            }

                    if (lastMessage != null) {
                        String lastMessageString = "";
                        Drawable typeIcon = null;

                        if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase(this.context.getString(R.string.default_left_panel_image_content_type))) {
                            lastMessageString = "   ".concat(this.context.getString(R.string.default_left_panel_image_text));
                            typeIcon = this.context.getResources().getDrawable(R.drawable.default_left_panel_image_icon);
                        } else { //if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase("TEXT")) {
                            lastMessageString = " ".concat(lastMessage.getMessageContent().getContent());
                        }

                        Drawable directionImg = this.context.getResources().getDrawable( (lastMessage.getUser().isMe()) ? R.drawable.default_left_panel_last_message_outgoing_icon : R.drawable.default_left_panel_last_message_incoming_icon );
                        directionImg.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                        if (typeIcon != null) {
                            typeIcon.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                            typeIcon.setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);
                        }

                        LastMessage = new SpannableString(lastMessageString);

                        if (typeIcon != null) {
                            LastMessage.setSpan(new ImageSpan(typeIcon,ImageSpan.ALIGN_BOTTOM),1,2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        LastMessage.setSpan(new ImageSpan(directionImg,ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    ((ChatViewHolder) holder).chatLastMessageTimestamp.setVisibility(View.VISIBLE);
                    ((ChatViewHolder) holder).chatPendingMessagesNumber.setVisibility(View.INVISIBLE);

                    ((ChatViewHolder) holder).chatImage.setOnClickListener(null);
                    ((ChatViewHolder) holder).chatImage.setClickable(false);
                    break;
                case HIVE:
                    ((ChatViewHolder) holder).chatHiveImage.setVisibility(View.GONE);
                    ((ChatViewHolder) holder).chatTypeImage.setImageResource(R.drawable.pestanha_chats_public_chat);
                    ((ChatViewHolder) holder).chatImage.setImageResource(R.drawable.default_hive_image);
                    try {
                        ((Chat) item).getParentHive().getHiveImage().OnImageLoaded.add(new EventHandler<EventArgs>(holder, "loadChatImage", EventArgs.class));
                        ((Chat) item).getParentHive().getHiveImage().loadImage(Image.ImageSize.medium, 0);
                    } catch (Exception e) {
                    }
                    if (((Chat) item).getParentHive() != null)
                        GroupName = context.getResources().getString(R.string.hivename_identifier_character).concat(((Chat) item).getParentHive().getName());

                    if (lastMessage != null) {
                        String userName = "";
                        String lastMessageString = "";
                        Drawable typeIcon = null;

                        if ((lastMessage.getUser() != null) && (lastMessage.getUser().getUserPublicProfile() != null) && (lastMessage.getUser().getUserPublicProfile().getShowingName() != null)) {
                            userName = context.getResources().getString(R.string.public_username_identifier_character).concat(lastMessage.getUser().getUserPublicProfile().getShowingName()).concat(":");
                        }

                        if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase(this.context.getString(R.string.default_left_panel_image_content_type))) {
                            lastMessageString = "   ".concat(this.context.getString(R.string.default_left_panel_image_text));
                            typeIcon = this.context.getResources().getDrawable(R.drawable.default_left_panel_image_icon);
                        } else { //if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase("TEXT")) {
                            lastMessageString = " ".concat(lastMessage.getMessageContent().getContent());
                        }

                        if (typeIcon != null) {
                            typeIcon.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                            typeIcon.setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);
                        }

                        LastMessage = new SpannableString(lastMessageString);

                        if (typeIcon != null) {
                            LastMessage.setSpan(new ImageSpan(typeIcon,ImageSpan.ALIGN_BOTTOM),1,2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        LastMessage = new SpannableString(TextUtils.concat(new SpannableString(userName),LastMessage));

                        //LastMessage.setSpan(new SpannableString(userName), 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }

                    /*if ((lastMessage != null) && (lastMessage.getUser() != null) && (lastMessage.getUser().getUserPublicProfile() != null) && (lastMessage.getUser().getUserPublicProfile().getShowingName() != null) && (lastMessage.getMessageContent() != null) && (lastMessage.getMessageContent().getContent() != null)) {
                        LastMessage = new SpannableString(context.getResources().getString(R.string.public_username_identifier_character).concat(lastMessage.getUser().getUserPublicProfile().getShowingName()).concat(": ").concat(lastMessage.getMessageContent().getContent()));
                    }*/
                    ((ChatViewHolder)holder).chatLastMessageTimestamp.setVisibility(View.GONE);
                    ((ChatViewHolder)holder).chatPendingMessagesNumber.setVisibility(View.INVISIBLE);
                    ((ChatViewHolder)holder).chatImage.setAdjustViewBounds(true);
                    ((ChatViewHolder)holder).chatImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                    ((ChatViewHolder) holder).chatImage.setOnClickListener(null);
                    ((ChatViewHolder) holder).chatImage.setClickable(false);
                    break;
                case PRIVATE_SINGLE:
                    ((ChatViewHolder) holder).chatHiveImage.setVisibility(View.GONE);
                    ((ChatViewHolder) holder).chatTypeImage.setImageResource(R.drawable.pestanha_chats_user);
                    ((ChatViewHolder) holder).chatImage.setImageResource(R.drawable.default_profile_image_male);

                    for (User user : ((Chat) item).getMembers())
                        if (!user.isMe()) {
                            ((ChatViewHolder) holder).user = user;
                            if ((user.getUserPrivateProfile() != null) && (user.getUserPrivateProfile().getShowingName() != null)) {
                                GroupName = user.getUserPrivateProfile().getShowingName();
                                if (user.getUserPrivateProfile().getProfileImage() == null) {
                                    if ((user.getUserPrivateProfile().getSex() != null) && (user.getUserPrivateProfile().getSex().equalsIgnoreCase("female")))
                                        ((ChatViewHolder) holder).chatImage.setImageResource(R.drawable.default_profile_image_female);
                                } else {
                                    user.getUserPrivateProfile().getProfileImage().OnImageLoaded.add(new EventHandler<EventArgs>(holder, "loadChatImage", EventArgs.class));
                                    user.getUserPrivateProfile().getProfileImage().loadImage(Image.ImageSize.medium, 0);
                                }
                            } else
                                user.UserLoaded.add(new EventHandler<EventArgs>(this, "OnAddItem", EventArgs.class));
                        }
                    if (lastMessage != null) {
                        String lastMessageString = "";
                        Drawable typeIcon = null;

                        if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase(this.context.getString(R.string.default_left_panel_image_content_type))) {
                            lastMessageString = "   ".concat(this.context.getString(R.string.default_left_panel_image_text));
                            typeIcon = this.context.getResources().getDrawable(R.drawable.default_left_panel_image_icon);
                        } else { //if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase("TEXT")) {
                            lastMessageString = " ".concat(lastMessage.getMessageContent().getContent());
                        }

                        Drawable directionImg = this.context.getResources().getDrawable( (lastMessage.getUser().isMe()) ? R.drawable.default_left_panel_last_message_outgoing_icon : R.drawable.default_left_panel_last_message_incoming_icon );
                        directionImg.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                        if (typeIcon != null) {
                            typeIcon.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                            typeIcon.setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);
                        }

                        LastMessage = new SpannableString(lastMessageString);

                        if (typeIcon != null) {
                            LastMessage.setSpan(new ImageSpan(typeIcon,ImageSpan.ALIGN_BOTTOM),1,2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        LastMessage.setSpan(new ImageSpan(directionImg,ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    ((ChatViewHolder) holder).chatLastMessageTimestamp.setVisibility(View.VISIBLE);
                    ((ChatViewHolder) holder).chatPendingMessagesNumber.setVisibility(View.INVISIBLE);

                    ((ChatViewHolder) holder).profileType = Profile.ProfileType.Private;
                    ((ChatViewHolder) holder).hiveName = null;

                    ((ChatViewHolder) holder).chatImage.setOnClickListener(((ChatViewHolder) holder).thumbnailClickListener);
                    ((ChatViewHolder) holder).chatImage.setClickable(true);
                    break;
                case PRIVATE_GROUP:
                    ((ChatViewHolder) holder).chatHiveImage.setVisibility(View.GONE);
                    ((ChatViewHolder) holder).chatTypeImage.setImageResource(R.drawable.pestanha_chats_group);
                    ((ChatViewHolder) holder).chatImage.setImageResource(R.drawable.chats_users_online);
                    if (((Chat) item).getName() != null)
                        GroupName = ((Chat) item).getName();
                    else
                        for (User user : ((Chat) item).getMembers())
                            if (!user.isMe()) {
                                if ((user.getUserPrivateProfile() != null) && (user.getUserPrivateProfile().getShowingName() != null))
                                    GroupName += ((GroupName.isEmpty()) ? "" : ", ") + user.getUserPrivateProfile().getFirstName();
                                else
                                    user.UserLoaded.add(new EventHandler<EventArgs>(this, "OnAddItem", EventArgs.class));
                            }
                    if (lastMessage != null) {
                        String lastMessageString = "";
                        Drawable typeIcon = null;

                        if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase(this.context.getString(R.string.default_left_panel_image_content_type))) {
                            lastMessageString = "   ".concat(this.context.getString(R.string.default_left_panel_image_text));
                            typeIcon = this.context.getResources().getDrawable(R.drawable.default_left_panel_image_icon);
                        } else { //if (lastMessage.getMessageContent().getContentType().equalsIgnoreCase("TEXT")) {
                            lastMessageString = " ".concat(lastMessage.getMessageContent().getContent());
                        }

                        Drawable directionImg = this.context.getResources().getDrawable( (lastMessage.getUser().isMe()) ? R.drawable.default_left_panel_last_message_outgoing_icon : R.drawable.default_left_panel_last_message_incoming_icon );
                        directionImg.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                        if (typeIcon != null) {
                            typeIcon.setBounds(0, 0, ((ChatViewHolder) holder).chatLastMessage.getLineHeight(), ((ChatViewHolder) holder).chatLastMessage.getLineHeight());
                            typeIcon.setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);
                        }

                        LastMessage = new SpannableString(lastMessageString);

                        if (typeIcon != null) {
                            LastMessage.setSpan(new ImageSpan(typeIcon,ImageSpan.ALIGN_BOTTOM),1,2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }

                        LastMessage.setSpan(new ImageSpan(directionImg,ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    ((ChatViewHolder) holder).chatLastMessageTimestamp.setVisibility(View.VISIBLE);
                    ((ChatViewHolder) holder).chatPendingMessagesNumber.setVisibility(View.INVISIBLE);

                    ((ChatViewHolder) holder).chatImage.setOnClickListener(null);
                    ((ChatViewHolder) holder).chatImage.setClickable(false);
                    break;
                default:
                    return null;
            }

            ((ChatViewHolder) holder).chatLastMessageTimestamp.setText(LastMessageTimestamp);
            ((ChatViewHolder) holder).chatName.setText(((GroupName == null) || (GroupName.isEmpty())) ? "No chat name" : GroupName);
            ((ChatViewHolder) holder).chatLastMessage.setText(LastMessage);

            ((ChatViewHolder)holder).chatItem.setTag(R.id.BO_Chat,item);
        } else if (type == context.getResources().getInteger(R.integer.LeftPanel_ListKind_Mates)) {
            ((FriendViewHolder)holder).setFriend((User)item);
        }

        return convertView;
    }

    private void openProfile(User user, Profile.ProfileType profileType, String hiveName) {
        if (user != null)
            ((Main) context).OpenWindow(new Profile(context, user, profileType, hiveName));
    }

    private abstract class ViewHolder {
    }

    private class ChatViewHolder extends ViewHolder {
        public RelativeLayout chatItem;
        public TextView chatName;
        public TextView chatLastMessage;
        public ImageView chatImage;
        public ImageView chatHiveImage;
        public ImageView chatTypeImage;
        public TextView chatLastMessageTimestamp;
        public TextView chatPendingMessagesNumber;

        public User user;
        public String hiveName;
        public Profile.ProfileType profileType;

        public View.OnClickListener thumbnailClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((user != null) && (profileType != null))
                    openProfile(user, profileType, hiveName);
            }
        };

        public void loadHiveImage(Object sender, EventArgs eventArgs) {
            if (!(sender instanceof Image)) return;

            final Image image = (Image) sender;
            final ChatViewHolder thisViewHolder = this;

            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InputStream is = image.getImage(Image.ImageSize.small, 0);
                    if (is != null) {
                        chatHiveImage.setImageBitmap(BitmapFactory.decodeStream(is));
                        try {
                            is.reset();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    image.OnImageLoaded.remove(new EventHandler<EventArgs>(thisViewHolder, "loadHiveImage", EventArgs.class));
                    //image.freeMemory();
                }
            });
        }

        public void loadChatImage(Object sender, EventArgs eventArgs) {
            if (!(sender instanceof Image)) return;

            final Image image = (Image) sender;
            final ChatViewHolder thisViewHolder = this;

            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InputStream is = image.getImage(Image.ImageSize.medium, 0);
                    if (is != null) {
                        chatImage.setImageBitmap(BitmapFactory.decodeStream(is));
                        try {
                            is.reset();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    image.OnImageLoaded.remove(new EventHandler<EventArgs>(thisViewHolder, "loadChatImage", EventArgs.class));
                    //image.freeMemory();
                }
            });
        }
    }

    private class FriendViewHolder extends ViewHolder {
        private View cardView;
        private User cardFriend;

        private ImageView friendImage;

        private TextView friendFullName;
        private TextView friendNickname;
        private TextView friendStatusMsg;

        public void setCardView(View cardView) {
            if (this.cardView != cardView) {
                this.cardView = cardView;

                if (this.cardView != null) {
                    this.friendImage = ((ImageView) cardView.findViewById(R.id.left_panel_friend_list_item_image));
                    this.friendFullName = ((TextView) cardView.findViewById(R.id.left_panel_friend_list_item_full_name));
                    this.friendNickname = ((TextView) cardView.findViewById(R.id.left_panel_friend_list_item_nickname));
                    this.friendStatusMsg = ((TextView) cardView.findViewById(R.id.left_panel_friend_list_item_status));

                    if (this.cardFriend != null)
                        this.updateData();
                } else {
                    this.friendImage = null;
                    this.friendFullName = null;
                    this.friendNickname = null;
                    this.friendStatusMsg = null;
                }
            }
        }

        public void setFriend(User friend) {
            if (this.cardFriend != friend) {
                this.cardFriend = friend;

                if ((this.cardFriend != null) && (this.cardView != null))
                    this.updateData();
            }
        }

        public FriendViewHolder() {
            this(null, null);
        }

        public FriendViewHolder(View cardView) {
            this(cardView, null);
        }

        public FriendViewHolder(User friend) {
            this(null, friend);
        }

        public FriendViewHolder(View cardView, User friend) {
            this.setCardView(cardView);
            this.setFriend(friend);
        }

        private View.OnClickListener onCardClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onImageClickListener.onClick(v);
                ((Main) context).OpenWindow(new MainChat(context, null, cardFriend));
            }
        };

        private View.OnClickListener onImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile(cardFriend, Profile.ProfileType.Private, null);
            }
        };

        private void updateData() {
            if ((this.cardFriend.getUserPrivateProfile() == null) || (this.cardFriend.getUserPrivateProfile().getLoadedProfileLevel().ordinal() < ProfileLevel.Basic.ordinal())) {
                this.cardFriend.UserLoaded.add(new EventHandler<EventArgs>(this, "onUserLoaded", EventArgs.class));
                this.cardFriend.loadProfile(ProfileType.PRIVATE, ProfileLevel.Basic);
                return;
            }
            if ((this.cardFriend.getUserPublicProfile() == null) || (this.cardFriend.getUserPublicProfile().getLoadedProfileLevel().ordinal() < ProfileLevel.Basic.ordinal())) {
                this.cardFriend.UserLoaded.add(new EventHandler<EventArgs>(this, "onUserLoaded", EventArgs.class));
                this.cardFriend.loadProfile(ProfileType.PUBLIC, ProfileLevel.Basic);
                return;
            }
            this.friendFullName.setText(this.cardFriend.getUserPrivateProfile().getShowingName());
            this.friendNickname.setText(context.getText(R.string.public_username_identifier_character).toString().concat(this.cardFriend.getUserPublicProfile().getPublicName()));

            String statusMessage = null;

            if (this.cardFriend.getUserPrivateProfile().getStatusMessage() != null)
                statusMessage = this.cardFriend.getUserPrivateProfile().getStatusMessage();

            if ((statusMessage == null) || (statusMessage.isEmpty())) {
                statusMessage = context.getString(R.string.profile_default_private_status_message);
            }
            this.friendStatusMsg.setText("\"".concat(statusMessage).concat("\""));

            if ((this.cardFriend.getUserPrivateProfile().getSex() != null) && (this.cardFriend.getUserPrivateProfile().getSex().equalsIgnoreCase("female")))
                this.friendImage.setImageResource(R.drawable.default_profile_image_female);
            else
                this.friendImage.setImageResource(R.drawable.default_profile_image_male);


            if (this.cardFriend.getUserPrivateProfile().getProfileImage() != null) {
                this.cardFriend.getUserPrivateProfile().getProfileImage().OnImageLoaded.add(new EventHandler<EventArgs>(this, "onImageLoaded", EventArgs.class));
                this.cardFriend.getUserPrivateProfile().getProfileImage().loadImage(Image.ImageSize.medium, 0);
            }

            this.friendImage.setOnClickListener(onImageClickListener);
            this.cardView.setOnClickListener(onCardClickListener);
        }

        public void onUserLoaded(Object sender, EventArgs eventArgs) {
            this.cardFriend.UserLoaded.remove(new EventHandler<EventArgs>(this, "onUserLoaded", EventArgs.class));
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateData();
                }
            });
        }

        public void onImageLoaded(Object sender, EventArgs eventArgs) {
            if (!(sender instanceof Image)) return;

            final Image image = (Image) sender;
            final FriendViewHolder thisViewHolder = this;

            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InputStream is = image.getImage(Image.ImageSize.medium, 0);
                    if (is != null) {
                        friendImage.setImageBitmap(BitmapFactory.decodeStream(is));
                        try {
                            is.reset();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    image.OnImageLoaded.remove(new EventHandler<EventArgs>(thisViewHolder, "onImageLoaded", EventArgs.class));
                    //image.freeMemory();
                }
            });
        }
    }


}
