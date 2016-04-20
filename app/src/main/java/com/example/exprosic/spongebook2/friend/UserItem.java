package com.example.exprosic.spongebook2.friend;

import java.util.List;

/**
 * Created by exprosic on 4/18/2016.
 */
public class UserItem {
    public int userId;
    public String nick;
    public int gender;
    public String location;
    public List<String> previewBookIds;

    public UserItem(int userId, String nick, int gender, String location, List<String> previewBookIds) {
        this.userId = userId;
        this.nick = nick;
        this.gender = gender;
        this.location = location;
        this.previewBookIds = previewBookIds;
    }
}
