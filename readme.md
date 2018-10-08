# BusTicket

## Sugar

Se ha usado la librería Sugar para trabajar más facil la base de datos local.
La versión usada es: 

`implementation 'com.github.satyan:sugar:1.5'`

> **IMPORTANTE**
> Instant-Run parece evitar que Sugar ORM encuentre las clases "tablas", por lo tanto, no puede crear las tablas de BD la primera vez que se ejecuta la aplicación.
> La solución, cuando ejecute la aplicación por primera vez, desactive Instant-Run una vez para permitir la creación de las tablas de la base de datos. Puede habilitarlo después que las tablas han sido creadas