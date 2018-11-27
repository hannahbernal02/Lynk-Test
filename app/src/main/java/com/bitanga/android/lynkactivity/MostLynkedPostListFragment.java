package com.bitanga.android.lynkactivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class MostLynkedPostListFragment extends Fragment
implements PostAdapter.OnPostSelectedListener {

    private static final String LOG_VAL = "Lynk";

    private RecyclerView mPostRecyclerView;
    private ViewGroup mEmptyView;
    private PostAdapter mAdapter;
    private int mPostCount;

    private FirebaseFirestore db;
    private DocumentReference mUserRef;
    private CollectionReference mLynkedPostRef;
    private ListenerRegistration mUserRegistration;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lynked_post_list, container, false);
        mPostRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycler_view);
        mEmptyView = (ViewGroup) view.findViewById(R.id.viewEmpty);

        db = FirebaseFirestore.getInstance();
        mUserRef = db.collection("users").document("testUser");
        mLynkedPostRef = db.collection("lynked posts");

        /**CHANGE QUERY TO RATE MOST LIKED**/
        // Get limit of posts and order by timestamp of posts
        Query postsQuery = mLynkedPostRef
                .orderBy("likes", Query.Direction.DESCENDING)
                .limit(50);

        //RecyclerView
        mAdapter = new PostAdapter(postsQuery, this) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mPostRecyclerView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                    Log.d(LOG_VAL, "mPostRecyclerView is Invisible");
                } else {
                    mPostRecyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    Log.d(LOG_VAL, "mPostRecyclerView is Visible");
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                Log.w(LOG_VAL, "Error: check logs for info.", e);
            }
        };

        Log.d(LOG_VAL, "mostLynked mAdapter: " + mAdapter);

        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPostRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAdapter.startListening();
//        mPostRegistration = mUserRef.addSnapshotListener(this);

    }

    @Override
    public void onStop() {
        super.onStop();

        mAdapter.stopListening();

//        if (mPostRegistration != null) {
//            mPostRegistration.remove();
//            mPostRegistration = null;
//        }
    }

    @Override
    public void onPostSelected(DocumentSnapshot post) {
        //Go to post and modify if you're the user of post
//        Intent intent = PostPagerActivity.newIntent(getActivity(),UUID.fromString(post.getId()));
        Intent intent = new Intent(getContext(), PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.KEY_POST_ID, post.getId());

//        startActivity(intent);
    }
}
