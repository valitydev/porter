package com.rbkmoney.porter

import com.rbkmoney.notification.BadNotificationTemplateState
import com.rbkmoney.notification.NotificationTemplateNotFound
import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.NotificationTemplateStatus
import com.rbkmoney.porter.service.NotificationTemplateService
import com.rbkmoney.porter.service.model.NotificationTemplateFilter
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Collectors
import java.util.stream.Stream

@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate"
    ]
)
class NotificationTemplateServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @Autowired
    lateinit var notificationTemplateRepository: NotificationTemplateRepository

    @Autowired
    lateinit var notificationTemplateService: NotificationTemplateService

    @BeforeEach
    fun setUp() {
        notificationRepository.deleteAll()
        notificationTemplateRepository.deleteAll()
    }

    @Test
    fun `create notification template test`() {
        // Given
        val title = "test title"
        val content = "<p>I really like using Markdown.</p>"

        // When
        val notificationTemplate = notificationTemplateService.createNotificationTemplate(title, content)

        // Then
        assertNotNull(notificationTemplate.templateId)
        assertEquals(title, notificationTemplate.title)
        assertEquals(content, notificationTemplate.content)
        assertEquals(NotificationTemplateStatus.draft, notificationTemplate.status)
    }

    @Test
    fun `modify notification template test`() {
        // Given
        val templateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            title = "test title"
            content = "<p>I really like using Markdown.</p>"
            templateId = UUID.randomUUID().toString()
        }

        // When
        val savedTemplateEntity = notificationTemplateRepository.save(templateEntity)
        val title = "test title"
        val content = "**bold text**"
        val editedNotificationTemplate = notificationTemplateService.editNotificationTemplate(
            savedTemplateEntity.templateId!!,
            title,
            content
        )

        // Then
        assertEquals(savedTemplateEntity.title, editedNotificationTemplate.title)
        assertNotEquals(savedTemplateEntity.content, editedNotificationTemplate.content)
        assertEquals(content, editedNotificationTemplate.content)
    }

    @Test
    fun `modify unknown notification template`() {
        assertThrows(NotificationTemplateNotFound::class.java) {
            notificationTemplateService.editNotificationTemplate(
                UUID.randomUUID().toString(),
                "testTitle",
                "testContent"
            )
        }
    }

    @Test
    fun `edit notification template status`() {
        // Given
        val notificationTemplateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            status = NotificationTemplateStatus.draft
        }

        // When
        notificationTemplateRepository.save(notificationTemplateEntity)
        val editedNotificationTemplate = notificationTemplateService.editNotificationTemplate(
            templateId = notificationTemplateEntity.templateId!!,
            status = NotificationTemplateStatus.final
        )

        // Then
        assertTrue(editedNotificationTemplate.status == NotificationTemplateStatus.final)
    }

    @Test
    fun `modify notification template with final status`() {
        // Given
        val templateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            content = "<p>I really like using Markdown.</p>"
            contentType = "text/markdown; charset=UTF-8"
            templateId = UUID.randomUUID().toString()
            status = NotificationTemplateStatus.final
        }

        // When
        val savedTemplateEntity = notificationTemplateRepository.save(templateEntity)

        // Then
        assertThrows(BadNotificationTemplateState::class.java) {
            notificationTemplateService.editNotificationTemplate(
                savedTemplateEntity.templateId!!,
                "testTitle",
                "testContent"
            )
        }
    }

    @Test
    fun `get notification template test`() {
        // Given
        val templateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            content = "<p>I really like using Markdown.</p>"
            status = NotificationTemplateStatus.draft
        }

        // When
        notificationTemplateRepository.save(templateEntity)
        val notificationTemplate = notificationTemplateService.getNotificationTemplate(templateEntity.templateId!!)

        // Then
        assertEquals(templateEntity.templateId, notificationTemplate.templateId)
        assertEquals(
            templateEntity.createdAt.withNano(0),
            notificationTemplate.createdAt.withNano(0)
        )
        assertEquals(
            templateEntity.updatedAt?.withNano(0),
            notificationTemplate.updatedAt?.withNano(0)
        )
        assertEquals(templateEntity.title, notificationTemplate.title)
        assertEquals(templateEntity.content, notificationTemplate.content)
        assertTrue(notificationTemplate.status == NotificationTemplateStatus.draft)
    }

    @Test
    fun `get unknown notification template`() {
        assertThrows(NotificationTemplateNotFound::class.java) {
            notificationTemplateService.getNotificationTemplate(UUID.randomUUID().toString())
        }
    }

    @Test
    fun `test notification templates pagination`() {
        // Given
        val notificationTemplates = EasyRandom().objects(NotificationTemplateEntity::class.java, 20).peek {
            it.id = null
            it.templateId = UUID.randomUUID().toString()
            it.status = NotificationTemplateStatus.draft
        }.collect(Collectors.toList())

        // When
        notificationTemplateRepository.saveAll(notificationTemplates)
        val firstPage = notificationTemplateService.findNotificationTemplate(limit = 10)
        val secondPage = notificationTemplateService.findNotificationTemplate(firstPage.token, limit = 10)

        // Then
        assertTrue(firstPage.entities.size == 10)
        assertTrue(firstPage.hasNext)
        assertTrue(secondPage.entities.size == 10)
        assertFalse(secondPage.hasNext)
    }

    @Test
    fun `test notification templates pagination with params`() {
        // Given
        val firstTemplatesStream = EasyRandom().objects(NotificationTemplateEntity::class.java, 20).peek {
            it.id = null
            it.title = "test"
            it.templateId = UUID.randomUUID().toString()
        }
        val secondTemplatesStream = EasyRandom().objects(NotificationTemplateEntity::class.java, 10).peek {
            it.id = null
            it.templateId = UUID.randomUUID().toString()
        }
        val notificationTemplates =
            Stream.concat(firstTemplatesStream, secondTemplatesStream).collect(Collectors.toList())

        // When
        notificationTemplateRepository.saveAll(notificationTemplates)
        val firstPage = notificationTemplateService.findNotificationTemplate(
            limit = 11,
            filter = NotificationTemplateFilter(title = "test")
        )
        val secondPage = notificationTemplateService.findNotificationTemplate(
            continuationToken = firstPage.token,
            filter = NotificationTemplateFilter(title = "test"),
            limit = 11
        )

        // Then
        assertFalse(secondPage.hasNext)
        assertTrue(secondPage.entities.size == 9)
    }

    @Test
    fun `test notification templates pagination with params by title`() {
        // Given
        val templateId = UUID.randomUUID().toString()
        val searchedNotificationEntity =
            Stream.of(
                EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
                    id = null
                    title = "test"
                    this.templateId = templateId
                }
            )
        val draftNotificationTemplates = EasyRandom().objects(NotificationTemplateEntity::class.java, 10).peek {
            it.id = null
            it.status = NotificationTemplateStatus.draft
        }
        val notificationTemplates =
            Stream.concat(searchedNotificationEntity, draftNotificationTemplates).collect(Collectors.toList())

        // When
        notificationTemplateRepository.saveAll(notificationTemplates)
        val page =
            notificationTemplateService.findNotificationTemplate(filter = NotificationTemplateFilter(title = "test"))

        // Then
        assertTrue(page.entities.size == 1)
        assertEquals(templateId, page.entities.first().templateId)
    }

    @Test
    fun `test notification templates pagination with params by content`() {
        // Given
        val searchedNotificationEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            content = "<p>I really like using Markdown.</p>"
            templateId = UUID.randomUUID().toString()
        }
        val notificationTemplates = EasyRandom().objects(NotificationTemplateEntity::class.java, 10).peek {
            it.id = null
            it.status = NotificationTemplateStatus.draft
        }.collect(Collectors.toList()).also {
            it.add(searchedNotificationEntity)
        }

        // When
        notificationTemplateRepository.saveAll(notificationTemplates)
        val page =
            notificationTemplateService.findNotificationTemplate(filter = NotificationTemplateFilter(content = "mark"))

        // Then
        assertTrue(page.entities.size == 1)
        assertEquals(searchedNotificationEntity.templateId, page.entities.first().templateId)
    }

    @Test
    fun `test notification template pagination with params by range date`() {
        // When
        val fromDate = LocalDateTime.now()
        val toDate = LocalDateTime.now().plusHours(1)
        val fromNotificationEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            createdAt = fromDate
            templateId = UUID.randomUUID().toString()
        }
        val toNotificationEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            createdAt = toDate
            templateId = UUID.randomUUID().toString()
        }
        val notificationTemplates = EasyRandom().objects(NotificationTemplateEntity::class.java, 10).peek {
            it.id = null
            it.createdAt = fromDate.plusDays(1)
        }.collect(Collectors.toList()).also {
            it.add(fromNotificationEntity)
            it.add(toNotificationEntity)
        }

        // When
        notificationTemplateRepository.saveAll(notificationTemplates)
        val firstPage = notificationTemplateService.findNotificationTemplate(
            filter = NotificationTemplateFilter(from = fromDate, to = toDate),
            limit = 1
        )
        val secondPage = notificationTemplateService.findNotificationTemplate(
            continuationToken = firstPage.token,
            filter = NotificationTemplateFilter(from = fromDate, to = toDate),
            limit = 1
        )

        // Then
        assertTrue(firstPage.entities.size == 1)
        assertTrue(firstPage.entities.find { it.templateId == fromNotificationEntity.templateId } != null)
        assertTrue(secondPage.entities.size == 1)
        assertTrue(secondPage.entities.find { it.templateId == toNotificationEntity.templateId } != null)
    }

    @Test
    fun `test notification template pagination with params by fixed date`() {
        // Given
        val date = LocalDateTime.now()
        val notificationEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            createdAt = date
            templateId = UUID.randomUUID().toString()
        }
        val notificationTemplates = EasyRandom().objects(NotificationTemplateEntity::class.java, 10).peek {
            it.id = null
            it.createdAt = date.plusDays(1)
        }.collect(Collectors.toList()).also {
            it.add(notificationEntity)
        }

        // When
        notificationTemplateRepository.saveAll(notificationTemplates)
        val page = notificationTemplateService.findNotificationTemplate(
            filter = NotificationTemplateFilter(date = date),
            limit = 1
        )

        // Then
        assertTrue(page.entities.size == 1)
        assertEquals(notificationEntity.templateId, page.entities.first().templateId)
    }

    @Test
    fun `test notification template pagination order`() {
        // Given
        val notificationTemplates = EasyRandom().objects(NotificationTemplateEntity::class.java, 20).peek {
            it.id = null
            it.templateId = UUID.randomUUID().toString()
            it.status = NotificationTemplateStatus.draft
        }.collect(Collectors.toList())

        // When
        notificationTemplateRepository.saveAll(notificationTemplates)
        val firstPage = notificationTemplateService.findNotificationTemplate(limit = 10)
        val secondPage = notificationTemplateService.findNotificationTemplate(firstPage.token, limit = 10)

        // Then
        var currentId: Long = firstPage.entities.first().id!!
        var currentCreatedAt: LocalDateTime? = firstPage.entities.first().createdAt
        for (entity in firstPage.entities.stream().skip(1)) {
            assertTrue(entity.id!!.compareTo(currentId) >= 1)
            assertTrue(entity.createdAt.compareTo(currentCreatedAt) >= 1)
            currentId = entity.id!!
            currentCreatedAt = entity.createdAt
        }
        assertTrue(firstPage.entities.last().id != secondPage.entities.first().id)
    }
}
