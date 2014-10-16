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
    public static final String ALARM_SCHEDULE_DELETED = "AlarmDeleted";
    public static final String SELECTED_ALARM_SCHEDULE = "SelectedAlarmSchedule";
    public static final String EDITED_ALARM_SCHEDULE = "EditedAlarmSchedule";
    public static final String EDITED_ALARM_POSITION = "EditedAlarmPosition";
    public static final String ACTIVITY_RESULT = "ActivityResult";
    public static final String NEW_ALARM_SCHEDULE = "NewAlarmSchedule";
    public static final String START_TIME_ARG = "StartTimeArgument";
    public static final String END_TIME_ARG = "EndTimeArgument";
    public static final String MAIN_IMAGE_STATUS = "MainActivityDrawing";
    public static final int NO_ALARM_RUNNING = 1;
    public static final int NON_SCHEDULE_ALARM_RUNNING = 2;
    public static final int SCHEDULE_RUNNING = 3;
    public static final int SCHEDULE_TIME_TO_STAND = 4;
    public static final int SCHEDULE_STOOD_UP = 5;
    public static final String INTENT_MAIN_IMAGE = "UpdateMainImage";
}
