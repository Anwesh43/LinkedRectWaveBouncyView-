package com.anwesh.uiprojects.rectwavebouncyview

/**
 * Created by anweshmishra on 13/02/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val parts : Int = 5
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#3F51B5")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20L / parts
val scGap : Float = 0.02f / parts

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawRectWave(i : Int, scale : Float, w : Float, size : Float, paint : Paint) {
    val sf : Float = scale.sinify().divideScale(i, parts)
    val sf1 : Float = sf.divideScale(0, 2)
    val sf2 : Float = sf.divideScale(1, 2)
    val gap : Float = w / parts
    val sj : Float = 1f - 2 * (i % 2)
    val si : Float = 1f - (i % 2)
    save()
    translate(gap * (i + 1), -size * si)
    drawLine(0f, 0f, gap * sf1, size * sf2 * sj, paint)
    restore()
}

fun Canvas.drawRectWaves(scale : Float, w : Float, size : Float, paint : Paint) {
    for (j in 0..(parts - 1)) {
        drawRectWave(j, scale, w, size, paint)
    }
}

fun Canvas.drawRWNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(0f, gap * (i + 1))
    drawRectWaves(scale, w, size, paint)
    restore()
}
