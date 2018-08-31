package com.philliphsu.clock2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pogant on 12.05.2018.
 */

public class User {
    private String mName;
    private String mEmail;

    private User() {}

    public User(String _email, String _Name) {
        this.mName = _Name;
        this.mEmail = _email;
    }

    public String getName() {
        return mName;
    }


    public String getEmail() {
        return mEmail;
    }

}

