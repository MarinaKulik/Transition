package com.kulikmarina.transitions;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class BlogPostId {
    @Exclude
    public String BlogPostId;

    public <T extends BlogPostId> T with(@NonNull String id){

        this.BlogPostId = id;
        return (T) this;

    }
}
