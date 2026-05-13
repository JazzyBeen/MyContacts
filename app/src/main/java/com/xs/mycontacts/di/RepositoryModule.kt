package com.xs.mycontacts.di

import com.xs.mycontacts.data.repository.ContactsRepositoryImpl
import com.xs.mycontacts.domain.repository.ContactsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindContactsRepository(contactsRepositoryImpl: ContactsRepositoryImpl): ContactsRepository
}