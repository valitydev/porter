package com.rbkmoney.porter.service

import com.rbkmoney.notification.BadNotificationTemplateState
import com.rbkmoney.notification.NotificationTemplateNotFound
import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import com.rbkmoney.porter.service.model.NotificationTemplateFilter
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.Page
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class NotificationTemplateService(
    private val conversionService: ConversionService,
    private val notificationTemplateRepository: NotificationTemplateRepository,
    private val notificationRepository: NotificationRepository,
) {

    fun createNotificationTemplate(
        title: String,
        content: String,
        contentType: String? = null,
    ): NotificationTemplateEntity {
        return notificationTemplateRepository.save(
            NotificationTemplateEntity().apply {
                templateId = UUID.randomUUID().toString()
                createdAt = LocalDateTime.now()
                this.title = title
                this.content = content
                this.contentType = contentType
                status = NotificationTemplateStatus.draft
            }
        )
    }

    @Transactional
    fun editNotificationTemplate(
        templateId: String,
        title: String? = null,
        content: String? = null,
        contentType: String? = null,
        status: NotificationTemplateStatus? = null,
    ): NotificationTemplateEntity {
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
        if (notificationTemplateEntity.status == NotificationTemplateStatus.final) {
            throw BadNotificationTemplateState(
                "You can't modify notification template (${notificationTemplateEntity.templateId}) in final state"
            )
        }
        notificationTemplateEntity.title = title ?: notificationTemplateEntity.title
        notificationTemplateEntity.content = content ?: notificationTemplateEntity.content
        notificationTemplateEntity.contentType = contentType ?: notificationTemplateEntity.contentType
        notificationTemplateEntity.status = status ?: notificationTemplateEntity.status
        notificationTemplateEntity.updatedAt = LocalDateTime.now()

        return notificationTemplateRepository.save(notificationTemplateEntity)
    }

    fun getNotificationTemplate(templateId: String): NotificationTemplateEntity {
        return notificationTemplateRepository.findByTemplateId(templateId)
            ?: throw NotificationTemplateNotFound()
    }

    fun findNotificationTemplate(
        continuationToken: ContinuationToken? = null,
        filter: NotificationTemplateFilter? = null,
        limit: Int = 10,
    ): Page<NotificationTemplateEntity> {
        return if (continuationToken != null) {
            notificationTemplateRepository.findNextNotificationTemplates(continuationToken, limit)
        } else {
            notificationTemplateRepository.findNotificationTemplates(
                from = filter?.from,
                to = filter?.to,
                title = filter?.title,
                content = filter?.content,
                fixedDate = filter?.date,
                limit = limit
            )
        }
    }
}
