package com.rbkmoney.porter.handler

import com.rbkmoney.notification.NotificationContent
import com.rbkmoney.notification.NotificationStatus
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateCreateRequest
import com.rbkmoney.notification.NotificationTemplateModifyRequest
import com.rbkmoney.notification.NotificationTemplatePartyRequest
import com.rbkmoney.notification.NotificationTemplateSearchRequest
import com.rbkmoney.notification.PartyNotification
import com.rbkmoney.porter.converter.model.NotificationTemplateEntityEnriched
import com.rbkmoney.porter.repository.TotalNotificationProjection
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.service.NotificationSenderService
import com.rbkmoney.porter.service.NotificationService
import com.rbkmoney.porter.service.NotificationTemplateService
import com.rbkmoney.porter.service.PartyService
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import com.rbkmoney.porter.service.pagination.Page
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.atMostOnce
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.core.convert.ConversionService

@MockBeans(
    *[
        MockBean(NotificationSenderService::class),
        MockBean(ContinuationTokenService::class),
        MockBean(PartyService::class)
    ]
)
@SpringBootTest(classes = [NotificationServiceHandler::class])
class NotificationServiceHandlerTest {

    @MockBean
    lateinit var conversionService: ConversionService

    @MockBean
    lateinit var notificationService: NotificationService

    @MockBean
    lateinit var notificationTemplateService: NotificationTemplateService

    @MockBean
    lateinit var partyService: PartyService

    @Autowired
    lateinit var notificationServiceHandler: NotificationServiceHandler

    @Test
    fun `test correct converter call for create notification template`() {
        // Given
        val title = "testTitle"
        val content = "<p>I really like using Markdown.</p>"
        val createRequest =
            NotificationTemplateCreateRequest(title, NotificationContent(content))

        // When
        `when`(
            notificationTemplateService.createNotificationTemplate(
                anyString(),
                anyString(),
                anyOrNull()
            )
        ).thenReturn(
            NotificationTemplateEntity()
        )
        `when`(
            conversionService.convert(
                any(NotificationTemplateEntityEnriched::class.java),
                eq(NotificationTemplate::class.java)
            )
        ).thenReturn(NotificationTemplate())

        val createNotificationTemplate = notificationServiceHandler.createNotificationTemplate(createRequest)

        // Then
        verify(notificationTemplateService, atMostOnce()).createNotificationTemplate(
            anyString(),
            anyString(),
            anyOrNull()
        )
        verify(conversionService, atMostOnce()).convert(
            any(NotificationTemplateEntityEnriched::class.java),
            eq(NotificationTemplate::class.java)
        )
    }

