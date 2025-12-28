package com.example.taskify.ui

import android.content.Context
import com.example.taskify.R
import org.json.JSONArray

// Data class (RENAMED to avoid redeclaration issue)
data class PhoneCountry(
    val name: String,
    val iso: String,
    val phoneCode: String,
    val flag: String
)

object CountryUtils {

    fun loadCountries(context: Context): List<PhoneCountry> {
        val countries = mutableListOf<PhoneCountry>()

        val inputStream = context.resources.openRawResource(R.raw.countries)
        val json = inputStream.bufferedReader().use { it.readText() }

        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            val iso = obj.getString("iso")

            countries.add(
                PhoneCountry(
                    name = obj.getString("name"),
                    iso = iso,
                    phoneCode = obj.getString("phoneCode"),
                    flag = isoToFlag(iso)
                )
            )
        }

        return countries
    }

    private fun isoToFlag(iso: String): String {
        return iso.uppercase()
            .map { char ->
                Character.toChars(0x1F1E6 + (char.code - 'A'.code))
            }
            .joinToString("") { String(it) }
    }
}
