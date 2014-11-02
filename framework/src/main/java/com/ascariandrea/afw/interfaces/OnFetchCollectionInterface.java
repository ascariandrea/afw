package com.ascariandrea.afw.interfaces;


import com.ascariandrea.afw.client.AsyncCollectionHandler;
import com.ascariandrea.afw.models.Model;

import java.util.List;

/**
 * Created by andreaascari on 03/02/14.
 */
public interface OnFetchCollectionInterface<T extends Model> {

    public void fetchData(AsyncCollectionHandler<T> asyncCollectionHandler );

    public void fetchCompleted(boolean completed);

    public void populateView(List<T> list);
}