package org.examole.goodsservice.util;

import org.example.goodsservice.entity.User;

public class UserContext {
    private static final ThreadLocal<User> USER_CONTEXT = new ThreadLocal<>();
    public static void setUser(User user) {
        USER_CONTEXT.set(user);
    }
    public static User getUser() {
        return USER_CONTEXT.get();
    }
    public static Long getId() {
        User user = USER_CONTEXT.get();
        return user == null ? null : user.getId();
    }
    public static void removeUser() {
        USER_CONTEXT.remove();
    }
}
