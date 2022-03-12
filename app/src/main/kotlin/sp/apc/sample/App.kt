package sp.apc.sample

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        println("on -> create app")
        // todo
    }
}
