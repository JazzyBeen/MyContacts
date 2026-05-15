package com.xs.mycontacts.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.xs.mycontacts.domain.model.Contact
import com.xs.mycontacts.domain.repository.ContactsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
) : ContactsRepository{

    override fun getContacts(): Flow<List<Contact>> = flow {
        val contactList = mutableListOf<Contact>()

        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = if (idIndex >= 0) it.getString(idIndex) else ""
                val name = it.getString(nameIndex).takeIf { nameIndex >= 0 } ?: "Имени нет"
                val number = it.getString(numberIndex).takeIf { numberIndex >= 0 } ?: ""

                contactList.add(Contact(id = id, name = name, phoneNumber = number))
            }
        }
    emit(contactList)

    }.flowOn(Dispatchers.IO)
}