package com.xs.mycontacts.domain.repository

import com.xs.mycontacts.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun getContacts(): Flow<List<Contact>>
}