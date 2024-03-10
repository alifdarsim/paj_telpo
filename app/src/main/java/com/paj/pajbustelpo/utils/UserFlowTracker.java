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

    public void viewAllUserInside() {
        String usersInsideString = "";
        for (String user : usersInside) {
            usersInsideString += user + ", ";
        }
        mainActivity.logger.writeToLogger("\nUsers inside: " + usersInsideString + "\n\n", "yellow");
    }

    public int userTap(String userId) {

        mainActivity.logger.writeToLogger("User tap with id: " + userId, "yellow");

        if (entryTimes.containsKey(userId)) {
            long previousTime = entryTimes.get(userId);
            long currentTime = System.currentTimeMillis();
            long timeElapsed = (currentTime - previousTime) / 1000; //1 here is 1 second

            if (timeElapsed < 30) {
                // Duplicate entry within 30 second, ignore it
                mainActivity.logger.writeToLogger("Duplicate entry within 30 second, ignore it", "yellow");
                viewAllUserInside();
                return UserTapState.USER_TAP_DOUBLE;
            }
            else if (timeElapsed > 7200){
                // if more than 2 hour then we will assume that user already off board without tapping a card. So mark this one as tap in
                removeUserQueue(userId);
                addUserQueue(userId);
                mainActivity.logger.writeToLogger("More than 2 hour is passed. So mark this one as tap in", "yellow");
                viewAllUserInside();
                return UserTapState.USER_TAP_IN;
            }
            else {
                // if tap before 2 hour and more than 30 sec, then consider as tapping out
                removeUserQueue(userId);
                mainActivity.logger.writeToLogger("Tap before 2 hour and more than 30 sec, then consider as tapping out", "yellow");
                viewAllUserInside();
                return UserTapState.USER_TAP_OUT;
            }
        } else {
            addUserQueue(userId);
            mainActivity.logger.writeToLogger("User first time tap, so tap in", "yellow");
            viewAllUserInside();
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

    public void addUserQueue(String userId) {
        usersInside.add(userId);
        entryTimes.put(userId, System.currentTimeMillis());
    }

    public void removeUserQueue(String userId) {
        usersInside.remove(userId);
        entryTimes.remove(userId);
    }

    public static class UserTapState {
        public static int USER_TAP_IN = 1;
        public static int USER_TAP_OUT = 0;
        public static int USER_TAP_DOUBLE = -1;
    }

}
