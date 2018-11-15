package com.bitanga.android.lynkactivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import org.w3c.dom.Document;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostListFragment extends Fragment
implements PostAdapter.OnPostSelectedListener {
    private static final String LOG_VAL = "Lynk";

    //actually has the post recycler view
    private RecyclerView mPostRecyclerView;
    private PostAdapter mAdapter;
    private ViewGroup mEmptyView;
    private int mPostPosition;

    private FirebaseFirestore db;
    private DocumentReference mUserRef;
    private CollectionReference mPostRef;
    private ListenerRegistration mUserRegistration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        mPostRecyclerView = (RecyclerView) view.findViewById(R.id.post_recycler_view);
//        mPostRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mEmptyView = (ViewGroup) view.findViewById(R.id.viewEmpty);

        db = FirebaseFirestore.getInstance();
        mUserRef = db.collection("users").document("testUser");
//        mUserRef.collection("lynked posts");
        mPostRef = db.collection("users").document("testUser").collection("posts");



        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**add post and enter postpageractivity**/
                /**or just go to postpageractivity to create post first**/
                Post post = new Post();
                /**maybe call to postFragment first to create post?**/
                addPost(mUserRef, post);

                /**
//                Intent intent = PostPagerActivity
//                        .newIntent((getContext()), post.getId());

//                startActivity(intent);
                 **/

                startActivity(new Intent(getActivity(), PostPagerActivity.class ));
            }

            /**Adds new post to database**/
            private Task<Void> addPost(final DocumentReference userRef, final Post post) {
                //create new reference for new post, for use into transaction
                final DocumentReference postRef = userRef.collection("posts").document(post.getId().toString());

                //add new post and update attributes
                return db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
//                        com.bitanga.android.lynkactivity.User user = transaction.get(userRef).toObject(com.bitanga.android.lynkactivity.User.class);

                        //commit to firestore
//                        transaction.set(userRef, user);
                        transaction.set(postRef, post);

                        return null;
                    }
                });
            }

        });

        // Get limit of posts and order by timestamp of posts
        Query postsQuery = mUserRef
                .collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
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
//        intent.putExtra(PostPagerActivity.EXTRA_POST_ID, post.getId());
        startActivity(intent);
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_post_list, menu);

    }


//    private void updateUI() {
//        PostLab postLab = PostLab.get(getActivity());
//        List<Post> posts = postLab.getPosts();
//
////        if(mAdapter == null) {
////            mAdapter = new PostAdapter(posts);
////            mPostRecyclerView.setAdapter(mAdapter);
////        } else {
////            mAdapter.setPosts(posts);
////            mAdapter.notifyDataSetChanged();
////        }
//    }

