# Лабораторная работа 3 — Docker и CI для API-автотестов

Контейнер запускает существующие API-тесты из [`lab-2`](lab-2/README.md). 
Тесты выполняют HTTP-запросы к публичным Playground REST API и Postman Echo
GraphQL.

## Локальный запуск

Из корня репозитория при запущенном Docker:

```bash
docker build -t api-test ./lab-2
docker run --rm api-test
```

При старте контейнера Maven автоматически запускает все тесты. Результаты видны
в консоли. Код завершения `docker run` равен 0 при успешном прогоне и ненулевой
при ошибке теста.

## CI

[GitHub Actions workflow](.github/workflows/ci.yml) запускается при `push` в
любую ветку, доступен также ручной запуск. Job получает текущий checkout,
собирает image из `lab-2` и запускает этот же image командой
`docker run --rm api-test:ci`.
