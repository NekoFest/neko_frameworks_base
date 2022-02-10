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

import android.app.NotificationChannel
import android.testing.AndroidTestingRunner
import android.testing.TestableLooper
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.statusbar.notification.collection.NotifPipeline
import com.android.systemui.statusbar.notification.collection.NotificationEntry
import com.android.systemui.statusbar.notification.collection.NotificationEntryBuilder
import com.android.systemui.statusbar.notification.collection.listbuilder.NotifSection
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifComparator
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSectioner
import com.android.systemui.statusbar.notification.collection.render.NodeController
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier.Companion.TYPE_IMPORTANT_PERSON
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier.Companion.TYPE_PERSON
import com.android.systemui.util.mockito.any
import com.android.systemui.util.mockito.withArgCaptor
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever

@SmallTest
@RunWith(AndroidTestingRunner::class)
@TestableLooper.RunWithLooper
class ConversationCoordinatorTest : SysuiTestCase() {
    // captured listeners and pluggables:
    private lateinit var promoter: NotifPromoter
    private lateinit var peopleSectioner: NotifSectioner
    private lateinit var peopleComparator: NotifComparator

    @Mock private lateinit var pipeline: NotifPipeline
    @Mock private lateinit var peopleNotificationIdentifier: PeopleNotificationIdentifier
    @Mock private lateinit var channel: NotificationChannel
    @Mock private lateinit var headerController: NodeController
    private lateinit var entry: NotificationEntry
    private lateinit var entryA: NotificationEntry
    private lateinit var entryB: NotificationEntry

    private lateinit var coordinator: ConversationCoordinator

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        coordinator = ConversationCoordinator(peopleNotificationIdentifier, headerController)
        whenever(channel.isImportantConversation).thenReturn(true)

        coordinator.attach(pipeline)

        // capture arguments:
        promoter = withArgCaptor {
            verify(pipeline).addPromoter(capture())
        }

        peopleSectioner = coordinator.sectioner
        peopleComparator = coordinator.comparator

        entry = NotificationEntryBuilder().setChannel(channel).build()

        val section = NotifSection(peopleSectioner, 0)
        entryA = NotificationEntryBuilder().setChannel(channel)
            .setSection(section).setTag("A").build()
        entryB = NotificationEntryBuilder().setChannel(channel)
            .setSection(section).setTag("B").build()
    }

    @Test
    fun testPromotesImportantConversations() {
        // only promote important conversations
        assertTrue(promoter.shouldPromoteToTopLevel(entry))
        assertFalse(promoter.shouldPromoteToTopLevel(NotificationEntryBuilder().build()))
    }

    @Test
    fun testInPeopleSection() {
        whenever(peopleNotificationIdentifier.getPeopleNotificationType(entry))
            .thenReturn(TYPE_PERSON)

        // only put people notifications in this section
        assertTrue(peopleSectioner.isInSection(entry))
        assertFalse(peopleSectioner.isInSection(NotificationEntryBuilder().build()))
    }

    @Test
    fun testComparatorIgnoresFromOtherSection() {
        val e1 = NotificationEntryBuilder().setId(1).setChannel(channel).build()
        val e2 = NotificationEntryBuilder().setId(2).setChannel(channel).build()

        // wrong section -- never classify
        assertThat(peopleComparator.compare(e1, e2)).isEqualTo(0)
        verify(peopleNotificationIdentifier, never()).getPeopleNotificationType(any())
    }

    @Test
    fun testComparatorPutsImportantPeopleFirst() {
        whenever(peopleNotificationIdentifier.getPeopleNotificationType(entryA))
            .thenReturn(TYPE_IMPORTANT_PERSON)
        whenever(peopleNotificationIdentifier.getPeopleNotificationType(entryB))
            .thenReturn(TYPE_PERSON)

        // only put people notifications in this section
        assertThat(peopleComparator.compare(entryA, entryB)).isEqualTo(-1)
    }

    @Test
    fun testComparatorEquatesPeopleWithSameType() {
        whenever(peopleNotificationIdentifier.getPeopleNotificationType(entryA))
            .thenReturn(TYPE_PERSON)
        whenever(peopleNotificationIdentifier.getPeopleNotificationType(entryB))
            .thenReturn(TYPE_PERSON)

        // only put people notifications in this section
        assertThat(peopleComparator.compare(entryA, entryB)).isEqualTo(0)
    }
}
