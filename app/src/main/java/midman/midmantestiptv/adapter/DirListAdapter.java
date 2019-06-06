package midman.midmantestiptv.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import midman.midmantestiptv.R;
import midman.midmantestiptv.adapter.holder.DirItemHolder;

public class DirListAdapter extends ArrayAdapter<DirItemHolder> {

    Context context;
    ImageView imageView;
    TextView txtTitle;
    DirItemHolder[] items;

    public DirListAdapter(Context context, int resourceId, DirItemHolder[] items) {
        super(context, resourceId, items);
        this.context = context;
        this.items=items;
    }

    /*private view holder class*/
    /**
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
    }
    **/

    public View getView(int position, View convertView, ViewGroup parent) {
        //DirItemHolder rowItem = getItem(position);
        DirItemHolder rowItem = items[position];

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        convertView = mInflater.inflate(R.layout.item_dir_pick, null);
        txtTitle = (TextView) convertView.findViewById(R.id.tvNamePick);
        imageView = (ImageView) convertView.findViewById(R.id.imgPick);

        txtTitle.setText(rowItem.getName());

        if (rowItem.isFile()) {
            txtTitle.setBackground(context.getResources().getDrawable(R.color.bb_main));
            imageView.setBackground(context.getResources().getDrawable(R.drawable.ic_file));

            String name = rowItem.getName();
            int idx = name.lastIndexOf('.');
            if (idx > 0) {
                String extension = name.substring(name.lastIndexOf("."));
                if (".m3u".equalsIgnoreCase(extension) || ".m3u8".equalsIgnoreCase(extension)) {
                    if (".m3u".equalsIgnoreCase(extension) || ".m3u8".equalsIgnoreCase(extension)) {
                        imageView.setBackground(context.getResources().getDrawable(R.drawable.ic_play));
                    }
                }
            }
        }
        else {
            txtTitle.setBackground(context.getResources().getDrawable(R.color.bb_main));
            imageView.setBackground(context.getResources().getDrawable(R.drawable.ic_folder));
        }

        //holder.imageView.setImageResource(rowItem.getImageId());

        return convertView;
    }
}
