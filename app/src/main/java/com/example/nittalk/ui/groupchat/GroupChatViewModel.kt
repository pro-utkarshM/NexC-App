package com.example.nittalk.ui.groupchat

import android.content.Context
import androidx.lifecycle.*
import com.example.nittalk.data.Group
import com.example.nittalk.data.Message
import com.example.nittalk.data.PreferencesManager
import com.example.nittalk.firebase.FirebaseUtil
import com.example.nittalk.util.Constant.GROUP_SELECTED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val groupChatRepository: GroupChatRepository,
    private val preferencesManager: PreferencesManager,
    private val firebaseUtil: FirebaseUtil
) :
    ViewModel() {

    val currentUserUid = groupChatRepository.currentUser!!.uid

    val currentUserFromDB = groupChatRepository.getCurrentUserFromDB()

//    @ExperimentalCoroutinesApi
//    fun getUserById(userId: String) =
//        groupChatRepository.getUserById(userId)

    val currentUserGroups get() = run {
        val groups = MutableLiveData<List<Group>>()
        viewModelScope.launch {
            groups.postValue(groupChatRepository.getUserGroup(currentUserUid).first())
        }
        groups
    }

    private val groupPref = groupChatRepository.getGroupPref()

    private val groupSelected = preferencesManager.groupSelected

    val currentGroup = groupSelected.flatMapLatest {
        groupChatRepository.getGroupById(it)
    }

    val onlineGroupMembers = groupChatRepository.getGroupOnlineMembers().asLiveData()

    val offlineGroupMembers = groupChatRepository.getGroupOfflineMembers().asLiveData()

    val groupName = groupSelected.flatMapLatest { groupSelected ->
        groupChatRepository.getGroupName(groupSelected)
    }.asLiveData()

    val selectedGroupId
        get() = run {
            var id = ""
            viewModelScope.launch {
                id = groupSelected.first()
            }
            id
        }

    fun updateGroupSelected(groupId: String) =
        viewModelScope.launch {
            preferencesManager.updateGroupSelected(GROUP_SELECTED, groupId)
        }

    val channelName = groupSelected.flatMapLatest { groupSelected ->
        val channelId =
            groupPref.first().find { it.groupSelectedId == groupSelected }?.channelSelectedId
                ?: groupSelected.first() + "General"
        groupChatRepository.getChannelName(groupSelected, channelId)
    }.asLiveData()

    val channelSelected = groupPref.flatMapLatest { serverSelectedList ->
        val channelSelectedFlow = MutableStateFlow(groupSelected.first() + "General")
        channelSelectedFlow.value =
            serverSelectedList.find { it.groupSelectedId == groupSelected.first() }?.channelSelectedId
                ?: groupSelected.first() + "General"
        channelSelectedFlow
    }.asLiveData()

    fun update(groupSelectedId: String) = viewModelScope.launch(Dispatchers.IO) {
        groupChatRepository.update(groupSelectedId)
    }

    fun updateChannelSelected(groupSelectedId: String, channelSelectedId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            groupChatRepository.updateChannelSelected(groupSelectedId, channelSelectedId)
        }

    val messages = groupPref.flatMapLatest { serverSelectedList ->
        val channelId =
            serverSelectedList.find { it.groupSelectedId == groupSelected.first() }?.channelSelectedId
                ?: groupSelected.first() + "General"
        groupChatRepository.getMessages(groupSelected.first(), channelId)
    }

    val textChannels = groupSelected.flatMapLatest {
        groupChatRepository.getGroupTextChannels(it)
    }

    val voiceChannels = groupSelected.flatMapLatest {
        groupChatRepository.getGroupVoiceChannels(it)
    }

    fun sendMessage(messageText: String, imageUrl: String, repliedTo: Message?) =
        viewModelScope.launch {
            groupChatRepository.sendMessage(
                groupPref.first().find { it.channelSelectedId == channelSelected.asFlow().first() }!!,
                messageText, imageUrl, repliedTo, currentUserFromDB.first()
            )
        }

    fun editMessage(messageText: String, message: Message) =
        viewModelScope.launch {
            groupChatRepository.editMessage(
                groupPref.first().find { it.channelSelectedId == channelSelected.asFlow().first() }!!,
                messageText, message
            )
        }

    fun deleteMessage(message: Message) =
        viewModelScope.launch {
            groupChatRepository.deleteMessage(
                groupPref.first().find { it.channelSelectedId == channelSelected.asFlow().first() }!!,
                message
            )
        }

    val currentUserToken =
        firebaseUtil.getCurrentUserToken(currentUserUid)

    fun sendNotification(context: Context, title: String, message: String, userId: String,currentUserToken: String) =
        firebaseUtil.sendNotification(context, title, message, userId, currentUserToken)

}