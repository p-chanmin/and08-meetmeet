package com.teameetmeet.meetmeet.presentation.addevent

import android.util.Log
import android.widget.RadioGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teameetmeet.meetmeet.R
import com.teameetmeet.meetmeet.data.repository.CalendarRepository
import com.teameetmeet.meetmeet.presentation.model.EventColor
import com.teameetmeet.meetmeet.presentation.model.EventNotification
import com.teameetmeet.meetmeet.presentation.model.EventRepeatTerm
import com.teameetmeet.meetmeet.presentation.model.EventTime
import com.teameetmeet.meetmeet.util.DateTimeFormat
import com.teameetmeet.meetmeet.util.toDateString
import com.teameetmeet.meetmeet.util.toLong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AddEventViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState: StateFlow<AddEventUiState> = _uiState

    private val _event = MutableSharedFlow<AddEventUiEvent>(
        extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event: SharedFlow<AddEventUiEvent> = _event

    fun eventSave() {
        viewModelScope.launch {
            _uiState.value.startTime.hour
            val startDateTime =
                _uiState.value.startDate
                    .plusHours(_uiState.value.startTime.hour.toLong())
                    .plusMinutes(_uiState.value.startTime.minute.toLong())
                    .toLong(ZoneId.systemDefault())
                    .toDateString(DateTimeFormat.ISO_DATE_TIME, ZoneId.of("UTC"))
            val endDateTime =
                _uiState.value.endDate
                    .plusHours(_uiState.value.endTime.hour.toLong())
                    .plusMinutes(_uiState.value.endTime.minute.toLong())
                    .toLong(ZoneId.systemDefault())
                    .toDateString(DateTimeFormat.ISO_DATE_TIME, ZoneId.of("UTC"))

            val repeatEndDate = _uiState.value.endDate.toLong(ZoneId.systemDefault())
                .toDateString(DateTimeFormat.ISO_DATE_TIME, ZoneId.of("UTC"))

            with(_uiState.value) {
                calendarRepository.addEvent(
                    title = eventName,
                    startDate = startDateTime,
                    endDate = endDateTime,
                    isJoinable = isJoinable,
                    isVisible = isOpen,
                    memo = memo,
                    repeatTerm = eventRepeat.value,
                    repeatFrequency = eventRepeatFrequency,
                    repeatEndDate = repeatEndDate,
                    color = color,
                    alarm = alarm,
                ).catch {
                    _event.emit(AddEventUiEvent.ShowMessage(R.string.add_event_err_fail))
                }.collectLatest {
                    _event.emit(AddEventUiEvent.FinishAddEventActivity)
                }
            }
        }
    }

    fun setEventName(name: CharSequence) {
        _uiState.update {
            it.copy(eventName = name.toString())
        }
    }

    fun setEventDate(startDate: LocalDateTime, endDate: LocalDateTime) {
        if (!endDate.isBefore(_uiState.value.eventRepeatEndDate)) {
            _uiState.update {
                it.copy(
                    startDate = startDate, endDate = endDate, eventRepeatEndDate = endDate
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        }

    }

    fun setEventMemo(memo: CharSequence) {
        _uiState.update {
            it.copy(memo = memo.toString())
        }
    }

    fun setEventOpen(isChecked: Boolean) {
        _uiState.update {
            it.copy(isOpen = isChecked)
        }
    }

    fun setEventJoinable(isChecked: Boolean) {
        _uiState.update {
            it.copy(isJoinable = isChecked)
        }
    }

    fun setEventAlarm(index: Int) {
        _uiState.update {
            it.copy(alarm = EventNotification.values()[index])
        }
    }

    fun setEventRepeat(index: Int) {
        _uiState.update {
            it.copy(eventRepeat = EventRepeatTerm.values()[index])
        }
    }

    fun setEventRepeatFrequency(frequency: String) {
        _uiState.update {
            it.copy(eventRepeatFrequency = frequency.toInt())
        }
    }

    fun setRepeatEndDate(repeatEndDate: LocalDateTime) {
        if (!repeatEndDate.isBefore(_uiState.value.endDate)) {
            _uiState.update {
                it.copy(eventRepeatEndDate = repeatEndDate)
            }
        }
    }

    fun setEventStartTime(hour: Int, min: Int) {
        _uiState.update {
            it.copy(startTime = EventTime(hour, min))
        }
    }

    fun setEventEndTime(hour: Int, min: Int) {
        _uiState.update {
            it.copy(endTime = EventTime(hour, min))
        }
    }


    fun setEventColor(radioGroup: RadioGroup, id: Int) {
        val index = radioGroup.indexOfChild(radioGroup.findViewById(id))
        _uiState.update {
            it.copy(color = EventColor.values()[index])
        }
        Log.d("test", uiState.value.toString())
    }
}