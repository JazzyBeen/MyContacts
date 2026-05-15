package com.xs.mycontacts.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.xs.mycontacts.IContactsCallback
import com.xs.mycontacts.IContactsManager
import android.content.ContentProviderOperation
import android.provider.ContactsContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DuplicateRemovalService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val binder = object : IContactsManager.Stub() {
        override fun removeDuplicates(callback: IContactsCallback?) {
            serviceScope.launch {
                try {
                    val duplicates = findDuplicates()

                    if (duplicates.isEmpty()) {
                        callback?.onNotFound()
                        return@launch
                    }

                    deleteContacts(duplicates)
                    callback?.onSuccess()

                } catch (e: Exception) {
                    Log.e("ContactsService", "Ошибка удаления", e)
                    callback?.onError(e.localizedMessage ?: "Неизвестная ошибка")
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun findDuplicates(): List<String> {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val contactsMap = mutableMapOf<String, MutableList<String>>()
        val duplicatesToDelete = mutableListOf<String>()

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idIdx)
                val name = cursor.getString(nameIdx) ?: ""
                val phone = cursor.getString(phoneIdx)?.replace(Regex("[^0-9+]"), "") ?: ""

                val key = "${name}_${phone}"
                if (!contactsMap.containsKey(key)) {
                    contactsMap[key] = mutableListOf()
                }
                contactsMap[key]?.add(id)
            }
        }

        for ((_, ids) in contactsMap) {
            if (ids.size > 1) {
                duplicatesToDelete.addAll(ids.drop(1))
            }
        }
        return duplicatesToDelete.distinct()
    }

    private fun deleteContacts(contactIds: List<String>) {
        val operations = ArrayList<ContentProviderOperation>()
        for (id in contactIds) {
            operations.add(
                ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                    .withSelection("${ContactsContract.RawContacts.CONTACT_ID} = ?", arrayOf(id))
                    .build()
            )
        }
        contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
    }
}