CREATE SCHEMA IF NOT EXISTS notify;

CREATE TYPE notify.notification_tpl_status AS ENUM ('draft', 'final');

CREATE TABLE notify.notification_template
(
    id           BIGSERIAL                      NOT NULL,
    template_id  CHARACTER VARYING              NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE    NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    title        VARCHAR                        NOT NULL,
    content      TEXT                           NOT NULL,
    content_type CHARACTER VARYING,
    status       notify.notification_tpl_status NOT NULL,

    UNIQUE (template_id),

    CONSTRAINT notification_template_pk PRIMARY KEY (id)
);

CREATE INDEX notification_template_created_at_idx ON notify.notification_template (created_at);

CREATE TYPE notify.notification_status AS ENUM ('read', 'unread');

CREATE TABLE notify.notification
(
    id              BIGSERIAL                   NOT NULL,
    notification_id CHARACTER VARYING           NOT NULL,
    template_id     BIGINT                      NOT NULL,
    party_id        CHARACTER VARYING           NOT NULL,
    status          notify.notification_status  NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted         BOOLEAN DEFAULT FALSE,

    UNIQUE (notification_id),

    CONSTRAINT notification_pk PRIMARY KEY (id),
    CONSTRAINT notification_to_notification_template_fk FOREIGN KEY (template_id) REFERENCES notify.notification_template (id)
);

CREATE INDEX notification_template_id_idx ON notify.notification (template_id, created_at);
CREATE INDEX notification_status_idx ON notify.notification (status);
CREATE INDEX notification_party_id_idx ON notify.notification (party_id);

CREATE TYPE notify.party_status AS ENUM ('active', 'suspended', 'blocked');

CREATE TABLE notify.party
(
    id         BIGSERIAL                   NOT NULL,
    party_id   CHARACTER VARYING           NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status     notify.party_status         NOT NULL,
    email      CHARACTER VARYING,

    UNIQUE (party_id),

    CONSTRAINT party_pk PRIMARY KEY (id)
);

CREATE INDEX party_party_id_idx ON notify.party (party_id);
CREATE INDEX party_status_idx ON notify.party (status);
