package bms.buchhaltung.utils

import java.security.MessageDigest


//import com.ionspin.kotlin.crypto.LibsodiumInitializer

object Crypto {

    /*init {
        runBlocking {
            LibsodiumInitializer.initialize()
        }
    }*/

    private val key = "oosonou2eevahphaec1Ereejae1keebeik2i".encodeToByteArray()

    //@ExperimentalUnsignedTypes
    //fun hash(data: UByteArray) = GenericHash.genericHash(data, 64, key)

    fun hash(data: ByteArray): ByteArray {
        val hashable = ByteArray(data.size + key.size)
        data.copyInto(hashable)
        key.copyInto(hashable, data.size)
        val digest = MessageDigest.getInstance("SHA-256")

        return digest.digest(data)
    }

}
