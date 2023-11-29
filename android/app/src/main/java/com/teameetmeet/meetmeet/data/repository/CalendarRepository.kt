package com.teameetmeet.meetmeet.data.repository

import com.teameetmeet.meetmeet.data.datasource.LocalCalendarDataSource
import com.teameetmeet.meetmeet.data.datasource.RemoteCalendarDataSource
import com.teameetmeet.meetmeet.data.local.database.entity.Event
import com.teameetmeet.meetmeet.data.network.entity.AddEventRequest
import com.teameetmeet.meetmeet.data.network.entity.EventResponse
import com.teameetmeet.meetmeet.data.toEvent
import com.teameetmeet.meetmeet.presentation.model.EventColor
import com.teameetmeet.meetmeet.presentation.model.EventNotification
import com.teameetmeet.meetmeet.util.DateTimeFormat
import com.teameetmeet.meetmeet.util.toDateString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.time.ZoneId
import javax.inject.Inject

class CalendarRepository @Inject constructor(
    private val localCalendarDataSource: LocalCalendarDataSource,
    private val remoteCalendarDataSource: RemoteCalendarDataSource
) {
    suspend fun getEvents(startDate: Long, endDate: Long): Flow<List<Event>> {
        try {
            syncEvents(startDate, endDate)
            println("get Events")
        } finally {
            return localCalendarDataSource.getEvents(startDate, endDate)
        }
    }

    suspend fun deleteEvents() {
        localCalendarDataSource.deleteAll()
    }

    fun addEvent(
        title: String,
        startDate: String,
        endDate: String,
        isJoinable: Boolean,
        isVisible: Boolean,
        memo: String,
        repeatTerm: String?,
        repeatFrequency: Int,
        repeatEndDate: String,
        color: EventColor,
        alarm: EventNotification
    ): Flow<Unit> {
        val request = AddEventRequest(
            title = title,
            startDate = startDate,
            endDate = endDate,
            isJoinable = isJoinable,
            isVisible = isVisible,
            alarmMinutes = alarm.minutes,
            memo = memo.ifEmpty { null },
            color = color.value,
            repeatTerm = repeatTerm,
            repeatFrequency = repeatFrequency,
            repeatEndDate = repeatEndDate
        )
        return remoteCalendarDataSource.addEvent(request)
            .catch {
            }
    }

    fun searchEvents(
        keyword: String?,
        startDate: String,
        endDate: String
    ): Flow<List<EventResponse>> {
        return remoteCalendarDataSource.searchEvents(keyword, startDate, endDate)
    }

    private suspend fun syncEvents(startDateTime: Long, endDateTime: Long) {
        println("sync events")
        remoteCalendarDataSource
            .getEvents(
                startDateTime.toDateString(DateTimeFormat.SERVER_DATE, ZoneId.of("UTC")),
                endDateTime.toDateString(DateTimeFormat.SERVER_DATE, ZoneId.of("UTC"))
            ).collect {
                localCalendarDataSource.deleteEvents(startDateTime, endDateTime)
                localCalendarDataSource.insertEvents(it.map(EventResponse::toEvent))
            }
    }
}