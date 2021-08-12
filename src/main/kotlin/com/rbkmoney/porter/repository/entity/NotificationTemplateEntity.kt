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
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "notification_template")
@TypeDef(
    name = "pgsql_enum",
    typeClass = PostgreSQLEnumType::class
)
class NotificationTemplateEntity : BaseEntity<Long>(), PageableEntity<Long> {
    @Column(nullable = false, unique = true)
    var templateId: String? = null

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column
    var updatedAt: LocalDateTime? = null

    @Column(nullable = false)
    var title: String? = null

    @Column(nullable = false)
    var content: String? = null

    @Column
    var contentType: String? = null

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(nullable = false)
    var status: NotificationTemplateStatus = NotificationTemplateStatus.draft

    @OneToMany(mappedBy = "notificationTemplateEntity")
    var notifications: List<NotificationEntity> = emptyList()

    override val timestamp: Long
        get() = createdAt.toEpochSecond(ZoneOffset.UTC)
}
