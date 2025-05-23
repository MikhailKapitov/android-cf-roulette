package com.example.cf_roulette.util

import java.security.MessageDigest

object HashingUtil {

    fun sha256(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
