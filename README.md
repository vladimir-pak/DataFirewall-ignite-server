# DataFirewall Ignite Server

Приложение с Ignite Server.

### Сборка приложения
Запуск собранного jar выполняется по команде:
```bash
mvn clean package
  ```

### Запуск приложения
Запуск собранного jar выполняется по команде:
```bash
# 1 node
java \
  -Dignite.local.host=10.10.1.11 \
  -Dignite.discovery.addresses=10.10.1.11:47500..47520,10.10.1.12:47500..47520 \
  -Dignite.config.file=/opt/ignite/conf/ignite-server.properties \
  -Dignite.instance.name=ignite-node-a \
  -jar ignite-server.jar

# 2 node
java \
  -Dignite.local.host=10.10.1.12 \
  -Dignite.discovery.addresses=10.10.1.11:47500..47520,10.10.1.12:47500..47520 \
  -Dignite.config.file=/opt/ignite/conf/ignite-server.properties \
  -Dignite.instance.name=ignite-node-b \
  -jar ignite-server.jar
  ```

Обязательно указать путь до ignite-server.properties.
Параметры ignite.local.host и ignite.discovery.addresses - если не указаны при запуске, то применяются из ignite-server.properties.

После поднятия кластера необходимо создать пользователя.

```bash
# Подключиться под дефолтным пользователем
ignite / ignite

# Создать пользователя
CREATE USER app_user WITH PASSWORD 'strong_password';

# (опционально) удалить дефолтного пользователя
DROP USER ignite;
  ```