package net.alexblass.capstoneproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.alexblass.capstoneproject.models.User;
import net.alexblass.capstoneproject.utils.UserDataUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static net.alexblass.capstoneproject.data.Keys.USER_BANNER_IMG_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_BIRTHDAY_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_DESCRIPTION_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_GENDER_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_NAME_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_PROFILE_IMG_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_RELATIONSHIP_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_SEXUALITY_KEY;
import static net.alexblass.capstoneproject.data.Keys.USER_ZIPCODE_KEY;

public class LoginActivity extends AppCompatActivity {

    private static final String PROMPT_FRAG = "prompt_fragment";

    @BindView(R.id.login_no_connection_tv) TextView mConnectivityTv;
    @BindView(R.id.login_progressbar) ProgressBar mProgress;

    private FirebaseAuth mAuth;
    private Query mQuery;
    private ValueEventListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        loadActivity(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                getSupportFragmentManager().popBackStack();
                if (getSupportFragmentManager().getBackStackEntryCount() == 1){
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mQuery != null) {
            mQuery.removeEventListener(mListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActivity(null);
    }

    private void loadActivity(Bundle savedInstanceState){
        if (!UserDataUtils.checkNetworkConnectivity(this)) {
            mProgress.setVisibility(View.GONE);
            mConnectivityTv.setVisibility(View.VISIBLE);
            return;
        } else {
            mConnectivityTv.setVisibility(View.GONE);
        }

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            mProgress.setVisibility(View.GONE);

            AccountPromptFragment promptFragment;
            if (savedInstanceState == null) {
                mProgress.setVisibility(View.GONE);
                promptFragment = new AccountPromptFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.login_fragment_container, promptFragment, PROMPT_FRAG)
                        .commit();
            } else {
                promptFragment = (AccountPromptFragment) getSupportFragmentManager()
                        .findFragmentByTag(PROMPT_FRAG);
            }

        } else {
            mQuery = FirebaseDatabase.getInstance().getReference().child(
                    mAuth.getCurrentUser().getEmail().replace(".", "(dot)"));

            mListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name, zipcode, description, sexuality, relationshipStatus, email, profilePicUri, bannerPicUri;
                        long gender;

                        name = (String) dataSnapshot.child(USER_NAME_KEY).getValue();

                        long birthdayInMillis = (long) dataSnapshot.child(USER_BIRTHDAY_KEY).getValue();

                        zipcode = (String) dataSnapshot.child(USER_ZIPCODE_KEY).getValue();
                        description = (String) dataSnapshot.child(USER_DESCRIPTION_KEY).getValue();

                        gender = (long) dataSnapshot.child(USER_GENDER_KEY).getValue();
                        sexuality = (String) dataSnapshot.child(USER_SEXUALITY_KEY).getValue();
                        relationshipStatus = (String) dataSnapshot.child(USER_RELATIONSHIP_KEY).getValue();
                        profilePicUri = (String) dataSnapshot.child(USER_PROFILE_IMG_KEY).getValue();
                        bannerPicUri = (String) dataSnapshot.child(USER_BANNER_IMG_KEY).getValue();

                        email = mAuth.getCurrentUser().getEmail();

                        User user = new User(email, name, birthdayInMillis, zipcode, gender,
                                sexuality, relationshipStatus, description, profilePicUri, bannerPicUri);

                        Intent dashboardActivity = new Intent(getApplicationContext(), DashboardActivity.class);
                        dashboardActivity.putExtra(USER_KEY, user);
                        startActivity(dashboardActivity);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.verification_error), Toast.LENGTH_SHORT).show();
                }
            };

            mQuery.addValueEventListener(mListener);
        }
    }
}
