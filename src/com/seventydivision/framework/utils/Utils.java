package com.seventydivision.framework.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.android.Util;
import com.seventydivision.framework.BuildConfig;
import com.seventydivision.framework.fragments.InjectedResourceFragment;
import com.seventydivision.framework.models.BaseModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by andreaascari on 05/02/14.
 */
public class Utils {

    public static final java.lang.String TAG = Utils.class.getSimpleName();

    @SuppressWarnings("unchecked")
    public static <T extends BaseModel> Class<T> getTypeParameter(Object o) {

        boolean typeFound = false;
        Class klass = o.getClass();
        Class<T> tClass = null;
        if (BuildConfig.DEBUG)
            Log.d(Utils.class.getSimpleName(), Arrays.toString(((ParameterizedType) klass.getSuperclass().getGenericSuperclass()).getActualTypeArguments()));
        int count = 0;
        while (!typeFound) {
            count++;
            if (BuildConfig.DEBUG) Log.d(Utils.class.getSimpleName(), klass.getName());
            try {
                if (BuildConfig.DEBUG) Log.d(TAG, "Trying to get parameterizedtype: " + ((ParameterizedType) klass.getGenericSuperclass()).toString());

                ParameterizedType parameterizedType = (ParameterizedType) klass.getGenericSuperclass();
                Type[] actualTypes = parameterizedType.getActualTypeArguments();
                tClass = (Class<T>) actualTypes[0];
                typeFound = true;
            } catch (ClassCastException e) {
                if (klass.getSuperclass() != null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "No type parameters for this class, get its superclass: " + klass.getSuperclass().toString());

                    klass = klass.getSuperclass();
                } else {
                    if (BuildConfig.DEBUG) Log.d(TAG, "No super class for this class["+klass.toString()+"], return");
                    typeFound = true;
                }
            }

            if (count == 10)
                typeFound = true;
        }

        return tClass;

    }

    public static class Views {

        public static void hideKeyBoard(Context context, View view) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        public static void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                return;
            }
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
            int totalHeight = 0;
            View view = null;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                view = listAdapter.getView(i, view, listView);
                if (i == 0) {
                    view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += view.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
            listView.requestLayout();
        }


        public static void showKeyboard(final Context context, final View view) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
//                imm.showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);


        }


        @SuppressWarnings("deprecation")
        public static void setBackground(View v, Drawable dw) {
            if (Build.VERSION.SDK_INT >= 16) {
                v.setBackground(dw);
            } else {
                v.setBackgroundDrawable(dw);
            }
        }

        public static void showLongToast(Activity activity, int resource) {
            Toast.makeText(activity, activity.getString(resource), Toast.LENGTH_LONG).show();
        }

    }


    public static java.lang.String getLoremPixel(int w, int h, boolean g) {
        java.lang.String loremPixelEndpoint = "http://lorempixel.com/";
        if (g) loremPixelEndpoint += "g/";
        return loremPixelEndpoint + w + "/" + h + "/?" + Calendar.getInstance().getTimeInMillis() + Math.random();
    }

    /**
     * Created by andreaascari on 06/05/14.
     */
    public static class String {

        public static java.lang.String capitalize(java.lang.String string) {
            return string.substring(0, 1).toUpperCase() + string.substring(1);
        }

        public static java.lang.String buildUrl(java.lang.String host, java.lang.String[] path) {
            java.lang.String[] parts = new java.lang.String[path.length + 1];
            if (host.endsWith("/")) {
                host = host.substring(0, host.length() - 1);
            }
            parts[0] = host;
            System.arraycopy(path, 0, parts, 1, path.length);
            return join(parts, "/");
        }

        protected static java.lang.String join(java.lang.String[] s, java.lang.String glue) {
            int k = s.length;
            if (k == 0) return "";
            StringBuilder out = new StringBuilder();
            out.append(s[0]);
            for (int x = 1; x < k; ++x)
                out.append(glue).append(s[x]);
            return out.toString();
        }

    }

    public static class API {


        public static boolean isGreatEqualsThan(int targetSdk) {
            return Build.VERSION.SDK_INT >= targetSdk;
        }
    }

    public static class Assets {

        private static final java.lang.String S3_ENDPOINT = "http://gift-app-uploads.s3.amazonaws.com";

        public static java.lang.String defaultGiftImageUrl() {
            return S3_ENDPOINT + "/default-gift-image@2x.png";
        }
    }
}
