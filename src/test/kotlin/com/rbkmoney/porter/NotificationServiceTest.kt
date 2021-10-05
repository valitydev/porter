package com.rbkmoney.porter

import com.rbkmoney.porter.repository.NotificationRepository
import com.rbkmoney.porter.repository.NotificationTemplateRepository
import com.rbkmoney.porter.repository.PartyRepository
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.repository.entity.PartyEntity
import com.rbkmoney.porter.repository.entity.PartyStatus
import com.rbkmoney.porter.service.IdGenerator
import com.rbkmoney.porter.service.NotificationService
import com.rbkmoney.porter.service.model.NotificationFilter
import org.hibernate.Hibernate
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import java.time.LocalDateTime
import java.util.stream.Collectors
import java.util.stream.Stream

@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate"
    ]
)
class NotificationServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var notificationTemplateRepository: NotificationTemplateRepository

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @Autowired
    lateinit var partyRepository: PartyRepository

    @Autowired
    lateinit var notificationService: NotificationService

    lateinit var notificationTemplateEntity: NotificationTemplateEntity

    lateinit var partyEntity: PartyEntity

    @BeforeEach
    internal fun setUp() {
        notificationRepository.deleteAll()
        notificationTemplateRepository.deleteAll()
        partyRepository.deleteAll()
        val partyObject = EasyRandom().nextObject(PartyEntity::class.java).apply {
            id = null
            partyId = IdGenerator.randomString()
        }
        partyEntity = partyRepository.save(partyObject)
        val notificationTemplateObject = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            templateId = TEMPLATE_ID
            title = TEMPLATE_TITLE
        }
        notificationTemplateEntity = notificationTemplateRepository.save(notificationTemplateObject)
    }

    @Test
    fun `create notification with parties test`() {
        // Given
        val partyEntities = EasyRandom().objects(PartyEntity::class.java, 10)
            .peek {
                it.status = PartyStatus.active
                it.partyId = IdGenerator.randomString()
            }
            .collect(Collectors.toList())

        // When
        partyRepository.saveAll(partyEntities)
        notificationService.createNotifications(
            TEMPLATE_ID,
            partyEntities.stream().limit(5).map { it.partyId!! }.collect(Collectors.toList())
        )
        val notifications = notificationRepository.findAll().toList()
        val notificationTemplateEntity = notificationTemplateRepository.findByTemplateId(TEMPLATE_ID)!!
        Hibernate.initialize(notificationTemplateEntity.notifications)

        // Then
        assertTrue(notifications.size == 5)
        assertTrue(notificationTemplateEntity.notifications.size == 5)
    }

    @Test
    fun `create notification test`() {
        // Given
        val partyEntities = EasyRandom().objects(PartyEntity::class.java, 30)
            .peek {
                it.status = PartyStatus.active
                it.partyId = IdGenerator.randomString()
            }
            .collect(Collectors.toList())

        // When
        partyRepository.saveAll(partyEntities)
        notificationService.createNotifications(TEMPLATE_ID)
        val notifications = notificationRepository.findAll().toList()

        // Then
        assertTrue(notifications.size == 30)
    }

    @Test
    fun `find notification pagination test`() {
        // Given
        val partyEntities = EasyRandom().objects(PartyEntity::class.java, 10)
            .peek {
                it.status = PartyStatus.active
                it.partyId = IdGenerator.randomString()
            }
            .collect(Collectors.toList())

        // When
        partyRepository.saveAll(partyEntities)
        notificationService.createNotifications(TEMPLATE_ID)
        val page = notificationService.findNotifications(NotificationFilter(templateId = TEMPLATE_ID), limit = 5)
        val secondPage = notificationService.findNotifications(
            NotificationFilter(TEMPLATE_ID),
            continuationToken = page.token,
            limit = 10
        )

        // Then
        assertTrue(page.hasNext)
        assertTrue(page.entities.size == 5)
        assertFalse(secondPage.hasNext)
        assertTrue(secondPage.entities.size == 5)
    }

    @Test
    fun `find notification pagination with status param`() {
        // Given
        val notificationEntities = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.partyEntity = partyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.status = NotificationStatus.unread
                it.notificationId = IdGenerator.randomString()
                it.deleted = false
            }
            .collect(Collectors.toList())

        // When
        notificationRepository.saveAll(notificationEntities)
        val page = notificationService.findNotifications(
            filter = NotificationFilter(templateId = TEMPLATE_ID, status = NotificationStatus.unread),
            limit = 20
        )

        // Then
        assertFalse(page.hasNext)
        assertTrue(page.entities.size == 10)
    }

    @Test
    fun `find notification pagination with party param`() {
        // Given
        val firstNotificationEntitiesStream = EasyRandom().objects(NotificationEntity::class.java, 6)
            .peek {
                it.notificationTemplateEntity = notificationTemplateEntity
                it.status = NotificationStatus.unread
                it.notificationId = IdGenerator.randomString()
                it.partyEntity = partyEntity
                it.deleted = false
            }
        val secondPartyEntity = partyRepository.save(
            EasyRandom().nextObject(PartyEntity::class.java).apply {
                id = null
                partyId = IdGenerator.randomString()
            }
        )
        val secondNotificationEntitiesStream = EasyRandom().objects(NotificationEntity::class.java, 5)
            .peek {
                it.partyEntity = secondPartyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.status = NotificationStatus.unread
                it.notificationId = IdGenerator.randomString()
                it.deleted = false
            }
        val entities = Stream.concat(firstNotificationEntitiesStream, secondNotificationEntitiesStream)
            .collect(Collectors.toList())

        // When
        notificationRepository.saveAll(entities)
        val firstPage = notificationService.findNotifications(
            filter = NotificationFilter(partyId = partyEntity.partyId),
            limit = 3
        )
        val secondPage = notificationService.findNotifications(
            filter = NotificationFilter(partyId = partyEntity.partyId),
            continuationToken = firstPage.token,
            limit = 3
        )

        // Then
        assertTrue(firstPage.hasNext)
        firstPage.entities.forEach { assertEquals(partyEntity.partyId, it.partyEntity!!.partyId) }
        assertFalse(secondPage.hasNext)
        secondPage.entities.forEach { assertEquals(partyEntity.partyId, it.partyEntity!!.partyId) }
    }

    @Test
    fun `find notification pagination with deleted param`() {
        // Given
        val firstNotificationEntitiesStream = EasyRandom().objects(NotificationEntity::class.java, 6)
            .peek {
                it.partyEntity = partyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.status = NotificationStatus.unread
                it.notificationId = IdGenerator.randomString()
                it.deleted = true
            }
        val secondPartyEntity = partyRepository.save(
            EasyRandom().nextObject(PartyEntity::class.java).apply {
                id = null
                partyId = IdGenerator.randomString()
            }
        )
        val secondNotificationEntitiesStream = EasyRandom().objects(NotificationEntity::class.java, 5)
            .peek {
                it.partyEntity = secondPartyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.status = NotificationStatus.unread
                it.notificationId = IdGenerator.randomString()
                it.deleted = false
            }
        val entities = Stream.concat(firstNotificationEntitiesStream, secondNotificationEntitiesStream)
            .collect(Collectors.toList())

        // When
        notificationRepository.saveAll(entities)
        val firstPage = notificationService.findNotifications(
            filter = NotificationFilter(deleted = true),
            limit = 3
        )
        val secondPage = notificationService.findNotifications(
            filter = NotificationFilter(deleted = true),
            continuationToken = firstPage.token,
            limit = 3
        )

        // Then
        assertTrue(firstPage.hasNext)
        firstPage.entities.forEach { assertEquals(true, it.deleted) }
        assertFalse(secondPage.hasNext)
        secondPage.entities.forEach { assertEquals(true, it.deleted) }
    }

    @Test
    fun `find notification pagination with fromTo param`() {
        // Given
        val fromTime = LocalDateTime.now()
        val toTime = fromTime.plusDays(1)
        val firstNotificationEntitiesStream = EasyRandom().objects(NotificationEntity::class.java, 6)
            .peek {
                it.partyEntity = partyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.status = NotificationStatus.unread
                it.notificationId = IdGenerator.randomString()
                it.createdAt = fromTime
                it.deleted = false
            }
        val secondNotificationEntitiesStream = EasyRandom().objects(NotificationEntity::class.java, 5)
            .peek {
                it.partyEntity = partyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.status = NotificationStatus.unread
                it.notificationId = IdGenerator.randomString()
                it.createdAt = fromTime.plusDays(10)
                it.deleted = false
            }

        val entities = Stream.concat(firstNotificationEntitiesStream, secondNotificationEntitiesStream)
            .collect(Collectors.toList())

        // When
        notificationRepository.saveAll(entities)
        val firstPage = notificationService.findNotifications(
            filter = NotificationFilter(fromTime = fromTime, toTime = toTime),
            limit = 3
        )
        val secondPage = notificationService.findNotifications(
            filter = NotificationFilter(fromTime = fromTime, toTime = toTime),
            continuationToken = firstPage.token,
            limit = 3
        )

        // Then
        assertTrue(firstPage.hasNext)
        firstPage.entities.forEach { assertEquals(fromTime.withNano(0), it.createdAt.withNano(0)) }
        assertFalse(secondPage.hasNext)
        secondPage.entities.forEach { assertEquals(fromTime.withNano(0), it.createdAt.withNano(0)) }
    }

    @Test
    fun `find total notification`() {
        // Given
        val unreadNotificationsStream = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.partyEntity = partyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.notificationId = IdGenerator.randomString()
                it.status = NotificationStatus.unread
            }
        val readNotificationStream = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.partyEntity = partyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.notificationId = IdGenerator.randomString()
                it.status = NotificationStatus.read
            }
        val notifications =
            Stream.concat(unreadNotificationsStream, readNotificationStream).collect(Collectors.toList())

        // When
        notificationRepository.saveAll(notifications)
        val notificationTotal = notificationService.findNotificationStats(TEMPLATE_ID)

        // Then
        assertTrue(notificationTotal.total == 20L)
        assertTrue(notificationTotal.read == 10L)
    }

    @Test
    fun `test notification pagination order`() {
        // Given
        val partyEntities = EasyRandom().objects(PartyEntity::class.java, 10)
            .peek {
                it.partyId = IdGenerator.randomString()
                it.status = PartyStatus.active
            }
            .collect(Collectors.toList())

        // When
        partyRepository.saveAll(partyEntities)
        notificationService.createNotifications(TEMPLATE_ID)
        val page = notificationService.findNotifications(NotificationFilter(templateId = TEMPLATE_ID), limit = 5)
        val secondPage = notificationService.findNotifications(
            NotificationFilter(templateId = TEMPLATE_ID), continuationToken = page.token, limit = 5
        )

        // Then
        var currentId: Long = page.entities.first().id!!
        var currentCreatedAt: LocalDateTime? = page.entities.first().createdAt
        for (entity in page.entities.stream().skip(1)) {
            assertTrue(entity.id!!.compareTo(currentId) >= 1)
            assertTrue(entity.createdAt.compareTo(currentCreatedAt) >= 1)
            currentId = entity.id!!
            currentCreatedAt = entity.createdAt
        }
        assertTrue(page.entities.last().id != secondPage.entities.first().id)
    }

    @Test
    fun `find notification by title`() {
        // Given
        val notificationTemplateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            templateId = IdGenerator.randomString()
            title = "My second title"
        }
        val firstNotificationStream = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.partyEntity = this.partyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.notificationId = IdGenerator.randomString()
                it.status = NotificationStatus.unread
                it.deleted = false
            }
        val secondNotificationStream = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.partyEntity = this.partyEntity
                it.notificationTemplateEntity = this.notificationTemplateEntity
                it.notificationId = IdGenerator.randomString()
                it.status = NotificationStatus.unread
                it.deleted = false
            }
        val notifications =
            Stream.concat(firstNotificationStream, secondNotificationStream).collect(Collectors.toList())

        // When
        notificationTemplateRepository.save(notificationTemplateEntity)
        notificationRepository.saveAll(notifications)
        notificationService.createNotifications(notificationTemplateEntity.templateId!!)
        val firstPage = notificationService.findNotifications(NotificationFilter(title = "second"), limit = 5)
        val secondPage = notificationService.findNotifications(
            NotificationFilter(title = "second"), continuationToken = firstPage.token, limit = 5
        )

        // Then
        assertTrue(firstPage.hasNext)
        firstPage.entities.forEach {
            assertEquals(
                notificationTemplateEntity.title,
                it.notificationTemplateEntity?.title
            )
        }
        assertFalse(secondPage.hasNext)
        secondPage.entities.forEach {
            assertEquals(
                notificationTemplateEntity.title,
                it.notificationTemplateEntity?.title
            )
        }
    }

    @Test
    fun `find notification by email`() {
        // Given
        val notificationTemplateEntity = EasyRandom().nextObject(NotificationTemplateEntity::class.java).apply {
            id = null
            templateId = IdGenerator.randomString()
            title = "Some test title"
        }
        val notificationEntity = EasyRandom().nextObject(NotificationEntity::class.java).apply {
            this.notificationTemplateEntity = notificationTemplateEntity
            this.partyEntity = this@NotificationServiceTest.partyEntity
        }

        // When
        notificationTemplateRepository.save(notificationTemplateEntity)
        notificationRepository.save(notificationEntity)
        notificationService.createNotifications(notificationTemplateEntity.templateId!!)
        val notificationPage = notificationService.findNotifications(NotificationFilter(email = partyEntity.email))

        // Then
        assertFalse(notificationPage.hasNext)
        assertTrue(notificationPage.entities.size == 1)
        assertEquals(partyEntity.email, notificationPage.entities.first().partyEntity?.email)
    }

    @Test
    fun `get notification test`() {
        // Given
        val notificationEntity = EasyRandom().nextObject(NotificationEntity::class.java).apply {
            this.partyEntity = this@NotificationServiceTest.partyEntity
            this.notificationTemplateEntity = this@NotificationServiceTest.notificationTemplateEntity
            this.notificationId = IdGenerator.randomString()
            this.status = NotificationStatus.unread
        }

        // When
        notificationRepository.save(notificationEntity)
        val notification = notificationService.getNotification(notificationEntity.notificationId!!)!!

        // Then
        assertEquals(notificationEntity.partyEntity, notification.partyEntity)
        assertEquals(notificationEntity.createdAt.withNano(0), notification.createdAt.withNano(0))
        assertEquals(notificationEntity.deleted, notification.deleted)
        assertEquals(notificationEntity.notificationId, notification.notificationId)
        assertEquals(notificationEntity.status, notification.status)
        assertEquals(notificationEntity.notificationTemplateEntity, notification.notificationTemplateEntity)
    }

    @Test
    fun `mark selected notifications test`() {
        // Given
        val unreadNotifications = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.notificationTemplateEntity = notificationTemplateEntity
                it.notificationId = IdGenerator.randomString()
                it.status = NotificationStatus.unread
                it.partyEntity = partyEntity
            }.collect(Collectors.toList())

        // When
        notificationRepository.saveAll(unreadNotifications)
        val notificationIds = unreadNotifications.map { it.notificationId!! }
        notificationService.notificationMark(partyEntity.partyId!!, notificationIds, NotificationStatus.read)
        val notifications = notificationRepository.findByNotificationIdIn(notificationIds)

        // Then
        assertTrue(notifications.size == notificationIds.size)
        notifications.forEach {
            assertTrue(it.status == NotificationStatus.read)
        }
    }

    @Test
    fun `mark all notifications test`() {
        // Given
        val readNotifications = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.notificationTemplateEntity = notificationTemplateEntity
                it.notificationId = IdGenerator.randomString()
                it.status = NotificationStatus.read
                it.partyEntity = partyEntity
            }.collect(Collectors.toList())

        // When
        notificationRepository.saveAll(readNotifications)
        val notificationIds = readNotifications.map { it.notificationId!! }
        notificationService.notificationMarkAll(partyEntity.partyId!!, NotificationStatus.unread)
        val notifications = notificationRepository.findByNotificationIdIn(notificationIds)

        // Then
        assertTrue(notifications.size == notificationIds.size)
        notifications.forEach {
            assertTrue(it.status == NotificationStatus.unread)
        }
    }

    @Test
    fun `delete notification test`() {
        // Given
        val notificationEntity = EasyRandom().nextObject(NotificationEntity::class.java).apply {
            this.partyEntity = this@NotificationServiceTest.partyEntity
            this.notificationTemplateEntity = this@NotificationServiceTest.notificationTemplateEntity
            this.notificationId = IdGenerator.randomString()
            this.status = NotificationStatus.unread
        }

        // When
        notificationRepository.save(notificationEntity)
        notificationService.softDeleteNotification(
            notificationEntity.partyEntity!!.partyId!!,
            notificationEntity.notificationId!!
        )
        val foundedNotificationEntity = notificationRepository.findByNotificationId(notificationEntity.notificationId!!)

        // Then
        assertTrue(foundedNotificationEntity != null)
        assertTrue(foundedNotificationEntity!!.deleted)
    }

    @Test
    fun `delete multiple notification test`() {
        // Given
        val notifications = EasyRandom().objects(NotificationEntity::class.java, 10)
            .peek {
                it.partyEntity = partyEntity
                it.notificationTemplateEntity = notificationTemplateEntity
                it.notificationId = IdGenerator.randomString()
                it.status = NotificationStatus.read
                it.partyEntity = partyEntity
            }.collect(Collectors.toList())

        // When
        notificationRepository.saveAll(notifications)
        val notificationIds = notifications.map { it.notificationId!! }.toTypedArray()
        notificationService.softDeleteNotification(partyEntity.partyId!!, *notificationIds)
        val foundedNotifications = notificationRepository.findByNotificationIdIn(notificationIds.toList())

        // Then
        assertTrue(foundedNotifications.size == notificationIds.size)
        foundedNotifications.forEach {
            assertTrue(it.deleted)
        }
    }

    companion object {
        const val TEMPLATE_ID = "testTemplateId"
        const val TEMPLATE_TITLE = "My first title"
    }
}
