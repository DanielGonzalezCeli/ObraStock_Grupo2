# ObraStock

Aplicación Android nativa para llevar el inventario de materiales de construcción por obra.
Proyecto final de Programación Móvil, PUCE (Facultad de Hábitat, Infraestructura y Creatividad).

## Integrantes

- Daniel González
- Dilan Mena
- Samantha Nuñez
- Juan Hoyos

## Estado: Fase 1 (avance)

Incluye la base técnica del proyecto:

- Configuración con catálogo de versiones (`gradle/libs.versions.toml`), SDK 36 y Java 21.
- Tema Material 3 con color dinámico.
- Navegación tipada con rutas `@Serializable`.
- Tres pantallas en Compose con datos de ejemplo: Bodega, Registro de material y Detalle con estados de sincronización.
- Estructura de Room (entidades, DAO y base de datos) y contratos del dominio.
- Configuración inicial de Retrofit para el servicio en Supabase.

Todavía no incluye la lógica real (repositorios, ViewModel, sincronización) ni el módulo opcional de lectura de facturas.
Eso corresponde a la Fase 2.

## Arquitectura

Tres capas: `ui` (pantallas y navegación), `domain` (modelos y contratos) y `data` (Room y Retrofit).

## Cómo ejecutarlo

1. Abrir la carpeta del proyecto con Android Studio y esperar la sincronización de Gradle.
2. Ejecutar en un emulador o dispositivo con Android 8.0 (API 26) o superior.
3. Para la parte de red, editar `data/remote/ConfiguracionRemota.kt` con los datos de Supabase del grupo.

## Informe

El informe técnico preliminar en PDF está en la carpeta `docs/`.
