# porter
Сервис для управления нотификациями мерчанта.
В текущей реализации сервис отвечает за создание, отображение, отслеживание уведомлений мерчанта.

## Общая схема работы сервиса
![Schema](doc/service-schema.jpg)
Frontend делится на две части. В общих чертах функционал такой:

**Control-center**. Предназначен для администраторов. Некоторые из функций который он на себя берет:
- создание/редактирование уведомлений
- поиск шаблонов уведомлений
- поиск мерчантов отвечающих заданным критериям

Протокол взаимодействия [Thrift](https://github.com/valitydev/notification-proto)

**Notification UI**. Предназначен для пользователей. Имеет следующий функционал:
- показывает список нотификаций мерчанта
- отмечать нотификации прочитано/не прочитано
- удалять нотификации

Протокол взаимодействия [OpenAPI](https://github.com/valitydev/swag-notification)

**Analytics**. Сервис аналитики используется для поиска мерчантов по различным критериям
Полученный список мерчантов передается в сервис **Porter** для создания уведомлений

**Porter**. Имплементирует протоколы для RPC и REST вызовов.
Читает события по party (мерчанту) из Kafka и сохраняет в базу для последующей отправки нотификаций по всем мерчантам

## Ссылки на протоколы
[Thrift](https://github.com/valitydev/notification-proto)

[OpenAPI](https://github.com/valitydev/swag-notification)
