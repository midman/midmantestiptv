package midman.midmantestiptv.listener;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

public interface OnSwipeItemListener {

    public void swipeLeft(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          MotionEvent event, float dX, float dY, int actionState, boolean isCurrentlyActive);

    public void swipeRight(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                           MotionEvent event, float dX, float dY, int actionState, boolean isCurrentlyActive);

    public void swipeTop(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                         float dX, float dY, int actionState, boolean isCurrentlyActive);

    public void swipeBottom(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive);

}
