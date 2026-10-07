package com.edupixel.school

import android.app.Application
import com.edupixel.school.core.config.ApiConfig

class EduPixelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiConfig.init(this)
    }
}