    @Test
    fun `test correct converter call for modify notification template`() {
        // Given
        val templateId = "testTemplateId"
        val templateTitle = "testTemplateTitle"
        val templateContent = "<p>I really like using Markdown.</p>"

        // When
        whenever(
            notificationTemplateService.editNotificationTemplate(
                eq(templateId),
                eq(templateTitle),
                eq(templateContent),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(NotificationTemplateEntity().apply { this.templateId = templateId })
        whenever(notificationService.findNotificationStats(eq(templateId))).thenReturn(
            object : TotalNotificationProjection {
                override val total: Long
                    get() = 10
                override val read: Long
                    get() = 10
            }
        )
        whenever(
            conversionService.convert(
                any(NotificationTemplateEntityEnriched::class.java),
                eq(NotificationTemplate::class.java)
            )
        ).thenReturn(NotificationTemplate())
        notificationServiceHandler.modifyNotificationTemplate(
            NotificationTemplateModifyRequest(templateId).apply {
                title = templateTitle
                content = NotificationContent(templateContent)
            }
        )

        // Then
        verify(notificationTemplateService, atMostOnce()).editNotificationTemplate(
            eq(templateId),
            eq(templateTitle),
            eq(templateContent),
            anyOrNull(),
            anyOrNull()
        )
        verify(conversionService, atMostOnce()).convert(
            any(NotificationTemplateEntityEnriched::class.java),
            eq(NotificationTemplate::class.java)
        )
    }

    @Test
    fun `test correct converter call for notification template`() {
        // Given
        val templateId = "testTemplateId"

        // When
        whenever(notificationTemplateService.getNotificationTemplate(eq(templateId)))
            .thenReturn(NotificationTemplateEntity().apply { this.templateId = templateId })
        whenever(notificationService.findNotificationStats(eq(templateId))).thenReturn(
            object : TotalNotificationProjection {
                override val total: Long
                    get() = 10
                override val read: Long
                    get() = 10
            }
        )
        whenever(
            conversionService.convert(
                any(NotificationTemplateEntityEnriched::class.java),
                eq(NotificationTemplate::class.java)
            )
        ).thenReturn(NotificationTemplate())
        notificationServiceHandler.getNotificationTemplate(templateId)

        // Then
        verify(notificationTemplateService, atMostOnce()).getNotificationTemplate(eq(templateId))
        verify(conversionService, atMostOnce()).convert(
            any(NotificationTemplateEntityEnriched::class.java),
            eq(NotificationTemplate::class.java)
        )
    }

    @Test
    fun `test correct converter call for find notification template parties`() {
        // Given
        val templateId = "testTemplateId"
        val partyRequest = NotificationTemplatePartyRequest(templateId)

        // When
        whenever(
            notificationService.findNotifications(
                org.mockito.kotlin.any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(
            Page(
                entities = listOf(
                    NotificationEntity().apply {
                        this.partyEntity = PartyEntity().apply {
                            partyId = "testPartyId"
                        }
                    }
                ),
                token = null,
                hasNext = false
            )
        )
        whenever(
            conversionService.convert(
                any(NotificationStatus::class.java),
                eq(com.rbkmoney.porter.repository.entity.NotificationStatus::class.java)
            )
        ).thenReturn(com.rbkmoney.porter.repository.entity.NotificationStatus.read)
        whenever(
            conversionService.convert(any(NotificationEntity::class.java), eq(PartyNotification::class.java))
        ).thenReturn(PartyNotification().apply { this.templateId = template_id })
        whenever(partyService.getPartyName(anyString())).thenReturn("testPartyName")
        notificationServiceHandler.findNotificationTemplateParties(partyRequest)

        // Then
        verify(conversionService, atMostOnce()).convert(
            any(NotificationStatus::class.java),
            eq(com.rbkmoney.porter.repository.entity.NotificationStatus::class.java)
        )
        verify(conversionService, atMostOnce()).convert(
            any(NotificationEntity::class.java),
            eq(PartyNotification::class.java)
        )
    }

    @Test
    fun `test correct converter call for find notification template`() {
        // Given
        val templateId = "testTemplateId"
        val titleSearch = "testTitle"
        val templateSearchRequest = NotificationTemplateSearchRequest().apply { title = titleSearch }

        // When
        whenever(notificationTemplateService.findNotificationTemplate(anyOrNull(), anyOrNull(), anyOrNull())).then {
            Page(listOf(NotificationTemplateEntity().apply { this.templateId = templateId }), null, false)
        }
        whenever(notificationService.findNotificationStats(eq(templateId))).thenReturn(
            object : TotalNotificationProjection {
                override val total: Long
                    get() = 10
                override val read: Long
                    get() = 10
            }
        )
        whenever(
            conversionService.convert(
                any(NotificationTemplateEntityEnriched::class.java),
                eq(NotificationTemplate::class.java)
            )
        ).thenReturn(NotificationTemplate())
        notificationServiceHandler.findNotificationTemplates(NotificationTemplateSearchRequest())

        // Then
        verify(conversionService, atMostOnce()).convert(
            any(NotificationTemplateEntityEnriched::class.java),
            eq(NotificationTemplate::class.java)
        )
    }
}
