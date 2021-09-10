package com.rbkmoney.porter.converter

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.notification.NotificationTemplate
import com.rbkmoney.notification.NotificationTemplateState
import com.rbkmoney.notification.PartyNotification
import com.rbkmoney.porter.config.ConverterConfig
import com.rbkmoney.porter.converter.model.NotificationTemplateEntityEnriched
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.convert.ConversionService
import java.time.LocalDateTime

@SpringBootTest(classes = [ConverterConfig::class])
class NotificationConverterTest(
    @Autowired val conversionService: ConversionService,
    @Autowired val converters: Set<NotificatorConverter<*, *>>,
) {

    @Test
    fun `convert notification entity to party notification`() {
        // Given
        val notificationEntity = EasyRandom().nextObject(NotificationEntity::class.java).apply {
            status = NotificationStatus.unread
        }

        // When
        val partyNotification = conversionService.convert(notificationEntity, PartyNotification::class.java)

        // Then
        assertEquals(notificationEntity.notificationTemplateEntity?.templateId, partyNotification?.template_id)
        assertTrue(partyNotification?.status == com.rbkmoney.notification.NotificationStatus.unread)
        assertEquals(notificationEntity.partyEntity!!.partyId, partyNotification?.party?.party_id)
    }

    @Test
    fun `notification template to notification template entity`() {
        // Given
        val notificationTemplate = EasyRandom().nextObject(NotificationTemplate::class.java).apply {
            state = NotificationTemplateState.draft_state
            created_at = TypeUtil.temporalToString(LocalDateTime.now())
            updated_at = TypeUtil.temporalToString(LocalDateTime.now())
        }

        // When
        val notificationTemplateEntity =
            conversionService.convert(notificationTemplate, NotificationTemplateEntity::class.java)!!

        // Then
        assertEquals(notificationTemplate.templateId, notificationTemplateEntity.templateId)
        assertEquals(notificationTemplate.title, notificationTemplateEntity.title)
        assertEquals(notificationTemplate.content.text, notificationTemplateEntity.content)
        assertEquals(notificationTemplate.content.content_type, notificationTemplateEntity.contentType)
        assertEquals(
            TypeUtil.stringToLocalDateTime(notificationTemplate.created_at),
            notificationTemplateEntity.createdAt
        )
        assertEquals(
            TypeUtil.stringToLocalDateTime(notificationTemplate.updatedAt),
            notificationTemplateEntity.updatedAt
        )
    }

    @Test
    fun `notification template entity to notification template`() {
        // Given
        val notificationTemplateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java)
        val notificationTemplateEntityEnriched = NotificationTemplateEntityEnriched(notificationTemplateEntity, 10, 50)

        // When
        val notificationTemplate =
            conversionService.convert(notificationTemplateEntityEnriched, NotificationTemplate::class.java)!!

        // Then
        assertEquals(notificationTemplateEntity.templateId, notificationTemplate.templateId)
        assertEquals(notificationTemplateEntity.title, notificationTemplate.title)
        assertEquals(
            notificationTemplateEntity.content,
            notificationTemplate.content.text
        )
        assertEquals(
            notificationTemplateEntity.contentType,
            notificationTemplate.content.content_type
        )
        assertEquals(
            notificationTemplateEntity.createdAt,
            TypeUtil.stringToLocalDateTime(notificationTemplate.createdAt)
        )
        assertEquals(
            notificationTemplateEntity.updatedAt,
            TypeUtil.stringToLocalDateTime(notificationTemplate.updatedAt)
        )
        assertEquals(notificationTemplateEntityEnriched.readCount, notificationTemplate.distribution_details.read_count)
        assertEquals(
            notificationTemplateEntityEnriched.totalCount,
            notificationTemplate.distribution_details.total_count
        )
    }

    @Test
    fun `convert notification template state to notification template status`() {
        // Given
        val notificationTemplateState = NotificationTemplateState.draft_state

        // When
        val notificationTemplateStatus =
            conversionService.convert(notificationTemplateState, NotificationTemplateStatus::class.java)

        // Then
        assertTrue(notificationTemplateStatus == NotificationTemplateStatus.draft)
    }

    @Test
    fun `convert notification template status to notification template state`() {
        // Given
        val notificationTemplateStatus = NotificationTemplateStatus.draft

        // When
        val notificationTemplateState =
            conversionService.convert(notificationTemplateStatus, NotificationTemplateState::class.java)

        // Then
        assertTrue(notificationTemplateState == NotificationTemplateState.draft_state)
    }

    @Test
    fun `convert notification entity status to notification status`() {
        // Given
        val notificationStatus = NotificationStatus.unread

        // When
        val notificationStatusThrift =
            conversionService.convert(notificationStatus, com.rbkmoney.notification.NotificationStatus::class.java)

        // Then
        assertTrue(notificationStatusThrift == com.rbkmoney.notification.NotificationStatus.unread)
    }
}
