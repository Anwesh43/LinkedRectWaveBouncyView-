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

class RectWaveBouncyView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RWNode(var i : Int, val state : State = State()) {

        private var next : RWNode? = null
        private var prev : RWNode? = null

        init {

        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = RWNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRWNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RWNode {
            var curr : RWNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BouncyRectWave(var i : Int) {

        private val root : RWNode = RWNode(0)
        private var curr : RWNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RectWaveBouncyView) {

        private val animator : Animator = Animator(view)
        private val brw : BouncyRectWave = BouncyRectWave(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            brw.draw(canvas, paint)
            animator.animate {
                brw.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            brw.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : RectWaveBouncyView {
            val view : RectWaveBouncyView = RectWaveBouncyView(activity)
            activity.setContentView(view)
            return view
        }
    }
}