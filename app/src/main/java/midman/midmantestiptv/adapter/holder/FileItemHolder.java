package midman.midmantestiptv.adapter.holder;

import android.app.FragmentManager;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import midman.midmantestiptv.MainActivity;
import midman.midmantestiptv.R;
import midman.midmantestiptv.adapter.FileItem;

public class FileItemHolder implements View.OnClickListener, View.OnLongClickListener {

    private Context mContext;
    private FragmentManager fm;
    private boolean animation;

    TextView fileName;
    ImageView check;

    public FileItemHolder(Context _context, View view, TextView _fileName, ImageView _check) {
        this.fileName = _fileName;
        this.check = _check;
        this.mContext=_context;

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    public void update(final FileItem item) {

        fileName.setText("  " + item.getName());

        /**
         int pos = item.getPosition();
         if (pos==0){
         fileName.setBackground(mContext.getResources().getDrawable(R.color.linePar));
         } else if (pos==1){
         fileName.setBackground(mContext.getResources().getDrawable(R.color.lineDispar));
         } else {
         if ((pos%2)==0) {
         fileName.setBackground(mContext.getResources().getDrawable(R.color.linePar));
         } else {
         fileName.setBackground(mContext.getResources().getDrawable(R.color.lineDispar));
         }
         }
         **/

        if (item.getCheck()!=null) {
            if (item.getCheck()) {
                check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.check_ok));
                fileName.setBackground(mContext.getResources().getDrawable(R.color.file_ok));
            } else {
                check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.check_ko));
                fileName.setBackground(mContext.getResources().getDrawable(R.color.file_ko));
            }
        } else {
            check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play));
            fileName.setBackground(mContext.getResources().getDrawable(R.color.file));
        }
    }

    @Override
    public void onClick(View v) {
        ((MainActivity)mContext).openAppToLoadURL(((String)fileName.getText()).trim());
    }

    @Override
    public boolean onLongClick(View v) {
        return true;
    }

}
