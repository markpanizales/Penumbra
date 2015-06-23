package com.wormz.penumbra;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by markanthonypanizales on 12/9/14.
 */
public abstract class DataLoader<D> extends AsyncTaskLoader {
    private D mData;

    public DataLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null){
            deliverResult(mData);
        }else{
            forceLoad();
        }
    }

    @Override
    public void deliverResult(Object data) {
         mData = (D) data;
        if (isStarted())
            super.deliverResult(data);
    }
}
