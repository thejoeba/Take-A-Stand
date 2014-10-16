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

package com.sean.takeastand.alarmprocess;

/**
 * Created by Sean on 2014-09-03.
 */

public interface RepeatingAlarm
{
    /* This interface sets the required methods for the ScheduledRepeatingAlarm class and
    UnscheduledRepeatingAlarm class, which both implement this interface.  */
    public void setRepeatingAlarm();

    public void setShortBreakAlarm();

    public void delayAlarm();

    public void cancelAlarm();

    public void takeBreak();

}
