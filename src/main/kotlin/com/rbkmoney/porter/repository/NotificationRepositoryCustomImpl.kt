package com.rbkmoney.porter.repository

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.porter.repository.entity.NotificationEntity
import com.rbkmoney.porter.repository.entity.NotificationStatus
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.service.model.NotificationFilter
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import com.rbkmoney.porter.service.pagination.Page
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.JoinType
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Component
class NotificationRepositoryCustomImpl(
    private val entityManager: EntityManager,
    private val continuationTokenService: ContinuationTokenService,
) : NotificationRepositoryCustom {

    override fun findNotifications(filter: NotificationFilter?, limit: Int): Page<NotificationEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationEntity::class.java)
        val root = query.from(NotificationEntity::class.java)

        val predicates = mutableListOf<Predicate>().apply {
            add(templatePredicate(cb, root, filter?.templateId))
            add(partyPredicate(cb, root, filter?.partyId))
            add(titlePredicate(cb, root, filter?.title))
            add(statusPredicate(cb, root, filter?.status))
            add(deletedPredicate(cb, root, filter?.deleted))
            add(fromTimeToTimePredicate(cb, root, filter?.fromTime, filter?.toTime))
        }

        val idPath = root.get<Long>("id")
        val createdAtPath = root.get<LocalDateTime>("createdAt")
        val criteriaQuery = query.select(root)
            .where(*predicates.toTypedArray())
            .orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        val resultList = entityManager.createQuery(criteriaQuery).setMaxResults(limit + 1).resultList.toList()
        val keyParams = HashMap<String, String>().apply {
            filter?.templateId?.let { put("template_id", it) }
            filter?.partyId?.let { put("party_id", it) }
            filter?.title?.let { put("title", it) }
            filter?.status?.let { put("status", it.name) }
            filter?.deleted?.let { put("deleted", it.toString()) }
            filter?.fromTime?.let { put("from_time", TypeUtil.temporalToString(it)) }
            filter?.toTime?.let { put("to_time", TypeUtil.temporalToString(it)) }
        }

        return continuationTokenService.createPage(resultList, null, keyParams, limit + 1)
    }

    override fun findNextNotifications(continuationToken: ContinuationToken, limit: Int): Page<NotificationEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationEntity::class.java)
        val root = query.from(NotificationEntity::class.java)

        val predicates = mutableListOf<Predicate>().apply {
            continuationToken.keyParams?.let { keyParams ->
                add(templatePredicate(cb, root, keyParams["template_id"]))
                add(partyPredicate(cb, root, keyParams["party_id"]))
                add(titlePredicate(cb, root, keyParams["title"]))
                add(statusPredicate(cb, root, keyParams["status"]))
                add(deletedPredicate(cb, root, keyParams["deleted"]?.let { it.toBoolean() }))
                add(
                    fromTimeToTimePredicate(
                        cb,
                        root,
                        keyParams["from_time"]?.let { TypeUtil.stringToLocalDateTime(it) },
                        keyParams["to_time"]?.let { TypeUtil.stringToLocalDateTime(it) }
                    )
                )
            }
            add(continuationPredicate(cb, root, continuationToken.timestamp, continuationToken.id.toLong()))
        }

        val criteriaQuery = query.select(root)
            .where(*predicates.toTypedArray())
            .orderBy(cb.asc(root.get<Long>("id")), cb.asc(root.get<LocalDateTime>("createdAt")))

        val resultList = entityManager.createQuery(criteriaQuery).setMaxResults(limit + 1).resultList.toList()

        return continuationTokenService.createPage(
            resultList,
            continuationToken,
            continuationToken.keyParams,
            limit + 1
        )
    }

    private fun templatePredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, templateId: String?): Predicate {
        return if (templateId != null) {
            val notificationTemplateJoin =
                root.join<NotificationEntity, NotificationTemplateEntity>("notificationTemplateEntity", JoinType.INNER)
            cb.equal(
                notificationTemplateJoin.get<String>("templateId"),
                templateId
            )
        } else cb.conjunction()
    }

    private fun statusPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationEntity>,
        status: NotificationStatus?,
    ): Predicate {
        return if (status != null) {
            cb.equal(root.get<NotificationStatus>("status"), status)
        } else cb.conjunction()
    }

    private fun statusPredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, status: String?): Predicate {
        return if (status != null) {
            cb.equal(root.get<NotificationStatus>("status"), NotificationStatus.valueOf(status))
        } else cb.conjunction()
    }

    private fun partyPredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, partyId: String?): Predicate {
        return if (partyId != null) {
            cb.equal(root.get<String>("partyId"), partyId)
        } else cb.conjunction()
    }

    private fun deletedPredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, deleted: Boolean?): Predicate {
        return if (deleted != null) {
            cb.equal(root.get<Boolean>("deleted"), deleted)
        } else cb.conjunction()
    }

    private fun fromTimeToTimePredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationEntity>,
        fromTime: LocalDateTime?,
        toTime: LocalDateTime?,
    ): Predicate {
        return if (fromTime != null && toTime != null) {
            val createdAtPath = root.get<LocalDateTime>("createdAt")
            cb.and(cb.greaterThanOrEqualTo(createdAtPath, fromTime), cb.lessThanOrEqualTo(createdAtPath, toTime))
        } else cb.conjunction()
    }

    private fun titlePredicate(cb: CriteriaBuilder, root: Root<NotificationEntity>, title: String?): Predicate {
        return if (title != null) {
            val notificationTemplateJoin =
                root.join<NotificationEntity, NotificationTemplateEntity>("notificationTemplateEntity", JoinType.INNER)
            val searchedTitle = "%${title.lowercase()}%"
            cb.like(
                cb.lower(notificationTemplateJoin.get<String>("title")),
                searchedTitle
            )
        } else cb.conjunction()
    }

    private fun continuationPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationEntity>,
        fromTimestamp: Long,
        id: Long,
    ): Predicate {
        val idPath = root.get<Long>("id")
        val createdAtPath = root.get<LocalDateTime>("createdAt")
        val timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(fromTimestamp), ZoneId.of("UTC"))
        return cb.and(
            cb.greaterThanOrEqualTo(createdAtPath, timestamp),
            cb.greaterThan(idPath, id)
        )
    }
}
