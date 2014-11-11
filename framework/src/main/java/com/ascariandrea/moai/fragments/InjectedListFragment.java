package com.ascariandrea.moai.fragments;


import android.util.Log;

import com.ascariandrea.moai.client.AsyncCollectionHandler;
import com.ascariandrea.moai.interfaces.OnFetchCollectionInterface;
import com.ascariandrea.moai.models.Model;
import com.ascariandrea.moai.utils.Utils;

import org.androidannotations.annotations.EFragment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Created by andreaascari on 01/07/14.
 */
@EFragment
public abstract class InjectedListFragment<T extends Model> extends InjectedFragment implements OnFetchCollectionInterface<T> {

    protected static final String TAG = InjectedListFragment.class.getSimpleName();

    protected List<T> mCollection;

    protected AsyncCollectionHandler<T> asyncCollectionHandler;
    private Class<? extends Model> mExtendedModelClass;

    private boolean mNeedRepopulate = false;


    @Override
    protected void toCallAfterCreation() {
        onCreated();
        initHandler();
        if (!mFetchDataIsDisabled)
            fetchData();
    }


    @SuppressWarnings("unchecked")
    protected void initHandler() {
        asyncCollectionHandler = new AsyncCollectionHandler<T>((Class<T>)  Utils.getTypeParameter(this)) {
            @Override
            public void onSuccess(List<T> res) {
                mCollection = res;
                fetchCompleted(true);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                mFetching = false;
            }
        };
    }


    protected void fetchData() {
        mFetching = true;
        fetchData(asyncCollectionHandler);
    }

    @Override
    protected void canPopulateView() {
        if (mCollection != null)
            populateView(mCollection);
        else
            throw new RuntimeException("Can't call populateView(mCollection) with null collection.");
    }


    protected void setModel(Class<? extends Model> extendedModel) {
        mExtendedModelClass = extendedModel;
    }

    @Override
    public void onDestroyView() {
        mNeedRepopulate = true;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mCollection = null;
        mNeedRepopulate = true;
        super.onDestroy();
    }

    @Override
    public void onResume() {

        if (!hasViewInjected())
            afterViewsInjected();

        if (mCollection == null && !mFetching && !mFetchDataIsDisabled) {
            if (getAsyncCollectionHandler() == null)
                initHandler();
            fetchData();
        } else if (mNeedRepopulate)
            populateViewAgain();
        super.onResume();
    }

    protected void populateViewAgain() {
        populateView(mCollection);
    }


    public AsyncCollectionHandler<T> getAsyncCollectionHandler() {
        return asyncCollectionHandler;
    }
}
