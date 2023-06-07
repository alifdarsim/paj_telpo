package com.paj.pajbustelpo.utils;

import com.paj.pajbustelpo.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;


public class UserFlowTracker {

    MainActivity mainActivity;
    private ArrayList<String> usersInside;
    private HashMap<String, Long> entryTimes;

    public UserFlowTracker(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        usersInside = new ArrayList<String>();
        entryTimes = new HashMap<String, Long>();
    }

    public int userTap(String userId) {
        if (entryTimes.containsKey(userId)) {
            long previousTime = entryTimes.get(userId);
            long currentTime = System.currentTimeMillis();
            long timeElapsed = (currentTime - previousTime) / 1000;

            if (timeElapsed < 30) {
                // Duplicate entry within one minute, ignore it
                System.out.println("Duplicate entry detected for employee " + userId + ". Ignoring.");
                return UserTapState.USER_TAP_IDLE;
            } else {
                employeeExits(userId);
                return UserTapState.USER_TAP_OUT;
            }
        } else {
            usersInside.add(userId);
            entryTimes.put(userId, System.currentTimeMillis());
            System.out.println("Employee " + userId + " has entered the office.");
            return UserTapState.USER_TAP_IN;
        }
    }

    public ArrayList<String> getUsersInside() {
        return usersInside;
    }

    public void clearAllUser() {
        usersInside.clear();
        entryTimes.clear();
    }

    public void employeeExits(String userId) {
        usersInside.remove(userId);
        entryTimes.remove(userId);
        System.out.println("User " + userId + " has exited the office.");
    }

    public static class UserTapState {
        public static int USER_TAP_IN = 1;
        public static int USER_TAP_OUT = 0;
        public static int USER_TAP_IDLE = -1;
    }

}
