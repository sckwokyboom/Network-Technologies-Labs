# Multicast-Copy-Finder
## Основные параметры
`-k` -- ключ безопасности, позволяющий отсеять нежелательные пакеты;

`-ip` -- ip адрес multicast-группы; поддерживает ipv4, ipv6;

`-r` -- режим приемника; прослушивает трафик и выводит всех publisher, которых удалось зарегистрировать;

`-p` -- режим публикатора; сообщает в сеть UDP-пакеты для обнаружения всеми receiver;

`-port` -- порт multicast-группы.
***
### Запуск
#### Публикатор (piblisher)
`java -jar out/artifacts/CopyFinderApp/CopyFinderApp.jar -k <security key> -p -port <port> -ip <ip address>`
#### Слушатель (receiver)
`java -jar out/artifacts/CopyFinderApp/CopyFinderApp.jar -k <security key> -r -port <port> -ip <ip address>`
***
### Пример
#### IPv4
`java -jar out/artifacts/CopyFinderApp/CopyFinderApp.jar -k 1337 -r -port 8080 -ip 224.2.2.1`

#### IPv6
`java -jar out/artifacts/CopyFinderApp/CopyFinderApp.jar -k 1337 -r -port 8080 -ip FF00:0db8:11a3:09d7:1f34:8a2e:07a0:765d`
***
### Формат multicast сообщений
Ключ безопасности, указанный в аргументах командной строки.
