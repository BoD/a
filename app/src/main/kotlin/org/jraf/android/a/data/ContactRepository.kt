/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2023-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.a.data

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jraf.android.a.R

class ContactRepository(private val context: Context) {
    data class Contact(
        val contactId: Long,
        val lookupKey: String,
        val displayName: String,
        val photoDrawable: Drawable,
        val phoneNumber: String?,
    )

    suspend fun getStarredContacts(): List<Contact> {
        return if (ContextCompat.checkSelfPermission(context, permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            emptyList()
        } else {
            withContext(Dispatchers.IO) {
                context.contentResolver.query(
                    Contacts.CONTENT_URI,
                    arrayOf(
                        Contacts._ID,
                        Contacts.LOOKUP_KEY,
                        Contacts.DISPLAY_NAME,
                    ),
                    "${Contacts.STARRED} = 1",
                    null,
                    null,
                )?.use { cursor ->
                    buildList<Contact> {
                        while (cursor.moveToNext()) {
                            val contactId = cursor.getLong(0)
                            val lookupKey = cursor.getString(1)
                            add(
                                Contact(
                                    contactId = contactId,
                                    lookupKey = lookupKey,
                                    displayName = cursor.getString(2),
                                    photoDrawable = getPhotoDrawable(
                                        Contacts.getLookupUri(contactId, lookupKey)
                                    ),
                                    phoneNumber = getPhoneNumber(contactId)
                                )
                            )
                        }
                    }
                } ?: emptyList()
            }
        }
    }

    private fun getPhotoDrawable(lookupUri: Uri): Drawable {
        return Contacts.openContactPhotoInputStream(
            context.contentResolver,
            lookupUri,
            false
        )?.use { inputStream ->
            Drawable.createFromStream(inputStream, null)
        } ?: AppCompatResources.getDrawable(context, R.drawable.ic_contact)!!
    }

    private fun getPhoneNumber(contactId: Long): String? {
        return context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY + " DESC",
        )?.use { cursor ->
            if (cursor.moveToNext()) {
                cursor.getString(0)
            } else {
                null
            }
        }
    }

}
