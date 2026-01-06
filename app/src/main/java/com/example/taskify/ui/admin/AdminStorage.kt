package com.example.taskify.ui.admin

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AdminStorage(private val context: Context) {

    private val prefs = context.getSharedPreferences("taskify_admin_storage", Context.MODE_PRIVATE)

    private val KEY_TEACHERS = "teachers_json"
    private val KEY_SUBJECT_ASSIGNMENTS = "subject_assignments_json"
    private val KEY_NEXT_TEACHER_ID = "next_teacher_id"

    fun nextTeacherId(): Long {
        val current = prefs.getLong(KEY_NEXT_TEACHER_ID, 1L)
        prefs.edit().putLong(KEY_NEXT_TEACHER_ID, current + 1L).apply()
        return current
    }

    fun saveTeacher(
        teacherId: Long,
        firstName: String,
        middleName: String?,
        lastName: String,
        email: String,
        subjectNames: List<String>
    ) {
        val list = loadTeachersInternal().toMutableList()
        val existingIndex = list.indexOfFirst { it.teacherId == teacherId }
        val record = TeacherRecord(
            teacherId = teacherId,
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            email = email,
            subjectNames = subjectNames
        )

        if (existingIndex >= 0) list[existingIndex] = record else list.add(record)
        saveTeachersInternal(list)
    }

    fun getAllTeachers(): List<TeacherRecord> {
        return loadTeachersInternal()
            .sortedWith(compareBy<TeacherRecord> { it.lastName.lowercase() }.thenBy { it.firstName.lowercase() })
    }

    fun getTeacherById(teacherId: Long): TeacherRecord? {
        return loadTeachersInternal().firstOrNull { it.teacherId == teacherId }
    }

    fun assignSubjectsToTeacher(teacherId: Long, subjectNames: List<String>) {
        val map = loadSubjectAssignmentsInternal().toMutableMap()
        subjectNames.forEach { subj -> map[subj] = teacherId }
        saveSubjectAssignmentsInternal(map)
    }

    fun getAssignedTeacherIdForSubject(subjectName: String): Long? {
        return loadSubjectAssignmentsInternal()[subjectName]
    }

    private fun loadTeachersInternal(): List<TeacherRecord> {
        val raw = prefs.getString(KEY_TEACHERS, null) ?: return emptyList()
        val arr = runCatching { JSONArray(raw) }.getOrNull() ?: return emptyList()

        val out = ArrayList<TeacherRecord>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val subjArr = o.optJSONArray("subjectNames") ?: JSONArray()
            val subjects = ArrayList<String>(subjArr.length())
            for (j in 0 until subjArr.length()) subjects.add(subjArr.optString(j))

            out.add(
                TeacherRecord(
                    teacherId = o.optLong("teacherId"),
                    firstName = o.optString("firstName"),
                    middleName = o.optString("middleName").takeIf { it.isNotBlank() },
                    lastName = o.optString("lastName"),
                    email = o.optString("email"),
                    subjectNames = subjects
                )
            )
        }
        return out
    }

    private fun saveTeachersInternal(list: List<TeacherRecord>) {
        val arr = JSONArray()
        list.forEach { t ->
            val o = JSONObject()
            o.put("teacherId", t.teacherId)
            o.put("firstName", t.firstName)
            o.put("middleName", t.middleName ?: "")
            o.put("lastName", t.lastName)
            o.put("email", t.email)

            val subjArr = JSONArray()
            t.subjectNames.forEach { subjArr.put(it) }
            o.put("subjectNames", subjArr)

            arr.put(o)
        }
        prefs.edit().putString(KEY_TEACHERS, arr.toString()).apply()
    }

    private fun loadSubjectAssignmentsInternal(): Map<String, Long> {
        val raw = prefs.getString(KEY_SUBJECT_ASSIGNMENTS, null) ?: return emptyMap()
        val obj = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyMap()

        val out = HashMap<String, Long>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            out[k] = obj.optLong(k)
        }
        return out
    }

    private fun saveSubjectAssignmentsInternal(map: Map<String, Long>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_SUBJECT_ASSIGNMENTS, obj.toString()).apply()
    }
}
