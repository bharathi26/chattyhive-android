package com.chattyhive.chattyhive;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.chattyhive.backend.Controller;
import com.chattyhive.backend.businessobjects.Hive;
import com.chattyhive.backend.businessobjects.Mate;
import com.chattyhive.backend.util.events.ChannelEventArgs;
import com.chattyhive.backend.util.events.EventArgs;
import com.chattyhive.backend.util.events.EventHandler;

import java.util.ArrayList;

/**
 * Created by Jonathan on 7/03/14.
 */
public class LeftPanel {
    Context context;

    LeftPanelListAdapter leftPanelListAdapter;
    TextView emptyMessage;

    Button chats;
    Button hives;
    Button mates;

    ViewSwitcher view_switcher;

    Boolean showingEmpty;

    public LeftPanel(Context activity) {
        this.context = activity;

        this.InitializeComponent();
    }

    private void InitializeComponent() {
        // Here we set the components to their respective elements.
        chats = (Button)((Activity)this.context).findViewById(R.id.left_panel_chats_button);
        hives = (Button)((Activity)this.context).findViewById(R.id.left_panel_hives_button);
        mates = (Button)((Activity)this.context).findViewById(R.id.left_panel_mates_button);

        view_switcher = (ViewSwitcher)((Activity)this.context).findViewById(R.id.left_panel_empty_list_view_switcher);

        showingEmpty = false;
        emptyMessage = (TextView)((Activity)this.context).findViewById(R.id.left_panel_empty_list_message);

        SetButtonSelected(chats, true, this.context.getResources().getString(R.string.left_panel_chats_button), R.drawable.pestanhas_panel_izquierdo_chats);
        SetButtonSelected(hives,false,this.context.getResources().getString(R.string.left_panel_hives_button),R.drawable.pestanhas_panel_izquierdo_hives_blanco);
        SetButtonSelected(mates,false,this.context.getResources().getString(R.string.left_panel_mates_button),R.drawable.pestanhas_panel_izquierdo_users_blanco);

        chats.setOnClickListener(left_panel_tab_button_click);
        hives.setOnClickListener(left_panel_tab_button_click);
        mates.setOnClickListener(left_panel_tab_button_click);

        this.leftPanelListAdapter = new LeftPanelListAdapter(((Activity)this.context), ((Main)this.context)._controller.getHives(),new ArrayList(),new ArrayList<Mate>());
        ((ListView)((Activity)this.context).findViewById(R.id.left_panel_element_list)).setAdapter(this.leftPanelListAdapter);

        try {
            ((Main)this.context)._controller.SubscribeToHivesListChange(new EventHandler<EventArgs>(leftPanelListAdapter, "OnAddItem", EventArgs.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        };

        this.leftPanelListAdapter.SetOnClickListener(OpenChat);

        this.leftPanelListAdapter.SetVisibleList(R.id.LeftPanel_ListKind_Chats);
        emptyMessage.setText(R.string.left_panel_chats_empty_list);
        if (this.leftPanelListAdapter.getCount() == 0) {
            view_switcher.showNext();
            showingEmpty = true;
        }
  }

    protected View.OnClickListener left_panel_tab_button_click = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.left_panel_chats_button:
                    SetButtonSelected(chats, true, v.getContext().getResources().getString(R.string.left_panel_chats_button), R.drawable.pestanhas_panel_izquierdo_chats);
                    SetButtonSelected(hives,false,v.getContext().getResources().getString(R.string.left_panel_hives_button),R.drawable.pestanhas_panel_izquierdo_hives_blanco);
                    SetButtonSelected(mates,false,v.getContext().getResources().getString(R.string.left_panel_mates_button),R.drawable.pestanhas_panel_izquierdo_users_blanco);
                    leftPanelListAdapter.SetVisibleList(R.id.LeftPanel_ListKind_Chats);
                    emptyMessage.setText(R.string.left_panel_chats_empty_list);
                    if ((showingEmpty) && (leftPanelListAdapter.getCount() > 0)) {
                        showingEmpty = false;
                        view_switcher.showPrevious();
                    } else if ((!showingEmpty) && (leftPanelListAdapter.getCount() == 0)) {
                        showingEmpty = true;
                        view_switcher.showNext();
                    }
                    break;
                case R.id.left_panel_hives_button:
                    SetButtonSelected(chats,false, v.getContext().getResources().getString(R.string.left_panel_chats_button), R.drawable.pestanhas_panel_izquierdo_chats_blanco);
                    SetButtonSelected(hives, true,v.getContext().getResources().getString(R.string.left_panel_hives_button),R.drawable.pestanhas_panel_izquierdo_hives);
                    SetButtonSelected(mates,false,v.getContext().getResources().getString(R.string.left_panel_mates_button),R.drawable.pestanhas_panel_izquierdo_users_blanco);
                    leftPanelListAdapter.SetVisibleList(R.id.LeftPanel_ListKind_Hives);
                    emptyMessage.setText(R.string.left_panel_hives_empty_list);
                    if ((showingEmpty) && (leftPanelListAdapter.getCount() > 0)) {
                        showingEmpty = false;
                        view_switcher.showPrevious();
                    } else if ((!showingEmpty) && (leftPanelListAdapter.getCount() == 0)) {
                        showingEmpty = true;
                        view_switcher.showNext();
                    }
                    break;
                case R.id.left_panel_mates_button:
                    SetButtonSelected(chats,false, v.getContext().getResources().getString(R.string.left_panel_chats_button), R.drawable.pestanhas_panel_izquierdo_chats_blanco);
                    SetButtonSelected(hives,false,v.getContext().getResources().getString(R.string.left_panel_hives_button),R.drawable.pestanhas_panel_izquierdo_hives_blanco);
                    SetButtonSelected(mates, true,v.getContext().getResources().getString(R.string.left_panel_mates_button),R.drawable.pestanhas_panel_izquierdo_users);
                    leftPanelListAdapter.SetVisibleList(R.id.LeftPanel_ListKind_Mates);
                    emptyMessage.setText(R.string.left_panel_mates_empty_list);
                    if ((showingEmpty) && (leftPanelListAdapter.getCount() > 0)) {
                        showingEmpty = false;
                        view_switcher.showPrevious();
                    } else if ((!showingEmpty) && (leftPanelListAdapter.getCount() == 0)) {
                        showingEmpty = true;
                        view_switcher.showNext();
                    }
                    break;
            }
        }
    };

    private void SetButtonSelected(Button button, Boolean selected, String text, int drawable) {
        Spannable buttonLabel = new SpannableString(" ");
        if (selected) {
            buttonLabel = new SpannableString(" ".concat(text));
            button.setTextColor(Color.parseColor("#3a3a3a"));
            button.setBackgroundColor(Color.parseColor("#F8F8F8"));
        } else {
            button.setTextColor(Color.parseColor("#a1a1a1"));
            button.setBackgroundColor(Color.parseColor("#292929"));
        }
        buttonLabel.setSpan(new ImageSpan(this.context.getApplicationContext(),drawable,
                ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        if (selected) {
            buttonLabel.setSpan(new SpannableString(text),1,text.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        button.setText(buttonLabel);
    }

    protected View.OnClickListener OpenChat = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (leftPanelListAdapter.GetVisibleList()) {
                case R.id.LeftPanel_ListKind_Hives:

                    if (((Main)context).ActiveLayoutID == R.layout.main_panel_chat_layout) {
                        ((Main)context)._controller.Leave((String)((Activity)context).findViewById(R.id.main_panel_chat_name).getTag());
                    }

                    Hive h = ((Hive)v.getTag(R.id.BO_Hive));
                    View chatView = ((Main)context).ShowLayout(R.layout.main_panel_chat_layout);
                    ((TextView)chatView.findViewById(R.id.main_panel_chat_name)).setText(h.getName());
                    chatView.findViewById(R.id.main_panel_chat_name).setTag(h.getNameURL());

                    chatView.findViewById(R.id.main_panel_chat_menu_icon).setOnClickListener(((Main)context).menuIcon_ClickListener);
                    chatView.findViewById(R.id.main_panel_chat_icon).setOnClickListener(((Main)context).appIcon_ClickListener);
                    ((Main)context).appIcon_ClickListener.onClick(chatView.findViewById(R.id.main_panel_chat_icon));

                    MainChat mainChat = new MainChat(context);
                    chatView.findViewById(R.id.main_panel_chat_send_icon).setOnClickListener(mainChat.send_button_click);

                    ChatListAdapter chatListAdapter = new ChatListAdapter(((Activity)context),((Main)context)._controller.getMessages(h.getNameURL()), R.id.MainPanelChat_ListKind_Hive);
                    ((ListView)((Activity)context).findViewById(R.id.main_panel_chat_message_list)).setAdapter(chatListAdapter);
                    try {
                        ((Main)context)._controller.SubscribeChannelEventHandler(new EventHandler<ChannelEventArgs>(chatListAdapter, "OnAddItem", ChannelEventArgs.class));
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    };

                    ((Main)context)._controller.Join(h.getNameURL());

                    break;
                case R.id.LeftPanel_ListKind_Chats:
                    break;
                case R.id.LeftPanel_ListKind_Mates:
                    break;
            }
        }
    };
}