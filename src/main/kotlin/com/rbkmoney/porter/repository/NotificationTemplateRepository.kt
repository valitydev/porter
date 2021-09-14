package com.rbkmoney.porter.repository

import com.rbkmoney.porter.repository.entity.NotificationTemplateEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationTemplateRepository :
    CrudRepository<NotificationTemplateEntity, Long>,
    NotificationTemplateRepositoryCustom {

    fun findByTemplateId(templateId: String): NotificationTemplateEntity?

    fun deleteByTemplateId(templateId: String)
}
