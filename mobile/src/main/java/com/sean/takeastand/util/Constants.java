/*
 * Copyright (C) 2014 Sean Allen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sean.takeastand.util;

/**
 * Constants used by multiple classes
 * Created by Sean on 2014-10-04.
 */
public final class Constants {
    /*
    Eventually will break some of these off into more defined classes.
    Break the status ints off into a ALARM_STATUS class.
     */
    public static final String ALARM_SCHEDULE = "CurrentAlarmSchedule";
    public static final String ALARM_UID = "AlarmUID";
    public static final int secondsInMinute = 60;
    public static final int millisecondsInSecond = 1000;
    public static final String EVENT_SHARED_PREFERENCES = "Event_Shared_Preferences";
    public static final String USER_SHARED_PREFERENCES = "User_Shared_Preferences";
    public static final String CURRENT_RUNNING_SCHEDULED_ALARM = "CurrentScheduledAlarm";
    public static final String CURRENT_SCHEDULED_ALARM_TITLE = "CurrentScheduledAlarmTitle";
    public static final String ALARM_SCHEDULE_DELETED = "AlarmDeleted";
    public static final String NEXT_ALARM_TIME_STRING = "NextAlarmTimeString";
    public static final String NEXT_ALARM_TIME_MILLIS = "NextAlarmTimeMillis";
    public static final String START_TIME_ARG = "StartTimeArgument";
    public static final String END_TIME_ARG = "EndTimeArgument";
    public static final String MAIN_IMAGE_STATUS = "MainActivityDrawing";
    public static final int NO_ALARM_RUNNING = 1;
    public static final int NON_SCHEDULE_ALARM_RUNNING = 2;
    public static final int NON_SCHEDULE_TIME_TO_STAND = 3;
    public static final int NON_SCHEDULE_STOOD_UP = 4;
    public static final int NON_SCHEDULE_PAUSED = 5;
    public static final int SCHEDULE_RUNNING = 6;
    public static final int SCHEDULE_TIME_TO_STAND = 7;
    public static final int SCHEDULE_STOOD_UP = 8;
    public static final int SCHEDULE_PAUSED = 9;
    public static final String INTENT_MAIN_IMAGE = "UpdateMainImage";
    public static final String USER_ALERT_LED = "UserAlertLED";
    public static final String USER_ALERT_VIBRATE = "UserAlertVibrate";
    public static final String USER_ALERT_SOUND = "UserAlertSound";
    public static final String USER_ALERT_FREQUENCY = "UserAlertFrequency";
    public static final String USER_FREQUENCY = "UserFrequency";
    public static final String USER_DELAY = "UserDelay";
    public static final String VIBRATE_SILENT = "VibrateOnSilent";
    public static final String END_ALARM_SERVICE = "EndAlarmService";
    public static final String STOOD_RESULTS = "STOOD_RESULTS";
    public static final String MAIN_ACTIVITY_VISIBILITY_STATUS = "MainVisibilityStatus";
    public static final String PRAISE_FOR_USER = "PraiseForUser";
    public static final String UPDATE_NEXT_ALARM_TIME = "UpdateNextAlarmTime";
    public static final String STAND_DETECTOR = "StandDetector";
    public static final String LAST_STEP = "LastStep";
    public static final int STOOD_BEFORE = 1;
    public static final int TAPPED_NOTIFICATION = 2;
    public static final int TAPPED_ACTIVITY = 3;
    public static final int STOOD_AFTER = 4;
    public static final String STOOD_METHOD = "HowUserStood";
    public static final String STAND_DETECTOR_RESULT = "StandDetectorResult";
    public static final String STAND_DETECTED = "StandDetected";
    public static final String UPDATE_ACTION_BAR = "UpdateActionBar";
    public static final String PAUSE_TIME = "PauseTimeAmount";
    public static final String PAUSED_UNTIL_TIME = "PausedUntilTime";
    public static final String UI_EVENT = "UI Event";
    public static final String ALARM_PROCESS_EVENT = "Alarm Process Event";
    public static final String SCHEUDULE_LIST_EVENT = "Schedule List Event";
}
