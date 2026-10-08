package com.mmocal.app

import android.app.Application
import com.mmocal.app.data.AccountsStore
import com.mmocal.app.data.GamesRepository

class MMOApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AccountsStore.init(this)
        GamesRepository.init(this)
    }
}
