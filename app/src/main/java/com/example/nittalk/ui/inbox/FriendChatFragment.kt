package com.example.nittalk.ui.inbox

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nittalk.R
import com.example.nittalk.data.Message
import com.example.nittalk.databinding.FragmentFriendChatBinding
import com.example.nittalk.ui.groupchat.MessageAdapter
import com.example.nittalk.ui.groupchat.OnMessageLongPress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendChatFragment: Fragment(R.layout.fragment_friend_chat), OnMessageLongPress {

    private var _binding: FragmentFriendChatBinding?= null
    private val binding get() = _binding!!
    private val friendChatViewModel by viewModels<FriendChatViewModel>()
    private val navArgs by navArgs<FriendChatFragmentArgs>()
    private lateinit var messageAdapter : MessageAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>
    private var repliedMessage: Message? = null
    private lateinit var currentUserName :String
    private lateinit var currentUserId: String

    private var imageUri : Uri?= null
    private var imageUrl :String?= null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFriendChatBinding.bind(view)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.messageBottomSheet)

        currentUserId = friendChatViewModel.currentUserId

        binding.apply {
            friendChatToolbar.title = navArgs.friendName
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        friendChatViewModel.currentUser.asLiveData().observe(viewLifecycleOwner) {
            currentUserName = it.name
        }

        binding.apply {

            blankView.setOnClickListener {
                hideMessageOptions()
            }

            messageEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }
                override fun afterTextChanged(p0: Editable?) {
                    val isEmpty = p0.toString().trim().isEmpty()
                    if (isEmpty) {
                        messageSendBtn.visibility = View.GONE
                    } else {
                        messageSendBtn.visibility = View.VISIBLE
                    }
                }
            })

            messageImageBtn.setOnClickListener {
                startCropActivity()
            }

            removeImageBtn.setOnClickListener {
                imageUri = null
                imageUrl = null
                imageContainer.visibility = View.GONE
            }

            messageSendBtn.setOnClickListener {
                val message = messageEditText.text.toString().trim()
                if (message != "") {
                    hideEditMessage()
                    messageEditText.setText("")
                    friendChatViewModel.sendPersonalMessage(
                        friendId = navArgs.friendId,
                        messageText = message,
                        imageUrl = imageUrl ?: "",
                        repliedTo = repliedMessage,
                        joinGroup = null
                    )
                    imageUri = null
                    imageUrl = null
                    imageContainer.visibility = View.GONE
                    friendChatViewModel.sendNotification(context = requireContext(), title = currentUserName, message = message, userId = navArgs.friendId)
                } else {
                    Toast.makeText(requireContext(), "Write Something", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setUpFriendChatRecyclerView()

    }

    private fun startCropActivity() {
        CropImage.activity()
            .setAspectRatio(16, 16)
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                imageUri = result.uri

                binding.apply {
                    msgImage.setImageURI(imageUri)
                    imageContainer.visibility = View.VISIBLE
                    messageSendBtn.isEnabled = false
                }
                FirebaseStorage.getInstance().reference.child("images/$imageUri").putFile(imageUri!!)
                    .addOnProgressListener {
                        val progress = 100.0 * it.bytesTransferred / it.totalByteCount
                        val currentProgress = progress.toInt()
                        binding.imageUploadProgressBar.progress = currentProgress
                    }
                    .addOnSuccessListener {
                        FirebaseStorage.getInstance().reference.child("images/$imageUri").downloadUrl
                            .addOnSuccessListener { uri ->
                                imageUrl = uri.toString()
                                binding.messageSendBtn.isEnabled = true
                            }
                    }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(requireContext(), result.error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpFriendChatRecyclerView() {
        messageAdapter = MessageAdapter(this)
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.friendChatRecyclerView.apply {
            adapter = messageAdapter
            layoutManager = mLayoutManager
        }
        mLayoutManager.stackFromEnd = true
        friendChatViewModel.getFriendMessages(navArgs.friendId).observe(viewLifecycleOwner) {
            messageAdapter.submitList(it)
            mLayoutManager.smoothScrollToPosition(binding.friendChatRecyclerView, null, it.size)
        }
    }

    override fun joinServer(groupId: String) {
        friendChatViewModel.joinServer(groupId = groupId, userId = currentUserId, activity = requireActivity())
            .invokeOnCompletion {
                Toast.makeText(requireContext(), "Server Joined!!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun showMessageOptions(message: Message, lastMessage: Message, nextLastMessage: Message?) {
        showOptions(message)
        binding.apply {
            editButton.setOnClickListener {
                binding.apply {
                    editMessageContainer.visibility = View.VISIBLE
                    messageEditBtn.visibility = View.VISIBLE
                    messageSendBtn.visibility = View.GONE
                    messageImageBtn.visibility = View.GONE
                    messageEditText.setText(message.message)
                    cancelEditingMessage.setOnClickListener {
                        messageEditText.setText("")
                        editMessageContainer.visibility = View.GONE
                        messageImageBtn.visibility = View.VISIBLE
                        messageEditBtn.visibility = View.GONE
                        messageSendBtn.visibility = View.GONE
                    }
                    messageEditBtn.setOnClickListener {
                        if (messageEditText.text.trim().isEmpty()) {
                            Toast.makeText(requireContext(), "Message Can't be Empty", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            friendChatViewModel.editMessage(friendId = navArgs.friendId, message = message, messageText = messageEditText.text.trim().toString(), lastMessage = lastMessage)
                            messageEditText.setText("")
                            Toast.makeText(requireContext(), "Message Edited", Toast.LENGTH_SHORT).show()
                            editMessageContainer.visibility = View.GONE
                            messageImageBtn.visibility = View.VISIBLE
                            messageEditBtn.visibility = View.GONE
                        }
                    }
                }
                hideMessageOptions()
            }

            replyButton.setOnClickListener {
                repliedMessage = message
                hideMessageOptions()
                showEditMessage(message)
            }

            cancelReplyingMessage.setOnClickListener {
                hideEditMessage()
                repliedMessage = null
            }

            deleteButton.setOnClickListener {
                friendChatViewModel.deleteMessage(friendId = navArgs.friendId, message = message, lastMessage = lastMessage, nextMessage = nextLastMessage)
                hideMessageOptions()
            }
        }
    }

    private fun showEditMessage(message: Message) {
        binding.apply {
            replyingToText.text = message.senderName
            repliedMessageContainer.visibility = View.VISIBLE
        }
    }

    private fun hideEditMessage() {
        binding.apply {
            repliedMessageContainer.visibility = View.GONE
        }
    }

    private fun showOptions(message: Message) {
        binding.apply {
            if(friendChatViewModel.currentUserId != message.senderId) {
                editButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
            }
            else {
                editButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            blankView.visibility = View.VISIBLE
        }
    }

    private fun hideMessageOptions() {
        binding.apply {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            blankView.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}