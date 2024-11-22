package com.osucad.server.messageOrderer

import com.osucad.server.multiplayer.RawOperationMessage

class QueuedMessage(val messages: List<RawOperationMessage>)