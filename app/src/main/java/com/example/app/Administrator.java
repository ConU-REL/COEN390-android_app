package com.example.app;

/*
 * Administrator user class, stores credentials
 */

import android.widget.EditText;

class Administrator {
    private String username;
    private String password;

    Administrator(String name, String password) {
        username = name;
        this.password = password;
    }

    String getUsername() {
        return username;
    }

    // validate the given credentials
    int validateCreds(EditText username, EditText password) {
        if (username.getText().toString().equals(this.username)) {
            if (password.getText().toString().equals(this.password)) {
                return 0;
            }
            // return 1 if incorrect password
            return 1;
        }
        // return 2 if incorrect username
        return 2;
    }
}
