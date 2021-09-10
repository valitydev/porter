package com.rbkmoney.porter.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.rbkmoney.openapi.notification.model.DeleteNotification
import com.rbkmoney.openapi.notification.model.MarkAllNotifications
import com.rbkmoney.openapi.notification.model.MarkNotifications
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.service.IdGenerator
import com.rbkmoney.porter.service.NotificationService
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.pagination.Page
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.jeasy.random.EasyRandom
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID

class NotificationControllerTest : AbstractControllerTest() {

    @MockBean
    lateinit var notificationService: NotificationService

    @Test
    fun `delete notification`() {
        // Given
        val notificationId = IdGenerator.randomString()

        // When
        val mvcActions = mockMvc.perform(
            MockMvcRequestBuilders.delete(String.format("/notification/%s", notificationId))
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId")
        )

        // Then
        argumentCaptor<String> {
            verify(notificationService, times(1)).softDeleteNotification(anyString(), capture())
            assertEquals(notificationId, firstValue)
        }
    }

    @Test
    fun `delete multiple notification`() {
        // Given
        val firstNotificationId = UUID.randomUUID()
        val secondNotificationId = UUID.randomUUID()
        val deleteNotification = DeleteNotification().apply {
            addNotificationIdsItem(firstNotificationId)
            addNotificationIdsItem(secondNotificationId)
        }

        // When
        val mvcActions = mockMvc.perform(
            MockMvcRequestBuilders.delete("/notification/remove")
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId")
                .content(ObjectMapper().writeValueAsString(deleteNotification))
        )

        // Then
        argumentCaptor<String> {
            verify(notificationService, times(1)).softDeleteNotification(anyString(), capture())
            assertTrue(allValues.contains(firstNotificationId.toString()))
            assertTrue(allValues.contains(secondNotificationId.toString()))
        }
    }

    @Test
    fun `get notification`() {
        // Given
        val notificationId = IdGenerator.randomString()
        val notificationEntity = EasyRandom().nextObject(NotificationEntity::class.java).apply {
            this.notificationId = notificationId.toString()
        }
        whenever(notificationService.getNotification(anyString())).then { notificationEntity }

        // When
        val mvcActions = mockMvc.perform(
            MockMvcRequestBuilders.get(String.format("/notification/%s", notificationId))
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId")
        )

        // Then
        mvcActions.andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id", equalTo(notificationId)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.status", equalTo(notificationEntity.status.name)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.title",
                    equalTo(notificationEntity.notificationTemplateEntity?.title)
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.content",
                    equalTo(notificationEntity.notificationTemplateEntity?.content)
                )
            )
    }

    @Test
    fun `mark notification`() {
        // Given
        val notificationId = UUID.randomUUID()
        val requestBody = MarkNotifications().apply {
            status = MarkNotifications.StatusEnum.READ
            notificationIds = mutableListOf(notificationId)
        }

        // When
        val mvcActions = mockMvc.perform(
            MockMvcRequestBuilders.post("/notification/mark")
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId")
                .content(ObjectMapper().writeValueAsString(requestBody))
        )

        // Then
        mvcActions.andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
        argumentCaptor<List<String>> {
            verify(notificationService, times(1)).notificationMark(anyString(), capture(), anyOrNull())
            assertEquals(notificationId.toString(), firstValue[0])
        }
    }

    @Test
    fun `mark multiple notification`() {
        // Given
        val notificationId = UUID.randomUUID()
        val requestBody = MarkAllNotifications().apply {
            status = MarkAllNotifications.StatusEnum.READ
        }

        // When
        val mvcActions = mockMvc.perform(
            MockMvcRequestBuilders.post("/notification/mark/all")
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId")
                .content(ObjectMapper().writeValueAsString(requestBody))
        )

        // Then
        mvcActions.andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
        argumentCaptor<NotificationStatus> {
            verify(notificationService, times(1)).notificationMarkAll(anyString(), capture())
            assertEquals(NotificationStatus.read, firstValue)
        }
    }

    @Test
    fun `search notifications`() {
        // Given
        val titleFilter = "my title"
        val notificationEntity = EasyRandom().nextObject(NotificationEntity::class.java).apply {
            notificationId = IdGenerator.randomString()
            status = NotificationStatus.unread
        }

        //  When
        whenever(notificationService.findNotifications(anyOrNull(), anyOrNull(), anyInt())).then {
            Page(listOf(notificationEntity), token = null, hasNext = false)
        }
        val mvcActions = mockMvc.perform(
            MockMvcRequestBuilders.get("/notification")
                .contentType("application/json")
                .header("Authorization", "Bearer " + generateRbkAdminJwt())
                .header("X-Request-ID", "testRequestId")
                .queryParam("title", titleFilter)
        )

        // Then
        mvcActions.andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
            .andExpect(MockMvcResultMatchers.jsonPath("$.result").isNotEmpty)
            .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].id", `is`(notificationEntity.notificationId)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result[0].status", `is`(notificationEntity.status.name)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.result[0].title",
                    `is`(notificationEntity.notificationTemplateEntity?.title)
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                    "$.result[0].content",
                    `is`(notificationEntity.notificationTemplateEntity?.content)
                )
            )
        argumentCaptor<NotificationFilter> {
            verify(notificationService, times(1)).findNotifications(capture(), anyOrNull(), anyInt())
            assertEquals(titleFilter, firstValue.title)
        }
    }
}
