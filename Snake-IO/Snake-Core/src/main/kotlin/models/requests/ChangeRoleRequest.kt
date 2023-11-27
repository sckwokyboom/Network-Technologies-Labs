package models.requests

import models.core.NodeRole

data class ChangeRoleRequest(
    val senderId : Int,
    val receiverId : Int,
    val senderRole: NodeRole,
    val receiverRole: NodeRole
)
