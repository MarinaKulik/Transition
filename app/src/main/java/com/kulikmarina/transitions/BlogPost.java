package com.kulikmarina.transitions;


import java.util.Date;

public class BlogPost extends BlogPostId{

    public String user_id, image_url, description, thumb;
    public Date time_stamp;

    public BlogPost(String user_id, String image_url, String description, String thumb, Date time_stamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.description = description;
        this.thumb = thumb;
        this.time_stamp = time_stamp;
    }

    public BlogPost() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }
}