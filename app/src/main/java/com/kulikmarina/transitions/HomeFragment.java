package com.kulikmarina.transitions;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private RecyclerView blog_post_view;
    private List<BlogPost> blog_list;
    private BlogRecyclerAdapter mBlogRecyclerAdapter;
    private DocumentSnapshot lastVisible;

    private Boolean isFirstPageLoad = true;


    private FirebaseFirestore mFirebaseFirestore;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);


        blog_list = new ArrayList<>();
        mBlogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blog_post_view = v.findViewById(R.id.blogPost_view);
        blog_post_view.setLayoutManager(new LinearLayoutManager(container.getContext()));
        blog_post_view.setAdapter(mBlogRecyclerAdapter);


        mFirebaseFirestore = FirebaseFirestore.getInstance();

        blog_post_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBootom = !recyclerView.canScrollVertically(1);
                if (reachedBootom) {

                    String desc = lastVisible.getString("description");
                    Toast.makeText(container.getContext(), "ReachedBottom: " + desc, Toast.LENGTH_SHORT).show();
                    loadMorePost();
                }

            }
        });

        Query firstQuery = mFirebaseFirestore.collection("Posts").orderBy("time_stamp", Query.Direction.DESCENDING).limit(3);
        firstQuery.addSnapshotListener(getActivity(),(queryDocumentSnapshots, e) -> {
            if(isFirstPageLoad) {

                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

            }
            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                if (doc.getType() == DocumentChange.Type.ADDED) {

                    String bloPostid = doc.getDocument().getId();

                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).with(bloPostid);
                    if(isFirstPageLoad) {
                        blog_list.add(blogPost);
                    }else{
                        blog_list.add(0, blogPost);
                    }

                    mBlogRecyclerAdapter.notifyDataSetChanged();


                }

            }
            isFirstPageLoad =false;
        });


        return v;
    }

    public void loadMorePost() {
        Query nextQuery = mFirebaseFirestore.collection("Posts").
                orderBy("time_stamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(getActivity(),(queryDocumentSnapshots, e) -> {
            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

            if(!queryDocumentSnapshots.isEmpty()){
            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                if (doc.getType() == DocumentChange.Type.ADDED) {
                    String bloPostid = doc.getDocument().getId();
                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).with(bloPostid);
                    blog_list.add(blogPost);

                    mBlogRecyclerAdapter.notifyDataSetChanged();


                }
            }

            }
        });



    }
}
