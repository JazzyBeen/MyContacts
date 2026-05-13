package com.xs.mycontacts.data.repository

import com.xs.mycontacts.domain.model.Contact
import com.xs.mycontacts.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ContactsRepositoryImpl @Inject constructor() : ContactsRepository{
    override fun getContacts(): Flow<List<Contact>> = flow {
        val forTest = listOf(Contact("1", "Torvalds","+79531566435"),
            Contact("2", "Tenenbaum","+79817046797"),
            Contact("3", "Torvalds","+79531566435")
        )
        emit(forTest)
    }
}