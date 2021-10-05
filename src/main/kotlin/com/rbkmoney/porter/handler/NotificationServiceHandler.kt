package com.rbkmoney.porter.handler

import com.rbkmoney.notification.NotificationServiceSrv
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateCreateRequest
import com.rbkmoney.notification.NotificationTemplateModifyRequest
import com.rbkmoney.notification.NotificationTemplatePartyRequest
import com.rbkmoney.notification.NotificationTemplatePartyResponse
import com.rbkmoney.notification.NotificationTemplateSearchRequest
import com.rbkmoney.notification.NotificationTemplateSearchResponse
import com.rbkmoney.notification.PartyNotification
import com.rbkmoney.notification.PartyNotificationRequest
import com.rbkmoney.notification.PartyNotificationResponse
import com.rbkmoney.porter.converter.model.NotificationTemplateEntityEnriched
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.service.NotificationSenderService
import com.rbkmoney.porter.service.NotificationService
import com.rbkmoney.porter.service.NotificationTemplateService
import com.rbkmoney.porter.service.PartyService
import com.rbkmoney.porter.service.model.DateFilter
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.model.NotificationTemplateFilter
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import mu.KotlinLogging
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class NotificationServiceHandler(
    private val notificationTemplateService: NotificationTemplateService,
    private val notificationService: NotificationService,
    private val notificationSenderService: NotificationSenderService,
    private val conversionService: ConversionService,
    private val continuationTokenService: ContinuationTokenService,
    private val partyService: PartyService,
) : NotificationServiceSrv.Iface {

    override fun createNotificationTemplate(
        request: NotificationTemplateCreateRequest,
    ): NotificationTemplate {
        log.info { "Create notification template request: $request" }
        val notificationTemplate = notificationTemplateService.createNotificationTemplate(
            title = request.title,
            content = request.content.text,
            contentType = request.content.content_type
        )
        val notificationTemplateEntityEnriched = NotificationTemplateEntityEnriched(notificationTemplate)
        log.info { "Create notification template result: $notificationTemplate" }

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!.also {
            log.info { "Created notification template: $it" }
        }
    }

    override fun modifyNotificationTemplate(
        request: NotificationTemplateModifyRequest,
    ): NotificationTemplate {
        log.info { "Modify notification template request: $request" }
        val notificationTemplate = notificationTemplateService.editNotificationTemplate(
            request.templateId,
            request.title,
            request.content.text,
            request.content.contentType
        )
        val notificationStats = notificationService.findNotificationStats(notificationTemplate.templateId!!)
        val notificationTemplateEntityEnriched =
            NotificationTemplateEntityEnriched(notificationTemplate, notificationStats.read, notificationStats.total)
        log.info { "Modify notification template result: $notificationTemplate" }

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!.also {
            log.info { "Modified notification template: $it" }
        }
    }

    override fun getNotificationTemplate(templateId: String): NotificationTemplate {
        log.info { "Get notification template by templateId=$templateId" }
        val notificationTemplate = notificationTemplateService.getNotificationTemplate(templateId)
        val notificationStats = notificationService.findNotificationStats(notificationTemplate.templateId!!)
        val notificationTemplateEntityEnriched =
            NotificationTemplateEntityEnriched(notificationTemplate, notificationStats.read, notificationStats.total)
        log.info { "Get notification template result: $notificationTemplate" }

        return conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!.also {
            log.info { "Found notification template: $it" }
        }
    }

    override fun removeNotificationTemplate(templateId: String) {
        log.info { "Remove notification template by templateId=$templateId" }
        notificationTemplateService.removeNotificationTemplate(templateId)
    }

    override fun findNotificationTemplateParties(
        request: NotificationTemplatePartyRequest,
    ): NotificationTemplatePartyResponse {
        log.info { "Find notification template request: $request" }
        val continuationToken = request.continuation_token?.let {
            continuationTokenService.tokenFromString(request.continuation_token)
        }
        val notificationFilter = NotificationFilter(
            templateId = request.template_id,
            status = conversionService.convert(request.status, NotificationStatus::class.java)
        )
        val page = notificationService.findNotifications(
            filter = notificationFilter,
            continuationToken = continuationToken,
            limit = request.limit
        )

        return NotificationTemplatePartyResponse().apply {
            continuation_token = if (page.hasNext)
                continuationTokenService.tokenToString(page.token!!) else null
            parties = page.entities.map {
                conversionService.convert(it, PartyNotification::class.java)!!
            }
        }.also {
            log.info { "Found ${it.parties.size} notification parties. continuationToken=${it.continuation_token}" }
        }
    }

    override fun findNotificationTemplates(
        request: NotificationTemplateSearchRequest,
    ): NotificationTemplateSearchResponse {
        log.info { "Find notification templates request: $request" }
        val createdAtDateFilter = if (request.isSetCreatedDateFilter) {
            conversionService.convert(request.createdDateFilter, DateFilter::class.java)
        } else null
        val sentAtDateFilter = if (request.isSetSentDateFilter) {
            conversionService.convert(request.sentDateFilter, DateFilter::class.java)
        } else null
        val notificationTemplateFilter = NotificationTemplateFilter(
            title = request.title,
            content = request.content,
            createdDateFilter = createdAtDateFilter,
            sentDateFilter = sentAtDateFilter
        )
        val token: String? = request.continuation_token
        val continuationToken = token?.let { continuationTokenService.tokenFromString(token) }

        val notificationTemplatesPage = notificationTemplateService.findNotificationTemplate(
            continuationToken = continuationToken,
            filter = notificationTemplateFilter,
            limit = request.limit
        )

        return NotificationTemplateSearchResponse().apply {
            continuation_token = if (notificationTemplatesPage.hasNext)
                continuationTokenService.tokenToString(notificationTemplatesPage.token!!) else null
            notification_templates = notificationTemplatesPage.entities.map {
                val notificationStats = notificationService.findNotificationStats(it.templateId!!)
                val notificationTemplateEntityEnriched =
                    NotificationTemplateEntityEnriched(it, notificationStats.read, notificationStats.total)

                conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!
            }
        }.also {
            log.info { "Found ${it.notification_templates.size} notification template. continuationToken=${it.continuation_token}" }
        }
    }

    override fun findPartyNotifications(request: PartyNotificationRequest): PartyNotificationResponse {
        log.info { "Find party notifications: $request" }
        val token: String? = request.continuation_token
        val continuationToken = token?.let { continuationTokenService.tokenFromString(token) }
        val notificationFilter = conversionService.convert(request, NotificationFilter::class.java)!!
        val notificationsPage =
            notificationService.findNotifications(filter = notificationFilter, continuationToken = continuationToken)

        return PartyNotificationResponse().apply {
            continuation_token = if (notificationsPage.hasNext)
                continuationTokenService.tokenToString(notificationsPage.token!!) else null
            parties = notificationsPage.entities.map {
                conversionService.convert(it, PartyNotification::class.java)
            }
        }
    }

    override fun sendNotification(templateId: String, partyIds: MutableList<String>) {
        val partyIds = partyIds.toSet()
        log.info { "Send notification: templateId=$templateId; partyIds=$partyIds" }
        notificationSenderService.sendNotification(templateId, partyIds)
    }

    override fun sendNotificationAll(templateId: String) {
        log.info { "Send notification all: templateId=$templateId" }
        notificationSenderService.sendNotificationAll(templateId)
    }
}
