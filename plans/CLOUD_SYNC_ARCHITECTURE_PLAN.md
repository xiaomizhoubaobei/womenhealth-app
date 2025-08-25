# LuminCore云端同步架构详细计划

## 1. 功能概述

### 1.1 目标与价值
- **数据安全保障**：确保用户数据在多设备间安全同步
- **无缝体验**：用户在不同设备上获得一致的使用体验
- **数据备份**：云端作为可靠的数据备份存储
- **隐私保护**：端到端加密确保数据隐私安全
- **离线优先**：支持离线使用，网络恢复时自动同步

### 1.2 核心功能
- **实时同步**：数据变更自动同步到云端
- **冲突解决**：智能处理多设备数据冲突
- **增量同步**：只同步变更部分，节省流量
- **版本控制**：支持数据版本管理和回滚
- **多云支持**：Firebase + 华为云双重保障
- **加密传输**：所有数据采用端到端加密

## 2. 技术架构设计

### 2.1 整体架构图
```
┌─────────────────────────────────────────────────────────────┐
│                      客户端架构                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │     UI      │  │  ViewModel  │  │     Repository      │  │
│  └──────┬──────┘  └──────┬──────┘  └──────────┬──────────┘  │
│         │                │                    │             │
│         └────────────────┼────────────────────┘             │
│                          │                                  │
│                          ▼                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                SyncManager                          │    │
│  └───────────────────────────┬─────────────────────────┘    │
└───────────────────────────────┼───────────────────────────────┘
                                │
┌───────────────────────────────┼───────────────────────────────┐
│                               ▼                               │
│  ┌─────────────────────────────────────────────────────┐     │
│  │               CloudSyncService                      │     │
│  └───────────┬─────────────────────────┬───────────────┘     │
│              │                         │                     │
│              ▼                         ▼                     │
│  ┌─────────────────────┐  ┌─────────────────────────────┐   │
│  │   Firebase云服务     │  │      华为云服务              │   │
│  │ (Firestore/Auth)    │  │  (AGConnect/CloudDB)        │   │
│  └─────────────────────┘  └─────────────────────────────┘   │
│                          云端服务层                           │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 技术栈选择
- **云服务平台**: Firebase Firestore + 华为云数据库
- **身份认证**: Firebase Auth + 华为AGConnect Auth
- **加密算法**: AES-256-GCM端到端加密
- **数据格式**: JSON + Protocol Buffers
- **网络框架**: Retrofit + OkHttp
- **异步处理**: Kotlin Coroutines + Flow
- **依赖注入**: Hilt框架
- **本地存储**: Room数据库 2.7.1

## 3. 核心组件设计

### 3.1 同步管理器
```kotlin
// SyncManager.kt
@Singleton
class SyncManager @Inject constructor(
    private val cloudSyncService: CloudSyncService,
    private val localRepository: MenstrualRecordRepository,
    private val encryptionService: EncryptionService,
    private val conflictResolver: ConflictResolver,
    private val networkManager: NetworkManager
) {
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val SYNC_INTERVAL = 30_000L // 30秒
        private const val RETRY_DELAY = 5_000L // 5秒
        private const val MAX_RETRY_COUNT = 3
    }
    
    fun startAutoSync() {
        syncScope.launch {
            while (true) {
                delay(SYNC_INTERVAL)
                if (networkManager.isNetworkAvailable()) {
                    performSync()
                }
            }
        }
    }
    
    suspend fun performSync(): SyncResult {
        return try {
            _syncState.value = SyncState.Syncing
            
            // 1. 获取本地变更
            val localChanges = localRepository.getPendingChanges()
            
            // 2. 上传本地变更
            val uploadResult = uploadLocalChanges(localChanges)
            
            // 3. 下载云端变更
            val downloadResult = downloadCloudChanges()
            
            // 4. 解决冲突
            val resolvedData = conflictResolver.resolveConflicts(
                localData = localChanges,
                cloudData = downloadResult.data
            )
            
            // 5. 应用解决后的数据
            applyResolvedData(resolvedData)
            
            _syncState.value = SyncState.Success
            SyncResult.Success(
                uploaded = uploadResult.count,
                downloaded = downloadResult.count
            )
            
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "同步失败")
            SyncResult.Error(e)
        }
    }
    
    private suspend fun uploadLocalChanges(changes: List<SyncableRecord>): UploadResult {
        val encryptedChanges = changes.map { record ->
            val encryptedData = encryptionService.encrypt(record.toJson())
            CloudRecord(
                id = record.id,
                data = encryptedData,
                timestamp = record.lastModified,
                version = record.version,
                deviceId = getDeviceId()
            )
        }
        
        return cloudSyncService.uploadRecords(encryptedChanges)
    }
    
    private suspend fun downloadCloudChanges(): DownloadResult {
        val lastSyncTimestamp = getLastSyncTimestamp()
        val cloudRecords = cloudSyncService.getRecordsSince(lastSyncTimestamp)
        
        val decryptedRecords = cloudRecords.map { cloudRecord ->
            val decryptedData = encryptionService.decrypt(cloudRecord.data)
            SyncableRecord.fromJson(decryptedData)
        }
        
        return DownloadResult(
            data = decryptedRecords,
            count = decryptedRecords.size
        )
    }
    
    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        object Success : SyncState()
        data class Error(val message: String) : SyncState()
    }
}
```

### 3.2 云端同步服务
```kotlin
// CloudSyncService.kt
@Singleton
class CloudSyncService @Inject constructor(
    private val firebaseService: FirebaseSyncService,
    private val huaweiService: HuaweiSyncService,
    private val authManager: AuthManager
) {
    
    private val primaryService: CloudProvider
        get() = if (isGoogleServicesAvailable()) firebaseService else huaweiService
    
    private val backupService: CloudProvider
        get() = if (isGoogleServicesAvailable()) huaweiService else firebaseService
    
    suspend fun uploadRecords(records: List<CloudRecord>): UploadResult {
        return try {
            // 主要云服务上传
            val primaryResult = primaryService.uploadRecords(records)
            
            // 备份云服务上传（异步）
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    backupService.uploadRecords(records)
                } catch (e: Exception) {
                    Log.w("CloudSync", "Backup upload failed", e)
                }
            }
            
            primaryResult
        } catch (e: Exception) {
            // 主要服务失败，尝试备份服务
            Log.w("CloudSync", "Primary service failed, trying backup", e)
            backupService.uploadRecords(records)
        }
    }
    
    suspend fun getRecordsSince(timestamp: Long): List<CloudRecord> {
        return try {
            primaryService.getRecordsSince(timestamp)
        } catch (e: Exception) {
            Log.w("CloudSync", "Primary service failed, trying backup", e)
            backupService.getRecordsSince(timestamp)
        }
    }
    
    suspend fun deleteRecord(recordId: String): Boolean {
        val primaryResult = runCatching { primaryService.deleteRecord(recordId) }
        val backupResult = runCatching { backupService.deleteRecord(recordId) }
        
        return primaryResult.isSuccess || backupResult.isSuccess
    }
    
    private fun isGoogleServicesAvailable(): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }
}

