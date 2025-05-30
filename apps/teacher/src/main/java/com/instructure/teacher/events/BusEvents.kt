/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
@file:Suppress("unused")

package com.instructure.teacher.events

import com.instructure.canvasapi2.apis.InboxApi
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.models.postmodels.PendingSubmissionComment
import com.instructure.teacher.utils.EditDateGroups
import org.greenrobot.eventbus.EventBus

/**
 * A bus event which only allows each subscriber access to the event payload once. Designed to be
 * used as a sticky event, [RationedBusEvent] should be used in components which need to receive
 * specific events exactly once but which may not currently live in memory.
 *
 * Example: The Courses page and the All Courses page both display cached data (if available). If a
 * user opens a course from the Courses page and edits its name, both the Courses page and All
 * Courses page will need to force read from the network to avoid showing stale data. The Courses
 * page is in memory while the All Courses page is not. By sending a sticky [RationedBusEvent],
 * the Courses page can immediately refresh with new data and the All Courses page will know to
 * skip the cache the next time it is instantiated.
 *
 * @param[T] The type of payload held by this [RationedBusEvent] instance
 * @param[payload] The payload held by this [RationedBusEvent] instance
 * @param[skipId] (Optional) A subscriber ID to skip, useful when the class sending the event is
 * also subscribed to receive events of the same type but wishes to ignore this specific event.
 * Generally this ID will be the simple name of the subscribing class (e.g. [Class.getSimpleName])
 */
abstract class RationedBusEvent<out T>(protected val payload: T, skipId: String? = null) {

    /* IDs of subscribers to be skipped */
    private val skipIds = mutableSetOf<String>()

    init {
        skipId?.let { skip(it) }
    }

    /**
     * Adds a subscriberId to be skipped.
     *
     * @param[subscriberId] The subscriber ID to be skipped. Generally this ID will be the simple
     * name of the subscribing class (e.g. [Class.getSimpleName])
     */
    fun skip(subscriberId: String) {
        if (subscriberId !in skipIds) skipIds += subscriberId
    }

    /**
     * Returns the payload of this event, or null if a subscriber with the given [subscriberId]
     * has already received the payload.
     *
     * @param[subscriberId] The subscriber ID of the subscribing class. Generally this ID will be
     * the simple name of the subscribing class (e.g. [Class.getSimpleName])
     */
    fun onceOrNull(subscriberId: String): T? {
        if (subscriberId in skipIds) return null
        skipIds += subscriberId
        return payload
    }

    /**
     * Calls the provided [block] function, passing in the payload of this event, or does nothing
     * if a subscriber with the given [subscriberId] has already received the payload.
     *
     * @param[subscriberId] The subscriber ID of the subscribing class. Generally this ID will be
     * the simple name of the subscribing class (e.g. [Class.getSimpleName])
     * @param[block] A function which will receive this event's payload. This function will only be
     * called once per unique [subscriberId] per event.
     */
    fun once(subscriberId: String, block: (T) -> Unit) {
        if (subscriberId in skipIds) return
        skipIds += subscriberId
        block(payload)
    }

    /**
     * Uses the [skipIds] as a key to trigger if a payload should be returned. The opposite of the once function.
     *
     * @param[subscriberId] The subscriber ID of the subscribing class. Generally this ID will be
     * the simple name of the subscribing class (e.g. [Class.getSimpleName])
     * @param[block] A function which will receive this event's payload. This function will only be
     * called if the [subscriberId] exists in the [skipIds].
     */
    fun only(subscriberId: String, block: (T) -> Unit) {
        if (subscriberId in skipIds) block(payload) else return
    }

    /**
     * Ignores any skip ids and gives a payload.
     *
     * @param[block] A function which will receive this event's payload.
     */
    fun get(block: (T) -> Unit) {
        block(payload)
    }
}

/** A RationedBusEvent for Courses. @see [RationedBusEvent] */
class CourseUpdatedEvent(course: Course, skipId: String? = null) : RationedBusEvent<Course>(course, skipId)

/** A RationedBusEvent for Assignments. @see [RationedBusEvent] */
class AssignmentUpdatedEvent(assignmentId: Long, skipId: String? = null) : RationedBusEvent<Long>(assignmentId, skipId)

/** A RationedBusEvent for Assignments. @see [RationedBusEvent] */
class AssignmentDeletedEvent(assignmentId: Long, skipId: String? = null) : RationedBusEvent<Long>(assignmentId, skipId)

/** A RationedBusEvent for AssignmentOverride Assignees. @see [RationedBusEvent] */
class AssigneesUpdatedEvent(dates: EditDateGroups, skipId: String? = null) : RationedBusEvent<EditDateGroups>(dates, skipId)

/** A RationedBusEvent for Assignment graded events. @see [RationedBusEvent] */
class AssignmentGradedEvent(assignmentId: Long, skipId: String? = null) : RationedBusEvent<Long>(assignmentId, skipId)

