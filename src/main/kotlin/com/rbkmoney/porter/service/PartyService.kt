package com.rbkmoney.porter.service

import com.rbkmoney.porter.repository.PartyRepository
import org.springframework.stereotype.Service

@Service
class PartyService(
    private val partyRepository: PartyRepository,
) {

    fun getPartyName(partyId: String): String {
        return partyRepository.findByPartyId(partyId)?.email ?: "unknown"
    }
}