interface CloudProvider {
    suspend fun uploadRecords(records: List<CloudRecord>): UploadResult
    suspend fun getRecordsSince(timestamp: Long): List<CloudRecord>
    suspend fun deleteRecord(recordId: String): Boolean
    suspend fun getUserProfile(): UserProfile?
    suspend fun updateUserProfile(profile: UserProfile): Boolean
}
```

### 3.3 Firebase同步服务
```kotlin
// FirebaseSyncService.kt
@Singleton
class FirebaseSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CloudProvider {
    
    private val userCollection: String
        get() = "users/${auth.currentUser?.uid}/records"
    
    override suspend fun uploadRecords(records: List<CloudRecord>): UploadResult {
        return withContext(Dispatchers.IO) {
            val batch = firestore.batch()
            var successCount = 0
            
            records.forEach { record ->
                try {
                    val docRef = firestore.collection(userCollection).document(record.id)
                    batch.set(docRef, record.toFirestoreMap())
                    successCount++
                } catch (e: Exception) {
                    Log.e("FirebaseSync", "Failed to prepare record ${record.id}", e)
                }
            }
            
            try {
                batch.commit().await()
                UploadResult(count = successCount, errors = records.size - successCount)
            } catch (e: Exception) {
                throw SyncException("Firebase batch upload failed", e)
            }
        }
    }
    
