package com.example.contactsapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database( entities = [Contact::class], version = 1, exportSchema = false)
abstract class ContactDatabase: RoomDatabase() {
    abstract fun contactDao(): ContactDao
}