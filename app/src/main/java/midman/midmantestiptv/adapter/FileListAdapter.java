package midman.midmantestiptv.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import midman.midmantestiptv.R;
import midman.midmantestiptv.adapter.holder.FileItemHolder;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {

    private List<FileItem> items;

    public FileListAdapter(List<FileItem> _items){
        items = _items;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_m3u, parent, false);
        FileViewHolder holder = new FileViewHolder(v);
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        final FileItem item = items.get(position);
        if (item != null) {
            holder.update(item);
        }
    }

    public void updateItemOK(int position) {

    }

    public void updateItemKO(int position) {

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void update(List<FileItem> _list) {
        this.items = _list;
        notifyDataSetChanged();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {

        FileItemHolder fileItemHolder;
        TextView fileName;
        ImageView img;


        public FileViewHolder(View v) {
            super(v);

            fileName = (TextView)v.findViewById(R.id.tvFileName);
            img = (ImageView) v.findViewById(R.id.imgCheck);
            fileItemHolder = new FileItemHolder(v.getContext(), v, fileName, img);
        }

        public void update(final FileItem item) {
            fileItemHolder.update(item);
        }
    }
}
