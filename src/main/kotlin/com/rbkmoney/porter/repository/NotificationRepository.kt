package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository :
    CrudRepository<NotificationEntity, Long>,
    NotificationRepositoryCustom {

    fun findByNotificationTemplateEntity(notificationTemplateEntity: NotificationTemplateEntity): List<NotificationEntity>

    fun findByNotificationTemplateEntityTemplateId(templateId: String): List<NotificationEntity>

    @Query(
        value = """SELECT total_count.total, read_count.read FROM
                    (SELECT count(*) AS total FROM notify.notification WHERE template_id=:templateId) AS total_count,
                    (SELECT count(*) AS read FROM notify.notification
                        WHERE template_id=:templateId
                            AND status=CAST('read' AS notify.notification_status)) AS read_count
                """,
        nativeQuery = true
    )
    fun findNotificationCount(@Param("templateId") templateId: Long): TotalNotificationProjection

    fun findByNotificationId(notificationId: String): NotificationEntity?

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update NotificationEntity n set n.status = :status
            where n.partyEntity.id in (select p.id from PartyEntity p where p.partyId = :partyId)
                and n.notificationId in (:notificationIds)
        """
    )
    fun markNotifications(
        @Param("partyId") partyId: String,
        @Param("notificationIds") notificationIds: List<String>,
        @Param("status") notificationStatus: NotificationStatus,
    )

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update NotificationEntity n set n.status = :status
            where n.partyEntity.id in (select p.id from PartyEntity p where p.partyId = :partyId)
        """
    )
    fun markAllNotifications(@Param("partyId") partyId: String, @Param("status") notificationStatus: NotificationStatus)

    fun findByNotificationIdIn(notificationIds: List<String>): List<NotificationEntity>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update NotificationEntity n set n.deleted = true
            where n.partyEntity.id in (select p.id from PartyEntity p where p.partyId = :partyId)
                and n.notificationId = :notificationId
        """
    )
    fun softDeleteByPartyIdAndNotificationId(partyId: String, notificationId: String)

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        update NotificationEntity n set n.deleted = true
            where n.partyEntity.id in (select p.id from PartyEntity p where p.partyId = :partyId)
                and n.notificationId in (:notificationIds)
        """
    )
    fun softDeleteAllByPartyIdAndNotificationIdIn(partyId: String, notificationIds: List<String>)

    fun deleteByNotificationTemplateEntity_TemplateId(templateId: String)
}
