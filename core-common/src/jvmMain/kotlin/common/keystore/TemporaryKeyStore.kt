package common.keystore

interface TemporaryKeyStore {
    suspend fun save(key: String, value: Boolean)
    suspend fun get(key: String, default: Boolean): Boolean
    suspend fun save(key: String, value: String)
    suspend fun get(key: String, default: String): String
    suspend fun save(key: String, value: Int)
    suspend fun get(key: String, default: Int): Int
    suspend fun save(key: String, value: Float)
    suspend fun get(key: String, default: Float): Float
    suspend fun save(key: String, value: Double)
    suspend fun get(key: String, default: Double): Double
}
