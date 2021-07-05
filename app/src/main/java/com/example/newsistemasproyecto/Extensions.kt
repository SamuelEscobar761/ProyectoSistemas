package com.example.newsistemasproyecto.Mapping

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

fun String.stripAccents(): String{
    if (this == null) {
        return "";
    }

    val chars: CharArray = this.toCharArray()

    var sb = StringBuilder(this)
    var cont: Int = 0

    while (chars.size > cont) {
        var c: Char = chars[cont]
        var c2:String = c.toString()
        c2 = c2.replace("á", "a")
        c2 = c2.replace("é", "e")
        c2 = c2.replace("í", "i")
        c2 = c2.replace("ó", "o")
        c2 = c2.replace("ú", "u")
        c = c2.single()
        sb.setCharAt(cont, c)
        cont++

    }
    return sb.toString()
}

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}
fun showShortMessage(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun showLongMessage(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}