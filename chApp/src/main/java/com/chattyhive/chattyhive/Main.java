package com.chattyhive.chattyhive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.chattyhive.backend.Controller;
import com.chattyhive.backend.StaticParameters;

import com.chattyhive.backend.businessobjects.Chats.Chat;
import com.chattyhive.backend.businessobjects.Chats.Hive;
import com.chattyhive.backend.contentprovider.AvailableCommands;
import com.chattyhive.backend.contentprovider.DataProvider;
import com.chattyhive.chattyhive.framework.OSStorageProvider.ChatLocalStorage;
import com.chattyhive.chattyhive.framework.OSStorageProvider.CookieStore;
import com.chattyhive.chattyhive.framework.OSStorageProvider.HiveLocalStorage;
import com.chattyhive.chattyhive.framework.OSStorageProvider.LoginLocalStorage;
import com.chattyhive.chattyhive.framework.OSStorageProvider.MessageLocalStorage;
import com.chattyhive.chattyhive.framework.OSStorageProvider.UserLocalStorage;

import com.chattyhive.chattyhive.backgroundservice.CHService;

import com.chattyhive.chattyhive.framework.CustomViews.ViewGroup.FloatingPanel;
import com.chattyhive.chattyhive.framework.Util.ViewPair;

import java.util.HashMap;
import java.util.Map;


public class Main extends Activity {
    static final int OP_CODE_LOGIN = 1;
    static final int OP_CODE_EXPLORE = 2;
    static final int OP_CODE_NEW_HIVE = 3;

    FloatingPanel floatingPanel;

    Controller controller;

    Home home;

    LeftPanel leftPanel;
    RightPanel2 rightPanel;

    HashMap <Integer, Window> viewStack;
    int lastOpenHierarchyLevel;

    void OpenWindow(Window window) {
        OpenWindow(window,window.getHierarchyLevel());
    }
    void OpenWindow(Window window,Integer hierarchyLevel) {
        if (hierarchyLevel > (this.lastOpenHierarchyLevel+1))
            throw new IllegalArgumentException("Expected at most one level over the last open hierarchy level");

        if (this.lastOpenHierarchyLevel > -1) {
            if (hierarchyLevel < this.lastOpenHierarchyLevel) {
                for (int i = this.lastOpenHierarchyLevel; i > hierarchyLevel; i--) {
                    this.viewStack.get(i).Close();
                    this.viewStack.remove(i);
                }
            } else if (hierarchyLevel == this.lastOpenHierarchyLevel) {
                this.viewStack.get(this.lastOpenHierarchyLevel).Close();
            } else if (hierarchyLevel > this.lastOpenHierarchyLevel) {
                this.viewStack.get(this.lastOpenHierarchyLevel).Hide();
            }
        }

        if (hierarchyLevel != window.getHierarchyLevel())
            window.setHierarchyLevel(hierarchyLevel);

        this.viewStack.put(hierarchyLevel,window);

        this.lastOpenHierarchyLevel = hierarchyLevel;

        if ((!window.hasContext()) || (window.context != this))
            window.setContext(this);

        window.Open();
    }

    void Close() {
        if (this.lastOpenHierarchyLevel >= 0)
            this.viewStack.get(this.lastOpenHierarchyLevel).Close();

        this.lastOpenHierarchyLevel--;

        if (this.lastOpenHierarchyLevel >= 0)
            this.viewStack.get(this.lastOpenHierarchyLevel).Show();

    }

    protected ViewPair ShowLayout (int layoutID, int actionBarID) {
        FrameLayout mainPanel = ((FrameLayout)findViewById(R.id.mainCenter));
        FrameLayout mainActionBar = ((FrameLayout)findViewById(R.id.actionCenter));
        mainPanel.removeAllViews();
        mainActionBar.removeAllViews();
        View actionBar = LayoutInflater.from(this).inflate(actionBarID,mainActionBar,true);
        View mainView = LayoutInflater.from(this).inflate(layoutID, mainPanel, true);
        ViewPair actualView = new ViewPair(mainView,actionBar);

        return actualView;
    }
    protected View ChangeActionBar (int actionBarID) {
        FrameLayout mainActionBar = ((FrameLayout)findViewById(R.id.actionCenter));
        mainActionBar.removeAllViews();
        View actionBar = LayoutInflater.from(this).inflate(actionBarID,mainActionBar,true);

        return actionBar;
    }

    protected void ShowHome() {
        if (this.home == null)
            this.home = new Home(this);
        else if (!this.home.hasContext())
            this.home.setContext(this);

        OpenWindow(this.home);
    }

    protected void ShowChats() {
        this.leftPanel.OpenChats();
        floatingPanel.openLeft();
    }

