package com.seventydivision.framework.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.facebook.Session;
import com.seventydivision.framework.persist.PersistentPreferences;
import com.seventydivision.framework.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;


/**
 * Created by andreaascari on 22/01/14.
 */
@EActivity
public abstract class MainActivity extends FragmentActivity {

    private Session mFbSession;
    protected PersistentPreferences mPrefs;

    private boolean mLaunching;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFacebookSession(false);

    }

    protected void initFacebookSession(boolean b) {
        mFbSession = new Session(this);
    }


    @AfterViews
    public void afterViews() {
        onViewInjected();
    }


    public abstract void onViewInjected();


    protected void enableBackButton() {
        if (getActionBar() != null) {
            if (Utils.API.isGreatEqualsThan(24))
                getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    protected void launch(Class<?> a) {
        mLaunching = true;
        startActivity(new Intent(this, a));
        mLaunching = false;

    }

    protected void launchForResult(Class<?> a, int requestCode) {
        startActivityForResult(new Intent(this, a), requestCode);
    }


    protected void updateFbSession() {
        if (mFbSession.isClosed() ||
                mFbSession.getAccessToken().isEmpty()) {
            mFbSession = new Session(this);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public boolean isLaunching() {
        return mLaunching;
    }


    public PersistentPreferences getPrefs() {
        return mPrefs;
    }
}