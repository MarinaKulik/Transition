package com.kulikmarina.transitions;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    List<BlogPost> blog_list;
    public Context context;
    private FirebaseFirestore mFirebaseFireStore;
    private FirebaseAuth mAuth;

    public BlogRecyclerAdapter(List<BlogPost> blog_list) {
        this.blog_list = blog_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_item, viewGroup, false);
        context = viewGroup.getContext();
        mFirebaseFireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.setIsRecyclable(false);

        String blogPostId = blog_list.get(i).BlogPostId;
        String current_user = mAuth.getCurrentUser().getUid();

        String desc_data = blog_list.get(i).getDescription();
        viewHolder.setDescText(desc_data);

        String image_url = blog_list.get(i).getImage_url();
        viewHolder.setBlogImage(image_url);

        String user_id = blog_list.get(i).getUser_id();

        mFirebaseFireStore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    viewHolder.setUserData(userName, userImage);

                } else {


                    }

                }

            }
        });

        long millisec = blog_list.get(i).getTime_stamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisec)).toString();
        viewHolder.setTime(dateString);

        //Get likes count
        mFirebaseFireStore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    int count = queryDocumentSnapshots.size();
                    viewHolder.updateLikeCount(count);

                } else {

                    viewHolder.updateLikeCount(0);

                }
            }
        });

        //GetLikes

        mFirebaseFireStore.collection("Posts/" + blogPostId + "/Likes").document(current_user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {

                    viewHolder.bloglikedbtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_liked));
                } else {
                    viewHolder.bloglikedbtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_liked));
                }
            }
        });

        //Likes feature
        viewHolder.bloglikedbtn.setOnClickListener(b -> {

            mFirebaseFireStore.collection("Posts/" + blogPostId + "/Likes").document(current_user).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (!task.getResult().exists()) {
                        Map<String, Object> likesMap = new HashMap<>();
                        likesMap.put("time_stamp", FieldValue.serverTimestamp());

                        mFirebaseFireStore.collection("Posts/" + blogPostId + "/Likes")
                                .document(current_user).set(likesMap);
                    }
                    //mFirebaseFireStore.collection("Posts/" + blogPostId + "/Likes")
                           // .document(current_user).delete();
                }
            });


        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView descView;
        private View mView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;

        private ImageView bloglikedbtn;
        private TextView bloglikedCount;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            bloglikedbtn = mView.findViewById(R.id.blog_like_btn);
        }

        public void setDescText(String descText) {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);

        }

        public void setBlogImage(String downloadUri) {
            blogImageView = mView.findViewById(R.id.blog_image);
            Glide.with(context).load(downloadUri).into(blogImageView);

        }

        public void setTime(String date) {
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);

        }

        public void setUserData(String name, String image) {
            blogUserName = mView.findViewById(R.id.blog_user_name);
            blogUserImage = mView.findViewById(R.id.blog_user_image);

            blogUserName.setText(name);

            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.ic_search);
            Glide.with(context).applyDefaultRequestOptions(placeHolder).load(image).into(blogUserImage);

        }

        public void updateLikeCount(int count) {

            bloglikedCount = mView.findViewById(R.id.blog_like_count);
            bloglikedCount.setText(count + " Likes");

        }
    }


}
