package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.repository.entity.PartyStatus
import org.hibernate.annotations.QueryHints.READ_ONLY
import org.hibernate.jpa.QueryHints.HINT_CACHEABLE
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import javax.persistence.QueryHint

@Repository
interface PartyRepository : CrudRepository<PartyEntity, Long> {

    @QueryHints(
        value = [
            QueryHint(name = HINT_CACHEABLE, value = "false"),
            QueryHint(name = READ_ONLY, value = "true")
        ]
    )
    fun findAllByStatus(status: PartyStatus): Stream<PartyEntity>

    fun findByPartyId(partyId: String): PartyEntity?

    fun findByPartyIdIn(partyIds: Collection<String>): List<PartyEntity>
}