    protected void ShowHives() {
        this.leftPanel.OpenHives();
        floatingPanel.openLeft();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Log.w("Main","onCreate..."); //DEBUG
        Object[] LocalStorage = {LoginLocalStorage.getLoginLocalStorage(), ChatLocalStorage.getGroupLocalStorage(), HiveLocalStorage.getHiveLocalStorage(), MessageLocalStorage.getMessageLocalStorage(), UserLocalStorage.getUserLocalStorage()};
        Controller.Initialize(new CookieStore(),LocalStorage);

        this.controller = Controller.GetRunningController(com.chattyhive.chattyhive.framework.OSStorageProvider.LocalStorage.getLocalStorage());

        this.viewStack = new HashMap<Integer, Window>();
        this.lastOpenHierarchyLevel = -1;

        this.leftPanel = new LeftPanel(this);

        if (savedInstanceState == null)
            this.ShowHome();

        this.rightPanel = new RightPanel2(this);

        try {
            Controller.bindApp(this.getClass().getMethod("hasToLogin"),this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        this.ConnectService();

        if (savedInstanceState != null)
            Restore(savedInstanceState);
    }

    private void Restore(Bundle savedInstanceState) {
        this.home = ((Home)savedInstanceState.getSerializable("Home"));
        int lastOpenHierarchyLevel = savedInstanceState.getInt("lastOpenHierarchyLevel");
        for (int i = 0; i <= lastOpenHierarchyLevel; i++)
            OpenWindow((Window)savedInstanceState.getSerializable(String.format("viewStackEntry_%d",i)),i);
    }

    public void hasToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, OP_CODE_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OP_CODE_LOGIN:
                    if (resultCode != RESULT_OK) {
                        Controller.DisposeRunningController();
                        this.finish();
                    }
                break;
            case OP_CODE_EXPLORE:
                    if (resultCode == RESULT_OK) {
                        String nameURL = null;
                        if ((data != null) && (data.hasExtra("NameURL")))
                            nameURL = data.getStringExtra("NameURL");

                        if ((nameURL != null) && (!nameURL.isEmpty())) {
                            Hive h = Hive.getHive(nameURL);
                            Chat c = null;
                            if (h != null)
                                c = h.getPublicChat();

                            if (c != null)
                                new MainChat(this, c);
                            else
                                this.ShowHives();
                        } else
                            this.ShowHives();
                    }
                    break;
            case OP_CODE_NEW_HIVE:
                if(resultCode == RESULT_OK){
                        this.ShowHives();
                    }
                break;
        }
    }

    private void ConnectService() {
        if (StaticParameters.BackgroundService) {
            Context context = this.getApplicationContext();
            context.startService(new Intent(context, CHService.class)); //If not, then start it.}
        }
    }

    @Override
    public void onDestroy() {
        Controller.unbindApp();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected View.OnClickListener appIcon_ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (floatingPanel.isOpen())
                floatingPanel.close();
            else
                floatingPanel.openLeft();
    } };

    protected View.OnClickListener menuIcon_ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (floatingPanel.isOpen())
                floatingPanel.close();
            else
                floatingPanel.openRight();
        }
    };

    public void setPanelBehaviour() {
        floatingPanel = ((FloatingPanel)findViewById(R.id.FloatingPanel));

        ImageButton appIcon = (ImageButton)findViewById(R.id.appIcon);
        appIcon.setOnClickListener(this.appIcon_ClickListener);

        ImageButton menuIcon = (ImageButton)findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(this.menuIcon_ClickListener);
    }

    protected View.OnClickListener explore_button_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(),Explore.class);
            startActivityForResult(intent, OP_CODE_EXPLORE);
        }
    };

    protected View.OnClickListener new_hive_button_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(),NewHive.class);
            startActivityForResult(intent,OP_CODE_NEW_HIVE);
        }
    };

    protected View.OnClickListener logout_button_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            controller.clearUserData();
            hasToLogin();
        }
    };

    protected View.OnClickListener clear_chats_button_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            controller.clearAllChats();
        }
    };

    protected View.OnClickListener chat_sync_button_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DataProvider dataProvider = DataProvider.GetDataProvider();
            dataProvider.InvokeServerCommand(AvailableCommands.ChatList, null);
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                if ((this.lastOpenHierarchyLevel > 0) && (!floatingPanel.isOpen())) { // Tell the framework to start tracking this event.
                    findViewById(R.id.mainCenter).getKeyDispatcherState().startTracking(event, this);
                    return true;
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                findViewById(R.id.mainCenter).getKeyDispatcherState().handleUpEvent(event);
                if (event.isTracking() && !event.isCanceled() && (!floatingPanel.isOpen())) {
                    this.Close();
                    if (this.lastOpenHierarchyLevel >= 0)
                        return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("Home",home);
        outState.putInt("lastOpenHierarchyLevel",lastOpenHierarchyLevel);
        for (Map.Entry<Integer,Window> viewStackEntry : viewStack.entrySet())
            outState.putSerializable(String.format("viewStackEntry_%d",viewStackEntry.getKey()),viewStackEntry.getValue());
    }


}