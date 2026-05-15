package com.xs.mycontacts.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.xs.mycontacts.IContactsCallback
import com.xs.mycontacts.IContactsManager
class DuplicateRemovalService : Service() {

    private val binder = object : IContactsManager.Stub() {

        override fun removeDuplicates(callback: IContactsCallback?) {
            Log.d("ContactsService", "Начат процесс поиска дубликатов...")

            try {
                Thread.sleep(2000)
                callback?.onSuccess()

            } catch (e: Exception) {
                callback?.onError(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}