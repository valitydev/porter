package com.rbkmoney.porter.repository

import com.rbkmoney.geck.common.util.TypeUtil
import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
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
        from: LocalDateTime?,
        to: LocalDateTime?,
        title: String?,
        content: String?,
        fixedDate: LocalDateTime?,
        limit: Int,
    ): Page<NotificationTemplateEntity> {
        val cb: CriteriaBuilder = entityManager.criteriaBuilder
        val query = cb.createQuery(NotificationTemplateEntity::class.java)
        val root = query.from(NotificationTemplateEntity::class.java)

        val predicates = mutableListOf<Predicate>().apply {
            add(titlePredicate(cb, root, title))
            add(contentPredicate(cb, root, content))
            if (fixedDate != null) {
                add(datePredicate(cb, root, fixedDate))
            } else {
                add(fromPredicate(cb, root, from))
            }
            add(toPredicate(cb, root, to))
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
            title?.let { put("title", title) }
            content?.let { put("content", content) }
            from?.let { put("from", TypeUtil.temporalToString(from)) }
            to?.let { put("to", TypeUtil.temporalToString(to)) }
            fixedDate?.let { put("date", TypeUtil.temporalToString(fixedDate)) }
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
            predicates.add(titlePredicate(cb, root, continuationToken.keyParams["title"]))
            predicates.add(contentPredicate(cb, root, continuationToken.keyParams["content"]))
            predicates.add(datePredicate(cb, root, continuationToken.keyParams["date"]))
        }
        predicates.add(
            continuationPredicate(cb, root, continuationToken.timestamp, continuationToken.id.toLong())
        )
        continuationToken.keyParams?.let {
            predicates.add(toPredicate(cb, root, continuationToken.keyParams["to"]))
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

    private fun datePredicate(cb: CriteriaBuilder, root: Root<NotificationTemplateEntity>, date: String?): Predicate {
        return if (date != null) {
            val utcDate = TypeUtil.stringToLocalDateTime(date)
            val createdAtPath = root.get<LocalDateTime>("createdAt")
            val updatedAtPath = root.get<LocalDateTime>("updatedAt")
            cb.or(cb.equal(createdAtPath, utcDate), cb.equal(updatedAtPath, utcDate))
        } else cb.conjunction()
    }

    private fun datePredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        date: LocalDateTime?,
    ): Predicate {
        return if (date != null) {
            val createdAtPath = root.get<LocalDateTime>("createdAt")
            val updatedAtPath = root.get<LocalDateTime>("updatedAt")
            cb.or(cb.equal(createdAtPath, date), cb.equal(updatedAtPath, date))
        } else cb.conjunction()
    }

    private fun fromPredicate(
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

    private fun toPredicate(
        cb: CriteriaBuilder,
        root: Root<NotificationTemplateEntity>,
        to: LocalDateTime?,
    ): Predicate {
        return if (to != null) {
            cb.lessThanOrEqualTo(root.get<LocalDateTime>("createdAt"), to)
        } else cb.conjunction()
    }

    private fun toPredicate(cb: CriteriaBuilder, root: Root<NotificationTemplateEntity>, to: String?): Predicate {
        return if (to != null) {
            val toDate = TypeUtil.stringToLocalDateTime(to)
            cb.lessThanOrEqualTo(root.get<LocalDateTime>("createdAt"), toDate)
        } else cb.conjunction()
    }
}
