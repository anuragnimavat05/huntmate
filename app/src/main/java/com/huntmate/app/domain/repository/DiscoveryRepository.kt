package com.huntmate.app.domain.repository

import com.huntmate.app.data.model.DiscoveryFilters
import com.huntmate.app.data.model.MatchResult
import kotlinx.coroutines.flow.Flow

interface DiscoveryRepository {
    fun observeMatches(currentUserId: String, filters: DiscoveryFilters): Flow<List<MatchResult>>
}