    override suspend fun getRecordsSince(timestamp: Long): List<CloudRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val query = firestore.collection(userCollection)
                    .whereGreaterThan("timestamp", timestamp)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                
                val snapshot = query.get().await()
                snapshot.documents.mapNotNull { doc ->
                    try {
                        CloudRecord.fromFirestoreDocument(doc)
                    } catch (e: Exception) {
                        Log.e("FirebaseSync", "Failed to parse document ${doc.id}", e)
                        null
                    }
                }
            } catch (e: Exception) {
                throw SyncException("Firebase download failed", e)
            }
        }
    }
    
    override suspend fun deleteRecord(recordId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                firestore.collection(userCollection)
                    .document(recordId)
                    .delete()
                    .await()
                true
            } catch (e: Exception) {
                Log.e("FirebaseSync", "Failed to delete record $recordId", e)
                false
            }
        }
    }
    
    override suspend fun getUserProfile(): UserProfile? {
        return withContext(Dispatchers.IO) {
            try {
                val doc = firestore.collection("users")
                    .document(auth.currentUser?.uid ?: return@withContext null)
                    .get()
                    .await()
                
                doc.toObject<UserProfile>()
            } catch (e: Exception) {
                Log.e("FirebaseSync", "Failed to get user profile", e)
                null
            }
        }
    }
}
```

### 3.4 华为云同步服务
```kotlin
// HuaweiSyncService.kt
@Singleton
class HuaweiSyncService @Inject constructor(
    private val cloudDB: AGConnectCloudDB,
    private val auth: AGConnectAuth
) : CloudProvider {
    
    private lateinit var cloudDBZone: CloudDBZone
    
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                val config = CloudDBZoneConfig(
                    "LuminCoreZone",
                    CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                    CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
                )
                
                cloudDBZone = cloudDB.openCloudDBZone2(config, true).await()
            } catch (e: Exception) {
                throw SyncException("Failed to initialize Huawei CloudDB", e)
            }
        }
    }
    
    override suspend fun uploadRecords(records: List<CloudRecord>): UploadResult {
        return withContext(Dispatchers.IO) {
            try {
                val huaweiRecords = records.map { it.toHuaweiCloudRecord() }
                val result = cloudDBZone.executeUpsert(huaweiRecords).await()
                
                UploadResult(
                    count = result.size,
                    errors = records.size - result.size
                )
            } catch (e: Exception) {
                throw SyncException("Huawei upload failed", e)
            }
        }
    }
    
    override suspend fun getRecordsSince(timestamp: Long): List<CloudRecord> {
        return withContext(Dispatchers.IO) {
            try {
                val query = CloudDBZoneQuery.where(HuaweiCloudRecord::class.java)
                    .greaterThan("timestamp", timestamp)
                    .orderByAsc("timestamp")
                
                val snapshot = cloudDBZone.executeQuery(
                    query,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
                ).await()
                
                snapshot.snapshotObjects.map { it.toCloudRecord() }
            } catch (e: Exception) {
                throw SyncException("Huawei download failed", e)
            }
        }
    }
    
    override suspend fun deleteRecord(recordId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val recordToDelete = HuaweiCloudRecord().apply { id = recordId }
                cloudDBZone.executeDelete(recordToDelete).await()
                true
            } catch (e: Exception) {
                Log.e("HuaweiSync", "Failed to delete record $recordId", e)
                false
            }
        }
    }
}
```

### 3.5 冲突解决器
```kotlin
// ConflictResolver.kt
@Singleton
class ConflictResolver @Inject constructor(
    private val userPreferences: UserPreferences
) {
    
    suspend fun resolveConflicts(
        localData: List<SyncableRecord>,
        cloudData: List<SyncableRecord>
    ): List<SyncableRecord> {
        val conflictStrategy = userPreferences.getConflictResolutionStrategy()
        val localMap = localData.associateBy { it.id }
        val cloudMap = cloudData.associateBy { it.id }
        
        val resolved = mutableListOf<SyncableRecord>()
        val allIds = (localMap.keys + cloudMap.keys).distinct()
        
        allIds.forEach { id ->
            val localRecord = localMap[id]
            val cloudRecord = cloudMap[id]
            
            val resolvedRecord = when {
                localRecord == null -> cloudRecord!! // 仅云端有
                cloudRecord == null -> localRecord // 仅本地有
                else -> resolveRecordConflict(localRecord, cloudRecord, conflictStrategy)
            }
            
            resolved.add(resolvedRecord)
        }
        
        return resolved
    }
    
    private fun resolveRecordConflict(
        local: SyncableRecord,
        cloud: SyncableRecord,
        strategy: ConflictStrategy
    ): SyncableRecord {
        return when (strategy) {
            ConflictStrategy.LOCAL_WINS -> local
            ConflictStrategy.CLOUD_WINS -> cloud
            ConflictStrategy.TIMESTAMP_WINS -> {
                if (local.lastModified > cloud.lastModified) local else cloud
            }
            ConflictStrategy.MERGE -> mergeRecords(local, cloud)
            ConflictStrategy.MANUAL -> {
                // 标记为需要手动解决的冲突
                local.copy(hasConflict = true, conflictData = cloud)
            }
        }
    }
    
    private fun mergeRecords(local: SyncableRecord, cloud: SyncableRecord): SyncableRecord {
        // 智能合并逻辑
        return when (local) {
            is MenstrualRecord -> mergeMenstrualRecords(local, cloud as MenstrualRecord)
            is UserProfile -> mergeUserProfiles(local, cloud as UserProfile)
            else -> if (local.lastModified > cloud.lastModified) local else cloud
        }
    }
    
    private fun mergeMenstrualRecords(local: MenstrualRecord, cloud: MenstrualRecord): SyncableRecord {
        return local.copy(
            // 保留最新的基本信息
            startDate = if (local.lastModified > cloud.lastModified) local.startDate else cloud.startDate,
            endDate = local.endDate ?: cloud.endDate, // 优先保留有值的结束日期
            flowLevel = if (local.lastModified > cloud.lastModified) local.flowLevel else cloud.flowLevel,
            // 合并症状列表
            symptoms = (local.symptoms.orEmpty() + cloud.symptoms.orEmpty()).distinct(),
            // 合并备注
            notes = mergeNotes(local.notes, cloud.notes),
            lastModified = maxOf(local.lastModified, cloud.lastModified)
        )
    }
    
    private fun mergeNotes(localNotes: String?, cloudNotes: String?): String? {
        return when {
            localNotes.isNullOrBlank() -> cloudNotes
            cloudNotes.isNullOrBlank() -> localNotes
            localNotes == cloudNotes -> localNotes
            else -> "$localNotes\n---\n$cloudNotes" // 合并备注内容
        }
    }
    
    enum class ConflictStrategy {
        LOCAL_WINS, CLOUD_WINS, TIMESTAMP_WINS, MERGE, MANUAL
    }
}
```

### 3.6 网络管理器
```kotlin
// NetworkManager.kt
@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _networkState = MutableStateFlow(getNetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()
    
    init {
        registerNetworkCallback()
    }
    
    fun isNetworkAvailable(): Boolean {
        return networkState.value != NetworkState.UNAVAILABLE
    }
    
    fun isWifiConnected(): Boolean {
        return networkState.value == NetworkState.WIFI
    }
    
    fun isMobileConnected(): Boolean {
        return networkState.value == NetworkState.MOBILE
    }
    
    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkState.value = getNetworkState()
            }
            
            override fun onLost(network: Network) {
                _networkState.value = getNetworkState()
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                _networkState.value = getNetworkState()
            }
        })
    }
    
    private fun getNetworkState(): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkState.UNAVAILABLE
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkState.UNAVAILABLE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkState.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkState.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkState.ETHERNET
            else -> NetworkState.UNAVAILABLE
        }
    }
    
    enum class NetworkState {
        WIFI, MOBILE, ETHERNET, UNAVAILABLE
    }
}
```

## 4. 数据模型设计

### 4.1 同步数据模型
```kotlin
// SyncableRecord.kt
@JsonClass(generateAdapter = true)
abstract class SyncableRecord {
    abstract val id: String
    abstract val lastModified: Long
    abstract val version: Int
    abstract val isDeleted: Boolean
    
