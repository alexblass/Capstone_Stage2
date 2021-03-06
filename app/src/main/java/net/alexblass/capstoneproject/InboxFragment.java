package net.alexblass.capstoneproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.alexblass.capstoneproject.models.Message;
import net.alexblass.capstoneproject.utils.InboxAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static net.alexblass.capstoneproject.data.Keys.MSG_CONVERSATION_KEY;
import static net.alexblass.capstoneproject.data.Keys.MSG_DATA_KEY;
import static net.alexblass.capstoneproject.data.Keys.MSG_DATE_TIME_KEY;
import static net.alexblass.capstoneproject.data.Keys.MSG_KEY;
import static net.alexblass.capstoneproject.data.Keys.MSG_READ_FLAG_KEY;
import static net.alexblass.capstoneproject.data.Keys.MSG_SENDER_EMAIL_KEY;
import static net.alexblass.capstoneproject.data.Keys.MSG_SENT_TO_EMAIL_KEY;

/**
 * A Fragment to display the user's message inbox.
 */
public class InboxFragment extends Fragment implements InboxAdapter.ItemClickListener {

    @BindView(R.id.inbox_messages_rv) RecyclerView mRecyclerView;
    @BindView(R.id.inbox_empty_tv) TextView mEmptyInboxTv;
    @BindView(R.id.inbox_progressbar) ProgressBar mProgress;

    private FirebaseAuth mAuth;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<String> mMessages;
    private ArrayList<Message> mLastMessages;
    private InboxAdapter mAdapter;
    private Query mQuery;
    private ValueEventListener mListener;

    public InboxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inbox, container, false);
        ButterKnife.bind(this, root);

        mAuth = FirebaseAuth.getInstance();
        final String email = mAuth.getCurrentUser().getEmail().replace(".", "(dot)");
        mMessages = new ArrayList<>();
        mLastMessages = new ArrayList<>();

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerView.setHasFixedSize(true);

        mAdapter = new InboxAdapter(getActivity(), mMessages, mLastMessages, mAuth.getCurrentUser().getEmail());
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        mQuery = FirebaseDatabase.getInstance().getReference().child(MSG_KEY);

        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMessages = new ArrayList<>();
                    Message lastMessage = null;
                    Iterable<DataSnapshot> results = dataSnapshot.getChildren();
                    for (DataSnapshot messageThreadData : results){
                        if(messageThreadData.getKey().contains(email)){

                            mMessages.add(messageThreadData.getKey());

                            Iterable<DataSnapshot> messages = messageThreadData.getChildren();
                            for (DataSnapshot messageData : messages) {
                                if (!messages.iterator().hasNext()){
                                    String sender = messageData.child(MSG_SENDER_EMAIL_KEY).getValue().toString();
                                    String sentTo = messageData.child(MSG_SENT_TO_EMAIL_KEY).getValue().toString();
                                    lastMessage = new Message(sender, sentTo,
                                            messageData.child(MSG_DATA_KEY).getValue().toString(),
                                            (boolean)messageData.child(MSG_READ_FLAG_KEY).getValue());
                                    lastMessage.setDateTime(messageData.child(MSG_DATE_TIME_KEY).getValue().toString());
                                    mLastMessages.add(lastMessage);
                                }
                            }
                        }
                    }
                }

                mAdapter.updateMessageResults(mMessages, mLastMessages);
                mProgress.setVisibility(View.GONE);
                if (mMessages.size() > 0){
                    mEmptyInboxTv.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mRecyclerView.setVisibility(View.GONE);
                    mEmptyInboxTv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), getResources().getString(R.string.message_retrieval_error), Toast.LENGTH_SHORT).show();
            }
        };

        mQuery.addValueEventListener(mListener);

        return root;
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent launchViewConversationActivity = new Intent(getContext(), ViewConversationActivity.class);
        launchViewConversationActivity.putExtra(MSG_CONVERSATION_KEY, mAdapter.getItem(position));
        startActivity(launchViewConversationActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mQuery != null) {
            mQuery.removeEventListener(mListener);
        }
    }
}
