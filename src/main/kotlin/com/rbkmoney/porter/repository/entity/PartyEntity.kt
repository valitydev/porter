package com.rbkmoney.porter.repository.entity

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "party")
@TypeDef(
    name = "pgsql_enum",
    typeClass = PostgreSQLEnumType::class
)
class PartyEntity : BaseEntity<Long>() {
    @Column(nullable = false, unique = true)
    var partyId: String? = null

    @Column
    var email: String? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(nullable = false)
    var status: PartyStatus? = null
}
