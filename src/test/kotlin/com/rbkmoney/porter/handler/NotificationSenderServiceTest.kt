package com.rbkmoney.porter.handler

import com.rbkmoney.porter.AbstractIntegrationTest
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.service.IdGenerator
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.util.*

@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate"
    ]
)
@Transactional
class NotificationSenderServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var notificationTemplateRepository: NotificationTemplateRepository

    @Autowired
    lateinit var partyRepository: PartyRepository

    @Autowired
    lateinit var notificationServiceHandler: NotificationServiceHandler

    lateinit var notificationTemplateEntity: NotificationTemplateEntity

    @BeforeEach
    internal fun setUp() {
        val notificationTemplateObject = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            templateId = TEMPLATE_ID
            title = TEMPLATE_TITLE
        }
        notificationTemplateEntity = notificationTemplateRepository.save(notificationTemplateObject)
    }

    @Test
    fun `send notification with duplicate id`() {
        // Given
        val partyId = IdGenerator.randomString()
        val duplicatePartyId = IdGenerator.randomString()
        val partyIds = mutableListOf<String>(duplicatePartyId, partyId, duplicatePartyId)

        // When
        val partyEntity = EasyRandom().nextObject(PartyEntity::class.java).apply { this.partyId = partyId }
        partyRepository.save(partyEntity)
        val duplicatePartyEntity =
            EasyRandom().nextObject(PartyEntity::class.java).apply { this.partyId = duplicatePartyId }
        partyRepository.save(duplicatePartyEntity)
        notificationServiceHandler.sendNotification(TEMPLATE_ID, partyIds)
        val notifications = notificationTemplateRepository.findByTemplateId(TEMPLATE_ID)?.notifications

        // Then
        assertTrue(notifications?.size == 2)
        assertTrue(notifications?.any { it.partyEntity?.partyId == partyId } ?: false)
        assertTrue(notifications?.any { it.partyEntity?.partyId == duplicatePartyId } ?: false)
    }

    companion object {
        const val TEMPLATE_ID = "12344598750"
        const val TEMPLATE_TITLE = "My first title"
    }
}
