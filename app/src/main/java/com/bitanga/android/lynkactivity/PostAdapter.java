package com.bitanga.android.lynkactivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import org.w3c.dom.Document;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PostAdapter extends FirestoreAdapter<PostAdapter.PostHolder>{

    public static Context context;

    public interface OnPostSelectedListener {

        void onPostSelected(DocumentSnapshot post);

    }

    private OnPostSelectedListener mListener;

    public PostAdapter(Query query, OnPostSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        context = parent.getContext();
        return new PostHolder(inflater.inflate(R.layout.list_item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(PostHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class PostHolder extends RecyclerView.ViewHolder {

        private Post mPost;
        private TextView mContentTextView;
        private TextView likes;
        private ImageView mPhotoImageView;
        private ImageButton mLike;
        private ImageButton mComment;
        private ImageButton mFlag;
        private ImageButton mAddFriend;
        private TextView mTimePostedTextView;
        private TextView mUsernameTextView;
        private int numOfLikes;
        private int numOfTimesFlagged;
        private FirebaseFirestore db;
        private DocumentReference mUserRef;


        public PostHolder (View itemView) {
            super(itemView);

        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnPostSelectedListener listener) {
            db = FirebaseFirestore.getInstance();
            mUserRef = db.collection("users").document("testUser");
            mContentTextView = (TextView) itemView.findViewById(R.id.post_content);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.post_photo);
            mUsernameTextView = (TextView) itemView.findViewById(R.id.username);
            mLike = (ImageButton) itemView.findViewById(R.id.imageButton2);
            mComment = (ImageButton) itemView.findViewById(R.id.imageButton);
            mFlag = (ImageButton) itemView.findViewById(R.id.imageButton3);
            mAddFriend = (ImageButton) itemView.findViewById(R.id.friend_user);
            mTimePostedTextView = (TextView) itemView.findViewById(R.id.timestamp);
            numOfTimesFlagged = 0;
            likes = (TextView) itemView.findViewById(R.id.numOfLikes);


            final Post post = snapshot.toObject(Post.class);
            Resources resources = itemView.getResources();
            mContentTextView.setText(post.getContent());
            mUsernameTextView.setText(post.getUsername());
            numOfTimesFlagged = post.getNumOfTimesFlagged();
            DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            mTimePostedTextView.setText(dateFormat.format(post.getTimestamp()));
            likes.setText(String.valueOf(post.getLikes()));
            numOfLikes = post.getLikes();


            File filesDir = context.getFilesDir();
            String photoFile = post.getPhotoFilename();
            File mPhotoFile = new File(filesDir, photoFile);

            if (mPhotoImageView != null && mPhotoFile.exists()) {
                Log.d("Lynk", "mPhotoImageView set to photo");
                Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),768, 1184);
                mPhotoImageView.setImageBitmap(bitmap);
            } else {
                Log.d("Lynk", "mPhotoImageView set to GONE");
                mPhotoImageView.setVisibility(View.GONE);
            }
            numOfTimesFlagged = post.getNumOfTimesFlagged();
            mFlag.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     numOfTimesFlagged++;
                     if (numOfTimesFlagged == 1) {
                         mFlag.setColorFilter(Color.rgb(255,165,0));
                         Toast.makeText(v.getContext(), "Post has been reported",
                                 Toast.LENGTH_SHORT).show();
                     }
                     if (numOfTimesFlagged == 2) {
                         mFlag.setColorFilter(Color.rgb(255,0,0));
                         Toast.makeText(v.getContext(), "Post has been reported",
                                 Toast.LENGTH_SHORT).show();
                     }
                     /**updates db if flagged once or twice (no need to update if third time)**/
                     if (numOfTimesFlagged == 1 || numOfTimesFlagged == 2) {
                         flagPost(mUserRef, mPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                                 Log.d("Lynk", "Successfully flagged post");
                             }
                         }).addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 Log.w("error", "Could not flag post", e);
                             }
                         });
                     }
                     if (numOfTimesFlagged == 3) {
                         deletePost(mUserRef, mPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                                 Log.d("Lynk", "this was a success");
                             }
                         }).addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 Log.w("error", "this was not a success", e);
                             }
                         });
                         Toast.makeText(v.getContext(), "Post has been deleted",
                                 Toast.LENGTH_SHORT).show();
                     }
                 }

                 private Task<Void> flagPost(final DocumentReference userRef, final Post post) {
                     return db.runTransaction(new Transaction.Function<Void>() {
                         @Nullable
                         @Override
                         public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                             DocumentReference postRef = userRef.collection("posts").document(snapshot.getId());

                             Post originalPost = transaction.get(postRef).toObject(Post.class);

                             int newNumFlags = originalPost.getNumOfTimesFlagged() + 1;

                             originalPost.setNumOfTimesFlagged(newNumFlags);

                             transaction.set(postRef, originalPost);

                             return null;
                         }
                     });
                 }

                 private Task<Void> deletePost(final DocumentReference userRef, final Post post) {
                     return db.runTransaction(new Transaction.Function<Void>() {
                         @Override
                         public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                             DocumentReference postRef = userRef.collection("posts").document(snapshot.getId());
                             transaction.delete(postRef);

                             return null;
                         }
                     });
                 }
             });
            mLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    numOfLikes++;
                    if(numOfLikes > 0){
                        mLike.setColorFilter(Color.RED);
                        likes.setText(String.valueOf(numOfLikes));

                        addLikePost(mUserRef, post)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Lynk", "Post successfully liked!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("error", "Unsuccessfully liked post", e);
                                    }
                                });

                        Toast.makeText(v.getContext(), "Liked post!",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                private Task<Void> addLikePost(final DocumentReference userRef, final Post post) {
                    /**save in lynkedposts collection (outside of users)**/
                    //when added to lynked post collection, will have same id as it does in post collection
                    final DocumentReference lynkedPost = db.collection("lynked posts").document(snapshot.getId());

                    return db.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            DocumentReference postRef = userRef.collection("posts").document(snapshot.getId());

                            Post originalPost = transaction.get(postRef).toObject(Post.class);

                            int newNumLikes = originalPost.getLikes() + 1;

                            originalPost.setLikes(newNumLikes);

                            transaction.set(postRef, originalPost);
                            transaction.set(lynkedPost, post);
                            return null;
                        }
                    });
                }
            });

            mComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Lynk", "this is post id: " + snapshot.getId());

                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra(PostDetailActivity.KEY_POST_ID, snapshot.getId());

                    context.startActivity(intent);
                }
            });

            /**Add friend**/
            mAddFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Log.d("Lynk", "Adding friend");

                    /**maybe add request?**/
                    /**need to verify that you're not befriending yourself**/
                    addFriend(mUserRef).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Lynk", "Successfully added user as friend");
                            Toast.makeText(v.getContext(), "Adding this user as friend",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("error", "Could not add user as friend..", e);
                        }
                    });

                }

                private Task<Void> addFriend(final DocumentReference userRef) {
                    //new document reference? or collection reference?
                    final CollectionReference friendRef = userRef.collection("friend list");

                    return db.runTransaction(new Transaction.Function<Void>() {
                        @Nullable
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            //get username from post since it saves it
                            DocumentReference postRef = userRef.collection("posts").document(snapshot.getId());

                            Post originalPost = transaction.get(postRef).toObject(Post.class);
                            String otherUserName = originalPost.getUsername();

                            //get post's user
                            User otherUser = transaction.get(db.collection("users").document(otherUserName)).toObject(User.class);

                            //get original user (that you are)
                            User user = transaction.get(userRef).toObject(User.class); //not sure if this is right?

                            int numOfFriends = user.getNumFriends();

                            //update number of friends you have
                            user.setNumFriends(numOfFriends);

                            transaction.set(userRef, user);
                            transaction.set(friendRef.document(otherUserName), otherUser);


                            return null;
                        }
                    });
                }
            });

            //Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onPostSelected(snapshot);
                    }
                }
            });
        }
    }
}
