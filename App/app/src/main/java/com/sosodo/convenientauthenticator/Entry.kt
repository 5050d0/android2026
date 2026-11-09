package com.sosodo.convenientauthenticator

import java.util.UUID

enum class HashAlgorithm(val title: String) {
    SHA1("SHA1"),
    SHA256("SHA256"),
    SHA512("SHA512")
}

class Entry {
    var uuid: UUID = UUID.randomUUID()
    var secret: String = ""
    var issuer: String = ""
    var username: String = ""
    var hashAlgorithm: HashAlgorithm = HashAlgorithm.SHA1
    var period: Int = 30
    var digits: Int = 6

    constructor(
        secret: String,
        issuer: String,
        username: String,
        period: Int,
        hashAlgorithm: HashAlgorithm,
        digits: Int
    ) {
        this.secret = secret
        this.issuer = issuer
        this.username = username
        this.period = period
        this.hashAlgorithm = hashAlgorithm
        this.digits = digits
    }


    fun getCode(): Int {
        return 555444
    }


}