//    private class PostHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        private Post mPost;
//        private TextView mContentTextView;
//        private ImageView mPhotoImageView;
//        private ImageButton mLike;
//        private ImageButton mComment;
//        private ImageButton mFlag;
//        private TextView mTimePostedTextView;
//
//        private int numOfTimesFlagged;
//
//        public PostHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
//            super(inflater.inflate(viewType, parent, false));
//            itemView.setOnClickListener(this);
//
//            mContentTextView = (TextView) itemView.findViewById(R.id.post_content);
//            mPhotoImageView = (ImageView) itemView.findViewById(R.id.post_photo);
//            mLike = (ImageButton) itemView.findViewById(R.id.imageButton2);
//            mComment = (ImageButton) itemView.findViewById(R.id.imageButton);
//            mFlag = (ImageButton) itemView.findViewById(R.id.imageButton3);
//            mTimePostedTextView = (TextView) itemView.findViewById(R.id.timestamp);
//            numOfTimesFlagged = 0;
//        }
//
//        public void bind(Post post) {
//            mPost = post;
//            mContentTextView.setText(mPost.getContent());
//
//            mLike.setOnClickListener(new View.OnClickListener() {
//                //only when set to true it works since takes longer to test
//                //if post is user's
//                boolean ownPost = false;
//
//                @Override
//                public void onClick(View v) {
//                    //if post belongs to user, disable mLike button
//                    //if post under the test user...
//                    mPostRef.whereEqualTo("uuid", mPost.getId().toString())
//                            .limit(1).get()
//                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                @Override
//                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                    Log.d(LOG_VAL, "Cannot like your own post");
//                                    Toast.makeText(getContext(), "You can't like your own post",
//                                    Toast.LENGTH_SHORT).show();
//                                    ownPost = true;
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.w(LOG_VAL, "Error getting post", e);
//                        }
//                    });
//
//                    if (!ownPost) {
//                        //add post to liked posts to user
//                        Map<String, Object> lykedPostMap = new HashMap<>();
//                        //what else should lykedPostMap include?
//                        lykedPostMap.put("post id", mPost.getId().toString());
//                        mUserRef.collection("lynked posts")
//                                .add(lykedPostMap)
//                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                    @Override
//                                    public void onSuccess(DocumentReference documentReference) {
//                                        Log.d(LOG_VAL, "You have successfully liked this post!");
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w(LOG_VAL, "Error: post has not been liked.", e);
//                            }
//                        });
//
//
//                        Toast toast = Toast.makeText(getContext(),
//                                "You liked this post!",
//                                Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.BOTTOM, 1, 1);
//                        toast.show();
//
//                        //once liked, is disabled?
//                        mLike.setEnabled(false);
//                    }
//                }
//            });
//
//            //need to get current time minus post time
//            //post time should be set in PostFragment
//
//            Date currentTime = Calendar.getInstance().getTime();
//            mPost.setTimestamp(mPost.getTimestamp());
//            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
//            mTimePostedTextView.setText(dateFormat.format(mPost.getTimestamp()));
//
//
//            mComment.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast toast = Toast.makeText(getContext(),
//                            "You chose to comment on this post!",
//                            Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.BOTTOM, 1, 1);
//                    toast.show();
//                    //get post id like when going to make comment?
//                    //go to comment activity/fragment
//                    //start the intent
//                }
//            });
//            mFlag.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast toast = Toast.makeText(getContext(),
//                            "You flagged this post!",
//                            Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.BOTTOM, 1, 1);
//                    toast.show();
//                    numOfTimesFlagged++;
//                    if(numOfTimesFlagged == 3) {
//                        Toast.makeText(getContext(),
//                                "Consider this post BANNED!",
//                                Toast.LENGTH_SHORT).show();
////                        mFlag.setEnabled(false);
//                        PostLab.get(getActivity()).deletePost(mPost);
////                        updateUI();
//                    }
//                }
//            });
//
//            File mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);
//            if (mPhotoImageView != null && mPhotoFile.exists()) {
//                Bitmap bitmap = PictureUtils.getScaledBitmap(
//                        mPhotoFile.getPath(), getActivity());
//                mPhotoImageView.setImageBitmap(bitmap);
//            } else {
//                mPhotoImageView.setVisibility(View.GONE);
//            }
//
//        }
//
//        /**Goes to that Post's PostFragment**/
//        //if user's post, can modify
//        //if not user's post, don't do anything!
//        @Override
//        public void onClick(View view) {
//            mPostPosition = getAdapterPosition();
//            Intent intent = PostPagerActivity.newIntent(getActivity(), mPost.getId());
//            startActivity(intent);
//        }
//    }

//    private class PostAdapter extends FirestoreAdapter<PostHolder>/*RecyclerView.Adapter<PostHolder>*/ {
//        public interface OnPostSelectedListener {
//            void onPostSelected(DocumentSnapshot post);
//        }
//
//        private OnPostSelectedListener mListener;
//
//        public PostAdapter(Query query, OnPostSelectedListener listener) {
//            super(query);
//            mListener = listener;
//        }
//
//        @Override
//        public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
//
//            return new PostHolder(layoutInflater, parent, viewType);
//        }
//
//        @Override
//        public void onBindViewHolder(PostHolder holder, int position) {
////            Post post = mPosts.get(position);
////            holder.bind(post);
//            holder.bind(getSnapshot(position).toObject(Post.class));
//        }
//
//        @Override
//        public int getItemCount() {
//            return mPosts.size();
//        }
//
//        public void setPosts(List<Post> posts) {
//            mPosts = posts;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return R.layout.list_item_post;
//        }
//
//    }
}
