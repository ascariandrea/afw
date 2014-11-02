package com.ascariandrea.afw.fragments;

import android.os.Bundle;
import android.util.Log;


import com.ascariandrea.afw.client.AsyncCollectionHandler;
import com.ascariandrea.afw.interfaces.OnFetchCollectionInterface;
import com.ascariandrea.afw.models.Model;
import com.ascariandrea.afw.utils.Utils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EFragment;

import java.lang.reflect.Field;

import java.util.List;

/**
 * Created by andreaascari on 01/07/14.
 */
@EFragment
public abstract class InjectedListFragment<T extends Model> extends InjectedFragment implements OnFetchCollectionInterface<T> {

    protected static final String TAG = InjectedListFragment.class.getSimpleName();

    protected List<T> mCollection;

    private AsyncCollectionHandler<T> asyncCollectionHandler;
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
        asyncCollectionHandler = new AsyncCollectionHandler<T>(getModelExtensionClassPluralName(), (Class<T>) Utils.getTypeParameter(this)) {
            @Override
            public void onSuccess(List<T> res) {
                mCollection = res;
                fetchCompleted(true);
            }
        };
    }


    private String getModelExtensionClassPluralName() {
        Class<T> klass = Utils.getTypeParameter(this);
        try {
            Field m = klass.getDeclaredField("PLURAL_NAME");
            m.setAccessible(true);
            return (String) m.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }


    protected void fetchData() {
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
        if (mCollection == null)
            fetchData();
        else if (mNeedRepopulate)
            populateViewAgain();
        super.onResume();
    }

    protected void populateViewAgain() {
        populateView(mCollection);
    }


}