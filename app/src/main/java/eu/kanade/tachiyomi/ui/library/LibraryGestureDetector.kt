package eu.kanade.tachiyomi.ui.library

import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout
import eu.kanade.tachiyomi.util.view.hide
import eu.kanade.tachiyomi.util.view.updateLayoutParams
import kotlinx.android.synthetic.main.filter_bottom_sheet.*
import kotlinx.android.synthetic.main.library_list_controller.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sign

class LibraryGestureDetector(private val controller: LibraryController) : GestureDetector
.SimpleOnGestureListener() {
    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val distance = ((e1?.rawX ?: 0f) - (e2?.rawX ?: 0f)) / 50
        val poa = 1.7f
        controller.category_hopper_frame.translationX = abs(distance).pow(poa) * -sign(distance)
        return super.onScroll(e1, e2, distanceX, distanceY)
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return super.onSingleTapUp(e)
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        var result = false
        val diffY = e2.y - e1.y
        val diffX = e2.x - e1.x
        val animator = controller.category_hopper_frame.animate().setDuration(150L)
        animator.translationX(0f)
        animator.withEndAction {
            controller.category_hopper_frame.translationX = 0f
        }
        if (abs(diffX) <= abs(diffY) &&
            abs(diffY) > SWIPE_THRESHOLD &&
            abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
        ) {
            if (diffY <= 0) {
                controller.showSheet()
            } else {
                controller.filter_bottom_sheet.sheetBehavior?.hide()
            }
        } else if (abs(diffX) >= abs(diffY) &&
            abs(diffX) > SWIPE_THRESHOLD * 5 &&
            abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
        ) {
            val hopperGravity = (controller.category_hopper_frame.layoutParams as CoordinatorLayout.LayoutParams).gravity
            if (diffX <= 0) {
                animator.translationX(
                    if (hopperGravity == Gravity.TOP or Gravity.LEFT) 0f
                    else (-(controller.view!!.width - controller.category_hopper_frame.width) / 2).toFloat()
                ).withEndAction {
                    controller.category_hopper_frame?.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                        gravity =
                            Gravity.TOP or (
                            if (gravity == Gravity.TOP or Gravity.RIGHT) {
                                controller.preferences.hopperGravity().set(1)
                                Gravity.CENTER
                            } else {
                                controller.preferences.hopperGravity().set(0)
                                Gravity.LEFT
                            }
                            )
                    }
                    savePrefs()
                }
            } else {
                animator.translationX(
                    if (hopperGravity == Gravity.TOP or Gravity.RIGHT) 0f
                    else ((controller.view!!.width - controller.category_hopper_frame.width) / 2).toFloat()
                ).withEndAction {
                    controller.category_hopper_frame?.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                        gravity =
                            Gravity.TOP or (
                            if (gravity == Gravity.TOP or Gravity.LEFT) {
                                controller.preferences.hopperGravity().set(1)
                                Gravity.CENTER
                            } else {
                                controller.preferences.hopperGravity().set(2)
                                Gravity.RIGHT
                            }
                            )
                    }
                    savePrefs()
                }
            }
            result = true
        }
        animator.start()
        return result
    }

    private fun savePrefs() {
        if (!controller.hasMovedHopper) {
            controller.preferences.shownHopperSwipeTutorial().set(true)
        }
        controller.hopperGravity = controller.preferences.hopperGravity().get()
        controller.category_hopper_frame?.translationX = 0f
    }

    private companion object {
        const val SWIPE_THRESHOLD = 50
        const val SWIPE_VELOCITY_THRESHOLD = 100
    }
}
