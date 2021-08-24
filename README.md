# VehicleManagementProject
Приложение состоит из сервера, и множетсва клиентов. Предусмотрено два типа клиентов обычные пользователи и администраторы, согласующие выдачу транспорта(ПДО).
Сервер подключается к базе данных, где и хранятся записи о текущих запросах, архив, данные пользователей, данные об автотранспорте.
### Предпологаемый сценарий использования
Сначала администратор запускает сервер, для запуска сервера, нужно ввести необходимые данные в поля в интерфейсной версии сервера и нажать сохранить. Для запуска сервера, 
нужно нажать соотвествующую кнопку. Чтобы у пользователя была возможность подключаться к серверу и делать запросы на выделение автотранспорта. Занос в базу данных должен производиться
по средством непосредственного подключения к базе данных, в данном конкретном случае pgadmin либо через shell postgres. База данных может быть изменена на желаемую, однако для этого надо будет менять исходный код.
Как только сервер запущен, а пользователь добавлен в базу данных, он может подключиться, заполнить поля и нажать кнопку отправить чтобы отправить запрос на сервер.
При отправлении запроса от пользователя, администратору приходит оповещение, он заполняет необходимые поля, и нажимает кнопку отправить. Пользователю приходит звуковое оповещение,
и окно интерфейса заменяется оповещением. В случае если заявка одобрена в оповещении написан номер выделенного транспортного средства и инструкции к действию. Если пользователь 
нажимает закончить поездку, его время фиксируется, транспорт в базе данных освобождается. Если выбирает закрыть, то программа закрывается и при следующем запуске, она выдаст ему то же самое оповещение.

# Пример интерфейса сервера
![image](https://user-images.githubusercontent.com/24436707/130604310-bb100acd-9ef7-474c-a8c1-3d4c3ecfde01.png)
Как видно на изображении, в интерфейсе отображается количество пользователей и администраторов. После заполенния всех полей нужно нажать клавишу сохранить, дальше нажать запустить сервер.
В случае переключения запоминать все запросы в отдельную таблицу, все пользовательские запросы после обработки администратором, будут отправляться в архив. Архив нельзя редактировать из пользовательского интерефейса.(интерфейса администратора)
# База данных
* Таблица archive
```
CREATE TABLE public.archive
(
    id serial NOT NULL,
    old_id integer,
    fio text COLLATE pg_catalog."default" NOT NULL,
    departure_time time without time zone NOT NULL,
    car_status integer,
    return_time time without time zone,
    pdo text COLLATE pg_catalog."default" NOT NULL,
    note text COLLATE pg_catalog."default",
    gos_num text COLLATE pg_catalog."default",
    CONSTRAINT archieve_pk PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE public.archive
    OWNER to postgres;
```
* Таблица car_list
```
CREATE TABLE public.car_list
(
    car_state integer NOT NULL,
    reg_num text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT reg_num_pk PRIMARY KEY (reg_num)
)

TABLESPACE pg_default;

ALTER TABLE public.car_list
    OWNER to postgres;
```
* Таблица summary
```
CREATE TABLE public.summary
(
    id serial NOT NULL,
    fio text COLLATE pg_catalog."default" NOT NULL,
    departure_time time without time zone NOT NULL,
    car_status integer,
    return_time time without time zone,
    pdo text COLLATE pg_catalog."default" NOT NULL,
    note text COLLATE pg_catalog."default",
    gos_num text COLLATE pg_catalog."default",
    username text COLLATE pg_catalog."default",
    CONSTRAINT summary_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE public.summary
    OWNER to postgres;
```
* Таблица users
```
CREATE TABLE public.users
(
    username text COLLATE pg_catalog."default" NOT NULL,
    password text COLLATE pg_catalog."default" NOT NULL,
    admin boolean,
    CONSTRAINT users_pkey PRIMARY KEY (username)
)

TABLESPACE pg_default;

ALTER TABLE public.users
    OWNER to postgres;
```
Пример данных пользователя
![image](https://user-images.githubusercontent.com/24436707/130606118-af0e516e-c92a-4d2f-9027-39da94c94846.png)

### Пример интерфейса пользователя
![image](https://user-images.githubusercontent.com/24436707/130606292-26e02f44-a528-46a6-91e5-ea69b20440b4.png)
В интерфейсе есть несколько полей ввода, пользователь должен ввести свое имя(обязательно дляввода), заметку(не обязательна для ввода), и желаемое время отправления(обязательно для ввода). В поле имени можно вводить любые символы, также как и в заметки. Поле времени не позволит ввести время неверного формата пример 11:23. После того как пользователь закончил ввод, он нажимает кнопку отправить. //можно подумать над фиксацией имени пользователя в настройках, чтобы оно заполнялось автоматом//
![image](https://user-images.githubusercontent.com/24436707/130607037-3e0eb61b-a43f-4fc1-bde2-ba7acd2fffa1.png)
*В одну сессию пользователь не может отправить больше одного запроса.*
![image](https://user-images.githubusercontent.com/24436707/130607049-17209ed4-68ba-4c9a-9478-3b9bb4634eed.png)
*Окончание поездки*
![image](https://user-images.githubusercontent.com/24436707/130607179-2339ad8a-0e2e-40ac-b326-13ce3d313764.png)
*Ошибка ввода не заполнено одон из обязательных полей*
![image](https://user-images.githubusercontent.com/24436707/130607355-c6528f91-6739-4004-af09-d2cd1e5530f6.png)
*Отказ в согласовании*
![image](https://user-images.githubusercontent.com/24436707/130607438-4d5b079d-6642-4639-8c4b-4b238c2fd926.png)
*Окно настроек из меню файл*
![image](https://user-images.githubusercontent.com/24436707/130607529-acbfe98d-96f3-4d9b-b322-69043929e4ac.png)
*Кнопка переподключения*
![image](https://user-images.githubusercontent.com/24436707/130607573-a2a3952a-917b-4af3-bcf1-e2e9ffa14c44.png)

