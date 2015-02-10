package com.ascariandrea.moai.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ascariandrea.moai.R;
import com.ascariandrea.moai.models.Model;

import net.sf.jfuzzydate.FuzzyDateFormat;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
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
    public static <T extends Model> Class<T> getTypeParameter(Object o) {
        Type[] actualTypes = new Type[0];
        boolean typeFound = false;
        Class klass = o.getClass();
        Class<T> tClass = null;

        int count = 0;
        while (!typeFound) {
            count++;

            try {
//                Log.d(TAG, klass.getName() + " type parameters: " + Arrays.toString(klass.getTypeParameters()));
                if (klass.getTypeParameters().length > 0) {
                    actualTypes = klass.getTypeParameters()[0].getBounds();
                } else {
                    ParameterizedType parameterizedType = (ParameterizedType) klass.getGenericSuperclass();
                    actualTypes = parameterizedType.getActualTypeArguments();
                }
                if (actualTypes.length > 0) {
//                    Log.d(TAG, Arrays.toString(actualTypes));
                    tClass = (Class<T>) actualTypes[0];
//                    Log.d(TAG, "class found!: " + tClass);
                    typeFound = true;
                } else {
                    //Log.d(TAG, "no type found for: " + klass.getName());
                }
            } catch (ClassCastException e) {
//                Log.d(TAG, "error casting to: " + Arrays.asList(actualTypes).toString());
                if (klass.getSuperclass() != null) {
                    klass = klass.getSuperclass();
                } else {
                    typeFound = true;
                }
            }

            if (count == 10)
                typeFound = true;
        }

        return tClass;

    }

    public static java.lang.String getLoremPixel(int w, int h, boolean g) {
        java.lang.String loremPixelEndpoint = "http://lorempixel.com/";
        if (g) loremPixelEndpoint += "g/";
        return loremPixelEndpoint + w + "/" + h + "/?" + Calendar.getInstance().getTimeInMillis() + Math.random();
    }

    public static class Color {

        public static int getTransparentColor(int color, float factor) {
            int alpha = Math.round(android.graphics.Color.alpha(color) * factor);
            int red = android.graphics.Color.red(color);
            int green = android.graphics.Color.green(color);
            int blue = android.graphics.Color.blue(color);
            return android.graphics.Color.argb(alpha, red, green, blue);
        }

        public static boolean isTooLight(int color) {

            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = (color >> 0) & 0xFF;

            double luma = 0.299 * r + 0.587 * g + 0.114 * b; // per ITU-R BT.709

            return luma > 160;

        }

    }

    public static class Views {

        private static boolean mKeyboardOpened;

        public static void showKeyboard(final Context context, final View view) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
            mKeyboardOpened = true;
            //imm.showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT);

        }

        public static void hideKeyBoard(final Context context, final View view) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    mKeyboardOpened = false;
                }
            }, 300);
        }


        public static void setColorToMany(int color, TextView... views) {
            for (TextView v : views) {
                v.setTextColor(color);
            }
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

        public static void setExpandableListViewHeightBasedOnContent(ExpandableListView expandableListView) {
            ExpandableListAdapter expandableListAdapter = expandableListView.getExpandableListAdapter();

            if (expandableListAdapter == null)
                return;

            int desiredWidth = View.MeasureSpec.makeMeasureSpec(expandableListView.getWidth(), View.MeasureSpec.AT_MOST);
            int totalHeight = 0;
            View view = null;
            View groupView = null;


            for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {

                groupView = expandableListAdapter.getGroupView(i, true, groupView, expandableListView);

                view = expandableListAdapter.getChildView(i, 0, false, view, expandableListView);

                if (i == 0) {
                    groupView.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                    view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
                }


                groupView.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                totalHeight += groupView.getMeasuredHeight() + view.getMeasuredHeight() * expandableListAdapter.getChildrenCount(i) + (expandableListView.getDividerHeight() * (expandableListAdapter.getChildrenCount(i) - 1));
            }
            ViewGroup.LayoutParams params = expandableListView.getLayoutParams();
            params.height = totalHeight / 2;
            Log.d(TAG, "height: " + totalHeight);
            expandableListView.setLayoutParams(params);
            expandableListView.requestLayout();
        }


        @SuppressWarnings("deprecation")
        public static void setBackground(View v, Drawable dw) {
            if (Build.VERSION.SDK_INT >= 16) {
                v.setBackground(dw);
            } else {
                v.setBackgroundDrawable(dw);
            }
        }


        public static void drawCircle(ImageView v, Canvas canvas) {
            Drawable drawable = v.getDrawable();

            if (drawable == null) {
                return;
            }

            if (v.getWidth() == 0 || v.getHeight() == 0) {
                return;
            }

            if (drawable instanceof BitmapDrawable) {

                Bitmap b = ((BitmapDrawable) drawable).getBitmap();

                Bitmap bitmap;
                if (!b.isRecycled()) {
                    bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
                } else {
                    bitmap = Bitmap.createBitmap(b);
                }


                int w = v.getWidth(), h = v.getHeight();

                Bitmap roundBitmap = Utils.Views.getCroppedBitmap(bitmap, w);
                canvas.drawBitmap(roundBitmap, 0, 0, null);
//                bitmap.recycle();
//                roundBitmap.recycle();
//                b.recycle();
            }

        }

        private static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
            Bitmap sBmp;

            if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
                float smallest = Math.min(bmp.getWidth(), bmp.getHeight());
                float factor = smallest / radius;
                sBmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() / factor), (int) (bmp.getHeight() / factor), false);
            } else {
                sBmp = bmp;
            }

            Bitmap output = Bitmap.createBitmap(radius, radius,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xffa19774;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, radius, radius);

            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(android.graphics.Color.parseColor("#BAB399"));
            canvas.drawCircle(
                    radius / 2 + 0.7f,
                    radius / 2 + 0.7f,
                    radius / 2 + 0.1f,
                    paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(sBmp, rect, rect, paint);

            return output;
        }


        public static int getDpMeasure(Context context, int i) {
            return (int) ((context.getResources().getDisplayMetrics().density * i) + 0.5f);
        }

        public static int getSpMeasure(Context context, int i) {
            return (int) ((context.getResources().getDisplayMetrics().scaledDensity * i));
        }
    }

    /**
     * Created by andreaascari on 06/05/14.
     */
    public static class String {

        public static java.lang.String capitalize(java.lang.String string) {
            if (string != null) {
                return string.substring(0, 1).toUpperCase() + string.substring(1);
            }
            return null;
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

        public static java.lang.String join(java.lang.String[] s, java.lang.String glue) {
            if (s == null) return "";
            int k = s.length;
            if (k == 0) return "";
            StringBuilder out = new StringBuilder();
            out.append(s[0]);
            for (int x = 1; x < k; ++x)
                out.append(glue).append(s[x]);
            return out.toString();
        }

        public static boolean isValidEmail(java.lang.String s) {
            return !TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches();
        }

        public static java.lang.String capitalizeWords(java.lang.String s) {
            final StringBuilder result = new StringBuilder(s.length());
            java.lang.String[] words = s.split("\\s");
            for(int i=0,l=words.length;i<l;++i) {
                if(i>0) result.append(" ");
                result.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1));

            }

            return result.toString();
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

    public static class Date {
        public static java.lang.String getFuzzyDate(java.util.Date date) {
            return FuzzyDateFormat.getInstance().formatDistance(date);
        }
    }

    public static class Dialogs {
        private static Dialog mLastDialog;


        public static AlertDialog showError(final Activity activity, java.lang.String message) {
            clearScreen();

            if (message == null)
                message = activity.getString(R.string.alert_error_message);

            return show(
                    activity,
                    activity.getString(R.string.an_error_has_occured),
                    message,
                    null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    },
                    null,
                    null,
                    null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    });
        }

        public static AlertDialog showRetry(final Context context, java.lang.String title, java.lang.String message, DialogInterface.OnClickListener clickListener) {
            return show(
                    context,
                    title,
                    message,
                    context.getString(R.string.retry),
                    clickListener,
                    null,
                    null,
                    null,
                    null
            );
        }


        public static AlertDialog showOk(Context context, java.lang.String title, java.lang.String message, java.lang.String buttonText, DialogInterface.OnClickListener neutralListener) {
            clearScreen();
            if (buttonText == null)
                buttonText = context.getString(R.string.done);

            return show(context, title, message, null, null, buttonText, neutralListener, null, null);

        }

        public static AlertDialog show(Context context, java.lang.String title, java.lang.String message, java.lang.String positiveLabel, DialogInterface.OnClickListener positiveListener, java.lang.String neutralLabel, DialogInterface.OnClickListener neutralListener, java.lang.String negativeLabel, DialogInterface.OnClickListener negativeListener) {

            clearScreen();

            if (positiveLabel == null)
                positiveLabel = context.getString(R.string.done);

            if (neutralLabel == null)
                negativeLabel = context.getString(R.string.done);

            if (negativeLabel == null)
                negativeLabel = context.getString(R.string.cancel);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message);

            if (positiveListener != null)
                dialogBuilder.setPositiveButton(positiveLabel, positiveListener);

            if (neutralListener != null)
                dialogBuilder.setNeutralButton(neutralLabel, neutralListener);

            if (negativeListener != null)
                dialogBuilder.setNegativeButton(negativeLabel, negativeListener);

            return (AlertDialog) (mLastDialog = dialogBuilder.show());

        }

        public static void showCircleProgress(Context context) {
            clearScreen();
            mLastDialog = new ProgressDialog(context);
            mLastDialog.show();
            mLastDialog.setContentView(R.layout.transparent_progress_dialog);
        }


        public static void clearScreen() {
            if (mLastDialog != null && mLastDialog.isShowing())
                mLastDialog.dismiss();
        }


        public static void lastDialog(AlertDialog alertDialog) {
            mLastDialog = alertDialog;
        }
    }

    public static class GoogleAuth {

        public static final java.lang.String OAUTH2 = "oauth2:";
        private static final java.lang.String PROFILE_EMAILS_READ_SCOPE = "https://www.googleapis.com/auth/plus.profile.emails.read";
        private static java.lang.String mOauthUrl;

        private static java.lang.String getServerOAuth2Prefix(java.lang.String serverClientId) {
            java.lang.String oauthPrefix = OAUTH2;
            if (serverClientId != null)
                oauthPrefix = oauthPrefix.concat(":server:client_id:").concat(serverClientId).concat(":api_scopes:");

            return oauthPrefix;
        }

        public static java.lang.String getServerOAuthUrl(java.lang.String serverClientId, boolean emailScope, java.lang.String... scopes) {
            return getServerOAuth2Prefix(serverClientId).concat(getScopes(emailScope, scopes));
        }

        public static java.lang.String getOAuth2Url(boolean emailScope, java.lang.String... scopes) {
            return OAUTH2.concat(getScopes(emailScope, scopes));
        }

        public static java.lang.String getScopes(boolean emailScope, java.lang.String... scopes) {
            java.lang.String scopesString = "";
            if (scopes != null) {
                if (scopes.length == 1) {
                    scopesString = scopesString.concat(scopes[0]).concat(" ");
                } else {
                    scopesString = scopesString
                            .concat(Utils.String.join(scopes, " "));
                }
            }
            if (emailScope)
                scopesString = scopesString.concat(PROFILE_EMAILS_READ_SCOPE);

            return scopesString;
        }
    }

    public static class Image {

        public static Bitmap decodeImage(Context context, Uri path, int max_width) {
            ParcelFileDescriptor parcelFileDescriptor;
            Bitmap b = null;
            try {
                parcelFileDescriptor = context.getContentResolver().openFileDescriptor(path, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                //Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, o);
                int scale = 1;
                if (o.outWidth > max_width) {
                    scale = (int) Math.pow(2, (int) Math.ceil(Math.log(max_width /
                            (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
                }
                //Decode with inSampleSize
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, o);
                return b;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        public static byte[] BitmapToByteArray(Bitmap bmp) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }
    }

    public static class Screen {

        @SuppressWarnings("deprecation")
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        public static Point getSize(WindowManager windowManager) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            if (Utils.API.isGreatEqualsThan(13)) {
                display.getSize(size);
            } else {
                size.x = display.getWidth();
                size.y = display.getHeight();
            }
            return size;
        }
    }

    public static class Location {


        public static boolean isLocationEnabled(Context context) {
            boolean locationAvailable = false;
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "gps enabled");
                locationAvailable = true;
            }

            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.d(TAG, "network enabled");
                locationAvailable = true;
            }

            return locationAvailable;
        }

        public static AlertDialog.Builder getLocationDisabledDialog(Context context, java.lang.String appName, DialogInterface.OnClickListener ignoreListener, DialogInterface.OnClickListener settingsListener) {

            return new AlertDialog.Builder(context)
                    .setTitle(Utils.String.capitalize(context.getString(R.string.location_service_disabled)))
                    .setMessage(context.getString(R.string.location_service_disabled_message, appName))
                    .setNegativeButton(Utils.String.capitalize(context.getString(R.string.ignore)), ignoreListener)
                    .setPositiveButton(Utils.String.capitalize(context.getString(R.string.settings)), settingsListener);

        }
    }

    public static class Network {


        public static boolean hasConnectivity(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }
    }


    public static class Toast {

        private static android.widget.Toast mLastToast;

        public static void showLongToast(Context context, java.lang.String message) {
            mLastToast = android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG);
            mLastToast.show();
        }

        public static void showLongToast(Context context, int resource) {
            mLastToast = android.widget.Toast.makeText(context, context.getString(resource), android.widget.Toast.LENGTH_LONG);
            mLastToast.show();
        }

        public static void cancelToast() {
            if (mLastToast != null) {
                mLastToast.cancel();
            }
        }
    }
}