/** A RationedBusEvent for Submission update events */
class SubmissionUpdatedEvent(staleSubmission: Submission, skipId: String? = null) : RationedBusEvent<Submission>(staleSubmission, skipId)

/** A RationedBusEvent for Assignment description changes. @see [RationedBusEvent] */
class AssignmentDescriptionEvent(description: String, skipId: String? = null) : RationedBusEvent<String>(description, skipId)

/** A RationedBusEvent for Quizzes. @see [RationedBusEvent] */
class QuizUpdatedEvent(quizId: Long, skipId: String? = null) : RationedBusEvent<Long>(quizId, skipId)

/** A RationedBusEvent for updating conversations, used between InboxFragment/MessageThreadFragment
 *  @see [RationedBusEvent] */
class ConversationUpdatedEvent(conversation: Conversation, val scope: InboxApi.Scope, skipId: String? = null) : RationedBusEvent<Conversation>(conversation, skipId)

/** A RationedBusEvent for updating conversations specifically for tablets, used between
 * InboxFragment/MessageThreadFragment.
 *  @see [RationedBusEvent] */
class ConversationUpdatedEventTablet(position: Int, val scope: InboxApi.Scope, skipId: String? = null) : RationedBusEvent<Int>(position, skipId)

/** A RationedBusEvent for the deletion of conversations, used between InboxFragment/MessageThreadFragment
 *  @see [RationedBusEvent] */
class ConversationDeletedEvent(skipId: String? = null) : RationedBusEvent<Any?>(skipId)

/** A RationedBusEvent adding a new message to the MessageThreadFragment. @see [RationedBusEvent] */
class MessageAddedEvent(shouldUpdate: Boolean, skipId: String? = null) : RationedBusEvent<Boolean>(shouldUpdate, skipId)

/** A RationedBusEvent for choosing message recipients. @see [RationedBusEvent] */
class ChooseMessageEvent(list: ArrayList<Recipient>, skipId: String? = null) : RationedBusEvent<ArrayList<Recipient>>(list, skipId)

/** A RationedBusEvent for updating discussions. @see [RationedBusEvent] */
class DiscussionUpdatedEvent(discussionTopicHeader: DiscussionTopicHeader, skipId: String? = null) : RationedBusEvent<DiscussionTopicHeader>(discussionTopicHeader, skipId)

/** Convenience function for posting this event to the EventBus */
fun RationedBusEvent<*>.post() = EventBus.getDefault().postSticky(this)

/** A RationedBusEvent for DiscussionEntry updates. @see [RationedBusEvent] */
class DiscussionEntryUpdatedEvent(discussionEntry: DiscussionEntry, skipId: String? = null) : RationedBusEvent<DiscussionEntry>(discussionEntry, skipId)

/** A RationedBusEvent for DiscussionTopicHeader changes. @see [RationedBusEvent] */
class DiscussionTopicHeaderEvent(discussionTopicHeader: DiscussionTopicHeader, skipId: String? = null) : RationedBusEvent<DiscussionTopicHeader>(discussionTopicHeader, skipId)

/** A RationedBusEvent for Deleted DiscussionTopicHeader. @see [RationedBusEvent] */
class DiscussionTopicHeaderDeletedEvent(discussionTopicHeaderId: Long, skipId: String? = null) : RationedBusEvent<Long>(discussionTopicHeaderId, skipId)

/** A RationedBusEvent for creating pages. @see [RationedBusEvent] */
class PageCreatedEvent(shouldUpdate: Boolean, skipId: String? = null) : RationedBusEvent<Boolean>(shouldUpdate, skipId)

/** A RationedBusEvent for updating pages. @see [RationedBusEvent] */
class PageUpdatedEvent(page: Page, skipId: String? = null) : RationedBusEvent<Page>(page, skipId)

/** A RationedBusEvent for deleted pages. @see [RationedBusEvent] */
class PageDeletedEvent(page: Page, skipId: String? = null) : RationedBusEvent<Page>(page, skipId)

/** A RationedBusEvent for updating to do count. @see [RationedBusEvent] */
class ToDoListUpdatedEvent(count: Int, skipId: String? = null) : RationedBusEvent<Int>(count, skipId)


class UploadMediaCommentUpdateEvent(updatedComments: MutableMap<String, MutableList<Pair<PendingSubmissionComment, SubmissionComment?>>>, skipId: String? = null)
: RationedBusEvent<MutableMap<String, MutableList<Pair<PendingSubmissionComment, SubmissionComment?>>>>(updatedComments, skipId)

class SubmissionCommentsUpdated(skipId: String? = null) : RationedBusEvent<Boolean>(true, skipId)

class SectionsUpdatedEvent

class SubmissionFilterChangedEvent(val filterIndex: Int = -1, val canvasContext: ArrayList<CanvasContext>? = null)

class CourseColorOverlayToggledEvent

class SyllabusUpdatedEvent(val content: String, val summaryAllowed: Boolean, skipId: String? = null) : RationedBusEvent<String>(content, skipId)
