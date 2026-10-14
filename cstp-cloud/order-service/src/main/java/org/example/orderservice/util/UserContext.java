package org.example.orderservice.util;

import org.example.orderservice.entity.CurrentUser;

public class UserContext {
    private static final ThreadLocal<CurrentUser> holder = new ThreadLocal<>();

    public static void setUser(CurrentUser user) { holder.set(user); }
    public static CurrentUser getUser() { return holder.get(); }
    public static Long getUserId() { CurrentUser u = holder.get(); return u == null ? null : u.getId(); }
    public static void removeUser() { holder.remove(); }
}
