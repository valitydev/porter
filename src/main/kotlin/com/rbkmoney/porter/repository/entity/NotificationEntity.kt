package com.rbkmoney.porter.repository.entity

import com.rbkmoney.porter.service.pagination.PageableEntity
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "notification")
@TypeDef(
    name = "pgsql_enum",
    typeClass = PostgreSQLEnumType::class
)
class NotificationEntity : BaseEntity<Long>(), PageableEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    var notificationTemplateEntity: NotificationTemplateEntity? = null

    @Column(nullable = false)
    var notificationId: String? = null

    @Column(nullable = false)
    var partyId: String? = null

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(nullable = false)
    var status: NotificationStatus = NotificationStatus.unread

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var deleted: Boolean = false

    override val timestamp: Long
        get() = createdAt.toEpochSecond(ZoneOffset.UTC)
}
