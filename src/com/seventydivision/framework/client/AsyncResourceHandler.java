package com.seventydivision.framework.client;

import android.util.Log;

import com.seventydivision.framework.models.BaseModel;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AsyncResourceHandler<T extends BaseModel> extends AsyncHttpResponseHandler {

    private String TAG = AsyncResourceHandler.class.getSimpleName();
    private String wrapProperty;
    private Class<T> mType;


    public AsyncResourceHandler() {
        wrapProperty = "data";
    }

    public AsyncResourceHandler(String wp) {
        wrapProperty = wp;
    }

    public AsyncResourceHandler(Class<T> type) {
        wrapProperty = "data";
        mType = type;
    }

    @SuppressWarnings("unchecked")
    public Class<T> getTypeParameterClass() {
        if (mType != null) return mType;

        Type type = getClass().getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType) type;
        return (Class<T>) paramType.getActualTypeArguments()[0];
    }

    public void onSuccess(int i, Header[] headers, byte[] bytes) {
        onSuccess(new String(bytes));
    }

    public final void onSuccess(String json) {
        if (json != null) Log.d(TAG, json);
        try {
            JSONObject jsonResponse = new JSONObject(json);
            if (wrapProperty != null) {
                json = jsonResponse.getJSONObject(wrapProperty).toString();
            }
            onSuccess(BaseModel.fromJSON(json, getTypeParameterClass()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
        if (bytes == null)
            onFailure(throwable, new String());
        else {
            String res = new String(bytes);
            onFailure(throwable, res);
            try {
                JSONObject jsonRes = new JSONObject(res);
                int code = jsonRes.getInt("code");
                if (code != 0) {
                    onFailure(throwable, jsonRes, code);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void onFailure(Throwable t, String res) {
        Log.d(TAG, t.getStackTrace().toString());
        t.printStackTrace();
        if (res != null) {
            Log.i(TAG, res);
        }
    }

    protected void onFailure(Throwable throwable, JSONObject jsonRes, int apiCode) {

    }


    public abstract void onSuccess(T res);
}
