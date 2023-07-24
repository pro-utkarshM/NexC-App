package com.example.nittalk.ui.editprofile

import android.app.Activity
import android.net.Uri
import com.example.nittalk.data.User
import com.example.nittalk.firebase.FirebaseSource
import javax.inject.Inject

class EditProfileRepository @Inject constructor(private val firebaseSource: FirebaseSource) {

    val currentUser = firebaseSource.currentUser()
    val progress = firebaseSource.progress
    val enable = firebaseSource.enable

    suspend fun uploadImage(imageUri: Uri, userId: String, activity: Activity) =
        firebaseSource.uploadImage(imageUri, userId, activity)

    suspend fun saveUserToDB(user: User) =
        firebaseSource.saveUserToDB(user)

    suspend fun imageDownloadUrl(userId: String): String =
        firebaseSource.getProfileImageDownloadUrl(userId)

    suspend fun updateFirebaseUser(user: User) = firebaseSource.updateFirebaseUser(user)

    suspend fun changeUserGroup(oldUserBranch: String, oldUserSemester: String, updatedUser: User, activity: Activity) =
        firebaseSource.changeUserGroup(oldUserBranch = oldUserBranch, oldUserSemester = oldUserSemester, updatedUser = updatedUser, activity = activity)

}