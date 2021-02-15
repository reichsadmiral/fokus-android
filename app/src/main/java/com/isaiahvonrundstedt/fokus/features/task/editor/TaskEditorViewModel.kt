package com.isaiahvonrundstedt.fokus.features.task.editor

import android.content.ClipboardManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaiahvonrundstedt.fokus.database.dao.ScheduleDAO
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.attachments.Attachment
import com.isaiahvonrundstedt.fokus.features.schedule.Schedule
import com.isaiahvonrundstedt.fokus.features.subject.Subject
import com.isaiahvonrundstedt.fokus.features.task.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val clipboardManager: ClipboardManager,
    private val scheduleDao: ScheduleDAO,
    private val repository: TaskRepository
): ViewModel() {

    private val _task: MutableLiveData<Task> = MutableLiveData(Task())
    private val _attachments: MutableLiveData<ArrayList<Attachment>> = MutableLiveData(arrayListOf())
    private val _subject: MutableLiveData<Subject?> = MutableLiveData(null)

    val task: LiveData<Task> = _task
    val attachments: LiveData<ArrayList<Attachment>> = _attachments
    val subject: LiveData<Subject?> = _subject

    var schedules: ArrayList<Schedule> = arrayListOf()

    fun getTask(): Task? {
        return task.value
    }
    fun setTask(task: Task?) {
        _task.value = task
    }

    fun getSubject(): Subject? {
        return subject.value
    }
    fun setSubject(subject: Subject?) {
        _subject.value = subject

        if (subject != null) {
            fetchSchedulesFromDatabase(subject.subjectID)
            setTaskSubjectID(subject.subjectID)
        } else {
            schedules.clear()
            setTaskSubjectID(null)
        }
    }

    fun getAttachments(): List<Attachment> {
        return attachments.value ?: emptyList()
    }
    fun setAttachments(items: ArrayList<Attachment>) {
        _attachments.value = items
    }
    fun addAttachment(attachment: Attachment) {
        val items = ArrayList(getAttachments())
        items.add(attachment)
        setAttachments(items)
    }
    fun removeAttachment(attachment: Attachment) {
        val items = ArrayList(getAttachments())
        items.add(attachment)
        setAttachments(items)
    }


    fun getID(): String? {
        return getTask()?.taskID
    }

    fun getName(): String? {
        return getTask()?.name
    }
    fun setName(name: String?) {
        val task = getTask()
        task?.name = name
        setTask(task)
    }

    fun getDueDate(): ZonedDateTime? {
        return getTask()?.dueDate
    }
    fun setDueDate(dueDate: ZonedDateTime?) {
        val task = getTask()
        task?.dueDate = dueDate
        setTask(task)
    }

    fun getTaskSubjectID(): String? {
        return getTask()?.subject
    }
    fun setTaskSubjectID(id: String?) {
        val task = getTask()
        task?.subject = id
        setTask(task)
    }

    fun getImportant(): Boolean {
        return getTask()?.isImportant == true
    }
    fun setImportant(isImportant: Boolean) {
        val task = getTask()
        task?.isImportant = isImportant
        setTask(task)
    }

    fun getFinished(): Boolean {
        return getTask()?.isFinished == true
    }
    fun setFinished(isFinished: Boolean) {
        val task = getTask()
        task?.isFinished = isFinished
        setTask(task)
    }

    fun getNotes(): String? {
        return getTask()?.notes
    }
    fun setNotes(notes: String?) {
        val task = getTask()
        task?.notes = notes
        setTask(task)
    }

    fun hasFileAttachment(): Boolean {
        return getAttachments().any {
            it.type != Attachment.TYPE_WEBSITE_LINK
        }
    }

    fun fetchRecentItemFromClipboard(): String
            = clipboardManager.primaryClip?.getItemAt(0)?.text.toString()

    fun setNextMeetingForDueDate() {
        setDueDate(getDateTimeForNextMeeting())
    }

    fun setClassScheduleAsDueDate(schedule: Schedule) {
        setDueDate(schedule.startTime?.let {
            Schedule.getNearestDateTime(schedule.daysOfWeek, it)
        })
    }

    private fun getDateTimeForNextMeeting(): ZonedDateTime? {
        val currentDate = LocalDate.now()
        val individualDates = mutableListOf<Schedule>()

        // Create new instance of schedule with
        // one day of week each
        schedules.forEach {
            it.parseDaysOfWeek().forEach { day ->
                val newSchedule = Schedule(startTime = it.startTime,
                    endTime = it.endTime)
                newSchedule.daysOfWeek = day
                individualDates.add(newSchedule)
            }
        }

        // Map the schedules to their respective
        // dateTime instances
        val dates = individualDates.map {
            it.startTime?.let { time -> Schedule.getNearestDateTime(it.daysOfWeek, time) }

        }
        if (dates.isEmpty())
            return null

        // Get the nearest date
        var targetDate = dates[0]
        dates.forEach {
            if (currentDate.isAfter(it?.toLocalDate()) && targetDate?.isBefore(it) == true)
                targetDate = it
        }

        return targetDate
    }

    private fun fetchSchedulesFromDatabase(id: String) = viewModelScope.launch {
        schedules.addAll(scheduleDao.fetchUsingID(id))
    }

    fun insert() = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        getTask()?.let {
            repository.insert(it, getAttachments())
        }
    }

    fun update() = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        getTask()?.let {
            repository.update(it, getAttachments())
        }
    }
}