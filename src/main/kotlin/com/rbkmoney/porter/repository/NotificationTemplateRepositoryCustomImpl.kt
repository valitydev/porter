package com.rbkmoney.porter.repository

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import com.rbkmoney.porter.service.model.DateFilter
import com.rbkmoney.porter.service.pagination.ContinuationToken
import com.rbkmoney.porter.service.pagination.ContinuationTokenService
import com.rbkmoney.porter.service.pagination.Page
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Component
class NotificationTemplateRepositoryCustomImpl(
    private val entityManager: EntityManager,
    private val continuationTokenService: ContinuationTokenService,
) : NotificationTemplateRepositoryCustom {

    override fun findNotificationTemplates(
        createdAt: DateFilter?,
        sentAt: DateFilter?,
        title: String?,
        content: String?,
        limit: Int,
    ): Page<NotificationTemplateEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationTemplateEntity::class.java)
        val root = query.from(NotificationTemplateEntity::class.java)

        val predicates = mutableListOf<Predicate>().apply {
            add(titlePredicate(cb, root, title))
            add(contentPredicate(cb, root, content))
            if (createdAt != null) {
                add(createdAtFromPredicate(cb, root, createdAt.from))
                add(createdAtToPredicate(cb, root, createdAt.to))
            }
            if (sentAt != null) {
                add(sentAtFromPredicate(cb, root, sentAt.from))
                add(sentAtToPredicate(cb, root, sentAt.to))
            }
        }

        val criteriaQuery = query.select(root)
            .where(
                cb.and(*predicates.toTypedArray())
            ).orderBy(
                cb.asc(root.get<Long>("id")),
                cb.asc(root.get<LocalDateTime>("createdAt"))
            )

        val resultList = entityManager.createQuery(criteriaQuery).setMaxResults(limit + 1).resultList.toList()
        val keyParams = HashMap<String, String>().apply {
            title?.let { put(TITLE_PARAM, title) }
            content?.let { put(CONTENT_PARAM, content) }
            createdAt?.let {
                put(CREATED_AT_FROM_PARAM, TypeUtil.temporalToString(it.from))
                put(CREATED_AT_TO_PARAM, TypeUtil.temporalToString(it.to))
            }
            sentAt?.let {
                put(SENT_AT_FROM_PARAM, TypeUtil.temporalToString(it.from))
                put(SENT_AT_TO_PARAM, TypeUtil.temporalToString(it.to))
            }
        }

        return continuationTokenService.createPage(resultList, null, keyParams, limit + 1)
    }

    override fun findNextNotificationTemplates(
        continuationToken: ContinuationToken,
        limit: Int,
    ): Page<NotificationTemplateEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationTemplateEntity::class.java)
        val root = query.from(NotificationTemplateEntity::class.java)
        val idPath = root.get<Long>("id")
        val createdAtPath = root.get<LocalDateTime>("createdAt")

        val predicates = mutableListOf<Predicate>()
        continuationToken.keyParams?.let {
            predicates.add(titlePredicate(cb, root, continuationToken.keyParams[TITLE_PARAM]))
            predicates.add(contentPredicate(cb, root, continuationToken.keyParams[CONTENT_PARAM]))
        }
        predicates.add(
            continuationPredicate(cb, root, continuationToken.timestamp, continuationToken.id.toLong())
        )
        continuationToken.keyParams?.let {
            predicates.add(createdAtToPredicate(cb, root, continuationToken.keyParams[CREATED_AT_TO_PARAM]))
            predicates.add(sentAtToPredicate(cb, root, continuationToken.keyParams[SENT_AT_TO_PARAM]))
        }

        val criteriaQuery = query.select(root)
            .where(
                *predicates.toTypedArray()
            ).orderBy(cb.asc(createdAtPath), cb.asc(idPath))

        val resultList = entityManager.createQuery(criteriaQuery).setMaxResults(limit + 1).resultList.toList()

        return continuationTokenService.createPage(
            resultList,
            continuationToken,
            continuationToken.keyParams,
            limit + 1
        )
    }

    private fun titlePredicate(cb: CriteriaBuilder, root: Root<NotificationTemplateEntity>, title: String?): Predicate {
        return if (title != null) {
            val searchedTitle = "%${title.lowercase()}%"
            cb.like(cb.lower(root.get<String>("title")), searchedTitle)
        } else cb.conjunction()
    }

    private fun contentPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        content: String?,
    ): Predicate {
        return if (content != null) {
            val searchedText = "%${content.lowercase()}%"
            cb.like(cb.lower(root.get<String>("content")), searchedText)
        } else cb.conjunction()
    }

    private fun createdAtFromPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        from: LocalDateTime?,
    ): Predicate {
        val createdAtPath = root.get<LocalDateTime>("createdAt")
        return cb.greaterThanOrEqualTo(
            createdAtPath,
            from ?: LocalDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.of("UTC"))
        )
    }

    private fun sentAtFromPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        from: LocalDateTime?,
    ): Predicate {
        return if (from != null) {
            val sentAtPath = root.get<LocalDateTime>("sentAt")
            return cb.greaterThanOrEqualTo(sentAtPath, from)
        } else cb.conjunction()
    }

    private fun continuationPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
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

    private fun createdAtToPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        to: LocalDateTime?,
    ): Predicate {
        return if (to != null) {
            cb.lessThanOrEqualTo(root.get<LocalDateTime>("createdAt"), to)
        } else cb.conjunction()
    }

    private fun createdAtToPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        to: String?
    ): Predicate {
        return if (to != null) {
            val toDate = TypeUtil.stringToLocalDateTime(to)
            cb.lessThanOrEqualTo(root.get<LocalDateTime>("createdAt"), toDate)
        } else cb.conjunction()
    }

    private fun sentAtToPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        to: LocalDateTime?,
    ): Predicate {
        return if (to != null) {
            cb.lessThanOrEqualTo(root.get<LocalDateTime>("sentAt"), to)
        } else cb.conjunction()
    }

    private fun sentAtToPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        to: String?
    ): Predicate {
        return if (to != null) {
            val toDate = TypeUtil.stringToLocalDateTime(to)
            cb.lessThanOrEqualTo(root.get<LocalDateTime>("sentAt"), toDate)
        } else cb.conjunction()
    }

    private companion object KeyParams {
        const val TITLE_PARAM = "title"
        const val CONTENT_PARAM = "content"
        const val CREATED_AT_FROM_PARAM = "created_at_from"
        const val CREATED_AT_TO_PARAM = "created_at_to"
        const val SENT_AT_FROM_PARAM = "sent_at_from"
        const val SENT_AT_TO_PARAM = "sent_at_to"
    }
}
