package com.example.taskify.ui

import android.content.Context
import com.example.taskify.R
import org.json.JSONArray

data class PhoneCountry(
    val name: String,
    val iso: String,
    val phoneCode: String,
    val flag: String
)

object CountryUtils {

    fun loadCountries(context: Context): List<PhoneCountry> {
        val result = mutableListOf<PhoneCountry>()

        val json = context.resources
            .openRawResource(R.raw.countries)
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val name = obj.optString("name", "").trim()
            val iso = obj.optString("code", "").trim()

            val rawDialCode = when (val v = obj.opt("dial_code")) {
                is String -> v
                else -> ""
            }

            val cleanedDialCode = rawDialCode
                .replace(" ", "")   // removes spaces like "+1 684"
                .trim()

            if (name.isBlank() || iso.isBlank() || cleanedDialCode.isBlank()) continue
            if (!cleanedDialCode.startsWith("+")) continue

            result.add(
                PhoneCountry(
                    name = name,
                    iso = iso,
                    phoneCode = cleanedDialCode,
                    flag = isoToFlag(iso)
                )
            )
        }

        return result
            .distinctBy { it.iso }
            .sortedBy { it.name }
    }


    private fun isoToFlag(iso: String): String {
        return iso.uppercase()
            .map { char ->
                Character.toChars(0x1F1E6 + (char.code - 'A'.code))
            }
            .joinToString("") { String(it) }
    }
}
