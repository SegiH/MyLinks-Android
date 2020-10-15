package com.segihovav.mylinks_android

import android.view.MotionEvent

interface OnActivityTouchListener {
     fun getTouchCoordinates(ev: MotionEvent?)
}