package com.seventydivision.framework.fragments;

import android.os.Bundle;

import com.seventydivision.framework.client.AsyncResourceHandler;
import com.seventydivision.framework.interfaces.OnFetchResourceInterface;
import com.seventydivision.framework.models.BaseModel;
import com.seventydivision.framework.utils.Utils;


/**
 * Created by andreaascari on 01/07/14.
 */
public abstract class InjectedResourceFragment<T extends BaseModel> extends InjectedFragment implements OnFetchResourceInterface<T> {

    private T mResource;

    private AsyncResourceHandler<T> asyncResourceHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler();
        fetchData();
    }

    @SuppressWarnings("unchecked")
    protected void initHandler() {
        asyncResourceHandler = new AsyncResourceHandler<T>((Class<T>) Utils.getTypeParameter(this)) {

            @Override
            public void onSuccess(T res) {
                setResource(res);
            }

        };
    }


    protected void fetchData() {
        fetchData(asyncResourceHandler);
    }

    @Override
    protected void canPopulateView() {
        populateView(mResource);
    }

    protected void setResource(T resource) {
        this.mResource = resource;
        fetchCompleted(true);
    }
}
