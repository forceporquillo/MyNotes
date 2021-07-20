package com.force.codes.mynotes

import android.app.Application
import com.force.codes.mynotes.data.db.MyTaskDatabase

class MyNotesApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // scope the database to the application's
        // lifecycle to prevent memory leaks
        // and guarantees singleton.
        MyTaskDatabase.createInstance(this)
    }
}