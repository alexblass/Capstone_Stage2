package net.alexblass.capstoneproject.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.alexblass.capstoneproject.R;
import net.alexblass.capstoneproject.models.Message;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.NORMAL;

/**
 * An adapter to display the message threads in a user's inbox.
 */

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private ArrayList<String> mMessageResults;
    private LayoutInflater mInflator;
    private Context mContext;
    private String mEmail;
    private ArrayList<Message> mLastMessages;
    private InboxAdapter.ItemClickListener mClickListener;

    public InboxAdapter(Context context, ArrayList<String> messages, ArrayList<Message> lastMessages, String email){
        this.mInflator = LayoutInflater.from(context);
        this.mMessageResults = messages;
        this.mContext = context;
        this.mEmail = email;
        this.mLastMessages = lastMessages;
    }

    public void updateMessageResults(ArrayList<String> messages, ArrayList<Message> lastMessages){
        this.mMessageResults = messages;
        this.mLastMessages = lastMessages;
        notifyDataSetChanged();
    }

    public void setClickListener(InboxAdapter.ItemClickListener itemClickListener){
        mClickListener = itemClickListener;
    }

    public String getItem(int position){
        return mMessageResults.get(position);
    }

    @Override
    public int getItemCount() {
        return mMessageResults.size();
    }

    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflator.inflate(R.layout.item_message, parent, false);
        InboxAdapter.ViewHolder viewHolder = new InboxAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final InboxAdapter.ViewHolder holder, int position) {
        Message lastMessageInThread = mLastMessages.get(position);
        if (lastMessageInThread != null){
            String recipient;
            if(lastMessageInThread.getSender().equals(mEmail)){
                recipient = lastMessageInThread.getSentTo();
            } else {
                recipient = lastMessageInThread.getSender();
            }
            holder.userNameTv.setText(recipient);

            if (!lastMessageInThread.isRead() && mEmail.equals(lastMessageInThread.getSentTo())){
                holder.userNameTv.setTypeface(holder.userNameTv.getTypeface(), BOLD);
                holder.messageTimeTv.setTypeface(holder.messageTimeTv.getTypeface(), BOLD);
            } else {
                holder.userNameTv.setTypeface(holder.userNameTv.getTypeface(), NORMAL);
                holder.messageTimeTv.setTypeface(holder.messageTimeTv.getTypeface(), NORMAL);
            }

            holder.messageTimeTv.setText(UserDataUtils.formatDate(lastMessageInThread));
        }
    }

    public interface ItemClickListener{
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.message_user_name) TextView userNameTv;
        @BindView(R.id.message_time) TextView messageTimeTv;

        public ViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null){
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }
}