package com.example.contactsapp

import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<Contact>>  = contactDao.getAllContacts()

    suspend fun insert(contact: Contact){
        contactDao.insert(contact)
    }

    suspend fun update(contact: Contact){
        contactDao.update(contact)
    }

    suspend fun delete(contact: Contact){
        contactDao.delete(contact)
    }
}