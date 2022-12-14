/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.systemui.statusbar.notification.collection.coordinator

import com.android.systemui.log.LogBuffer
import com.android.systemui.log.LogLevel
import com.android.systemui.log.dagger.NotificationLog
import javax.inject.Inject

class PreparationCoordinatorLogger @Inject constructor(
    @NotificationLog private val buffer: LogBuffer
) {
    fun logNotifInflated(key: String) {
        buffer.log(TAG, LogLevel.DEBUG, {
            str1 = key
        }, {
            "NOTIF INFLATED $str1"
        })
    }

    fun logInflationAborted(key: String, reason: String) {
        buffer.log(TAG, LogLevel.DEBUG, {
            str1 = key
            str2 = reason
        }, {
            "NOTIF INFLATION ABORTED $str1 reason=$str2"
        })
    }

    fun logDoneWaitingForGroupInflation(groupKey: String) {
        buffer.log(TAG, LogLevel.DEBUG, {
            str1 = groupKey
        }, {
            "Finished inflating all members of group $str1, releasing group"
        })
    }

    fun logGroupInflationTookTooLong(groupKey: String) {
        buffer.log(TAG, LogLevel.WARNING, {
            str1 = groupKey
        }, {
            "Group inflation took too long for $str1, releasing children early"
        })
    }

    fun logDelayingGroupRelease(groupKey: String, childKey: String) {
        buffer.log(TAG, LogLevel.DEBUG, {
            str1 = groupKey
            str2 = childKey
        }, {
            "Delaying release of group $str1 because child $str2 is still inflating"
        })
    }
}

private const val TAG = "PreparationCoordinator"