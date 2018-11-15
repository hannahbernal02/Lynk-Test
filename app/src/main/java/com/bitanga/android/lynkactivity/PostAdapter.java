package com.bitanga.android.lynkactivity;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

public class PostAdapter extends FirestoreAdapter<PostAdapter.PostHolder>{

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
        return new PostHolder(inflater.inflate(R.layout.list_item_post, parent, false));
    }

    @Override
    public void onBindViewHolder(PostHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class PostHolder extends RecyclerView.ViewHolder {

        private Post mPost;
        private TextView mContentTextView;
        private ImageView mPhotoImageView;
        private ImageButton mLike;
        private ImageButton mComment;
        private ImageButton mFlag;
        private TextView mTimePostedTextView;

        private int numOfTimesFlagged;

        public PostHolder (View itemView) {
            super(itemView);
            //what is butterknife?
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnPostSelectedListener listener) {

            mContentTextView = (TextView) itemView.findViewById(R.id.post_content);
            mPhotoImageView = (ImageView) itemView.findViewById(R.id.post_photo);
            mLike = (ImageButton) itemView.findViewById(R.id.imageButton2);
            mComment = (ImageButton) itemView.findViewById(R.id.imageButton);
            mFlag = (ImageButton) itemView.findViewById(R.id.imageButton3);
            mTimePostedTextView = (TextView) itemView.findViewById(R.id.timestamp);
            numOfTimesFlagged = 0;


            Post post = snapshot.toObject(Post.class);
            Resources resources = itemView.getResources();

            mContentTextView.setText(post.getContent());
            numOfTimesFlagged = post.getNumOfTimesFlagged();
            mTimePostedTextView.setText(post.getTimestamp().toString());
            //error here
//            mPost.setTimestamp(post.getTimestamp());

            String photoName = post.getPhotoFilename();

//            File mPhotoFile = PostLab.get(getActivity()).getPhotoFile(mPost);
//            if (mPhotoImageView != null) {
//                Bitmap bitmap = PictureUtils.getScaledBitmap(
//                        mPhotoFile.getPath(), getActivity());
//                mPhotoImageView.setImageBitmap(bitmap);
//            } else {
//                mPhotoImageView.setVisibility(View.GONE);
//            }

            numOfTimesFlagged = post.getNumOfTimesFlagged();

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