    var hasConflict: Boolean = false
    var conflictData: SyncableRecord? = null
    
    abstract fun toJson(): String
    
    companion object {
        fun fromJson(json: String): SyncableRecord {
            // 根据JSON内容判断类型并反序列化
            val jsonObject = JSONObject(json)
            val type = jsonObject.getString("type")
            
            return when (type) {
                "menstrual_record" -> Gson().fromJson(json, MenstrualRecord::class.java)
                "user_profile" -> Gson().fromJson(json, UserProfile::class.java)
                "user_settings" -> Gson().fromJson(json, UserSettings::class.java)
                else -> throw IllegalArgumentException("Unknown record type: $type")
            }
        }
    }
}

// CloudRecord.kt
@JsonClass(generateAdapter = true)
data class CloudRecord(
    val id: String,
    val data: EncryptedData,
    val timestamp: Long,
    val version: Int,
    val deviceId: String,
    val checksum: String = calculateChecksum(data)
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "data" to data.toMap(),
            "timestamp" to timestamp,
            "version" to version,
            "deviceId" to deviceId,
            "checksum" to checksum
        )
    }
    
    companion object {
        fun fromFirestoreDocument(doc: DocumentSnapshot): CloudRecord {
            return CloudRecord(
                id = doc.getString("id")!!,
                data = EncryptedData.fromMap(doc.get("data") as Map<String, Any>),
                timestamp = doc.getLong("timestamp")!!,
                version = doc.getLong("version")!!.toInt(),
                deviceId = doc.getString("deviceId")!!,
                checksum = doc.getString("checksum")!!
            )
        }
        
        private fun calculateChecksum(data: EncryptedData): String {
            val content = data.data + data.iv
            val digest = MessageDigest.getInstance("SHA-256")
            return Base64.encodeToString(digest.digest(content), Base64.NO_WRAP)
        }
    }
}
```

## 5. 实施计划

### 第一阶段：基础架构搭建（2周）
- **Week 1**: 同步管理器和网络管理器实现
- **Week 2**: 冲突解决器和数据模型设计

### 第二阶段：云服务集成（2.5周）
- **Week 3**: Firebase同步服务实现
- **Week 4**: 华为云同步服务实现
- **Week 5**: 云端同步服务整合和测试

### 第三阶段：安全和优化（1.5周）
- **Week 6**: 端到端加密实现和安全测试
- **Week 7**: 性能优化和错误处理完善

## 6. 安全策略

### 6.1 端到端加密
- 客户端数据加密后上传云端
- 云端仅存储加密数据，无法解密
- 密钥仅存储在客户端设备上
- 支持密钥备份和恢复机制

### 6.2 身份验证
- Firebase Auth + 华为AGConnect Auth双重保障
- 支持多种登录方式（邮箱、手机、第三方）
- JWT Token有效期管理
- 设备绑定和授权管理

### 6.3 数据完整性
- 数据传输完整性校验
- 云端数据版本控制
- 定期数据一致性检查
- 数据损坏检测和修复

## 7. 性能优化

### 7.1 同步优化
- 增量同步减少数据传输
- 智能同步时机选择
- 批量操作提高效率
- 压缩算法减少流量

### 7.2 缓存策略
- 本地数据缓存机制
- 智能预加载策略
- 过期数据清理
- 内存使用优化

## 8. 监控和告警

### 8.1 同步监控
- 同步成功率统计
- 网络状态监控
- 错误日志收集
- 性能指标跟踪

### 8.2 用户体验
- 同步进度展示
- 离线状态提示
- 冲突解决引导
- 错误恢复建议

## 9. 风险评估

### 9.1 技术风险
- **网络不稳定**: 实现重试机制和离线队列
- **云服务故障**: 多云备份和自动切换
- **数据冲突**: 智能冲突解决和手动处理

### 9.2 安全风险
- **数据泄露**: 端到端加密和访问控制
- **身份伪造**: 多重身份验证
- **中间人攻击**: HTTPS和证书验证

通过以上详细的云端同步架构设计，LuminCore将为用户提供安全、可靠、高效的多设备数据同步体验，确保女性健康数据在任何设备上都能获得一致的访问体验。

---

**文档版本**: 1.0.0
**创建日期**: 2025年8月25日
**计划负责人**: 祁潇潇
**审核状态**: 已审核
**预计开始时间**: 2028年4月1日
**预计完成时间**: 2028年6月30日
**相关依赖**: 数据备份同步计划,数据加密计划,用户认证系统,网络状态管理器,冲突解决器,应用锁功能计划