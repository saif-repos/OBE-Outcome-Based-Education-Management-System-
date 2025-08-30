package com.example.obe_mngt_sys.MODELS

data class AddPLOResponse(

    val Message: String,      // Message from the server
    val PLOId: Int,          // Auto-generated PLO ID
    val Description: String  // Description of the PLO
)
