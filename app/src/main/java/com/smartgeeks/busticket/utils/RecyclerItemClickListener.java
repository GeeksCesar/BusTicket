package com.smartgeeks.busticket.utils;


import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener{

    private OnItemClickListener itemClickListener;
    GestureDetector gestureDetector;

    public interface OnItemClickListener{
        public void OnItemClick(View view, int position);
    }

    public RecyclerItemClickListener(Context context, OnItemClickListener listener){
        itemClickListener = listener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
        View childView = rv.findChildViewUnder(event.getX(), event.getY());

        if (childView != null && itemClickListener != null && gestureDetector.onTouchEvent(event)){
            itemClickListener.OnItemClick(childView, rv.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

}
