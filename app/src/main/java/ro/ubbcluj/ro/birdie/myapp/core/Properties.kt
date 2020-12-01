package ro.ubbcluj.ro.birdie.myapp.core

import androidx.lifecycle.MutableLiveData
import java.net.MalformedURLException

class Properties protected constructor() {
    var internetActive : MutableLiveData<Boolean> = MutableLiveData(false)
    var toastMessage : MutableLiveData<String> = MutableLiveData()

    companion object {
        private var mInstance: Properties? = null

        @get:Synchronized
        val instance: Properties
            get() {
                if (null == mInstance) {
                    mInstance = Properties()
                }
                return mInstance!!
            }
    }
}