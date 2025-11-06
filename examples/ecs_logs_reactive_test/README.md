# Proyecto Ejemplo para la Implementación de la Librería ECS

Este proyecto es una guía práctica para implementar la librería ECS en Java Reactivo. El objetivo es mostrar cómo se puede implementar la libreria ECS de manera clara.

## Estructura del Proyecto

El proyecto está dividido en los siguientes módulos:

- **Domain**: Encapsula la lógica y reglas del negocio mediante modelos y entidades del dominio.
- **Usecases**: Implementa los casos de uso del sistema, orquestando los flujos entre los entry points y las entidades.
- **Infrastructure**: Contiene los helpers, driven adapters (conexiones externas como bases de datos, servicios REST, etc.) y entry points (puntos de entrada de la aplicación).
- **Application**: Ensambla los módulos, resuelve dependencias y expone los casos de uso como beans. Aquí se encuentra el método `main` para iniciar la aplicación.

## Como funciona el Proyecto

- **Caso de uso**:
   Consulta de información de un cliente a través de su número de identificación. Este endpoint permite obtener los datos básicos del cliente.

- **Que datos se pueden Consultar**:
   Se puede consultar información como el número de identificación del cliente, dirección y otros datos relevantes asociados al cliente.

## Consulta de Cliente

Esta petición permite consultar la información básica de un cliente usando su número de identificación. El endpoint está diseñado para recibir datos en formato JSON y retorna información relevante del cliente.

### Documentos disponibles para consulta

Los documentos disponibles para la consulta estática de los siguientes clientes, identificados por su número de documento:

| Número de identificación | Nombre  | Dirección | Ciudad    |
|-------------------------|---------|-----------|-----------|
| 12368542                | Jorge   | 1         | Cal       |
| 5462145                 | Jane    | 2         | Medellin  |
| 65841236                | Pablo   | 3         | Bogota    |
| 8459312                 | Maria   | 4         | Pereira   |

Estos son los documentos que se pueden consultar mediante el endpoint de cliente.

### Nota importante

Si se consulta el cliente con número de identificación `8459312`, el sistema retornará una excepción controlada con código 500 (`DEFAULT_EXCEPTION`), indicando un error interno en el servicio.


**¿Cómo se construye la petición?**
- Método: `GET`
- URL: `http://localhost:8080/getClient`
- Headers: Se deben incluir identificadores de relación, creador, código de negocio, content type y un message id.
- Body: Se envía el número de identificación del cliente en el campo correspondiente.


**Ejemplo de headers:**
```http
relations-identifier: RELSDASDASDS
aid-creator: A8A77339260DA412B8238F21BBC1CF398
code: NEG
Content-Type: application/json
message-id: fc17bfa3-a05a-4858-98fd-0df87a6d8a65
```

**Ejemplo de body:**
```json
{
  "data": {
    "identification": {
      "number": "65841236"
    }
  }
}
```

**Ejemplo de Respuesta:**
```json
{
    "meta": {
        "_messageId": "32ed5636-c6d2-4a87-a1ba-af9bb95935fa",
        "_requestDateTime": "03/10/2025 17:03:40:2086"
    },
    "data": {
        "identification": "65841236",
        "clientName": "Pablo",
        "address": "3",
        "city": "Bogota"
    }
}
```

## ¿Cómo funciona ECS en este proyecto?

La Librería de Logging ECS es una biblioteca basada en Java diseñada para generar logs estructurados que cumplen con el formato del Elastic Common Schema (ECS). Soporta los paradigmas de programación imperativa y reactiva, para proyectos Java generados a partir de plantillas (scaffold). La biblioteca facilita la generación de logs con un esquema previamente definido, permitiendo un y análisis de los logs de request/response/error.

## Implementación de librería ECS

### Nota importante

Para este proyecto se dejaron comentarios en las importaciones y configuraciones necesarias para la implementación de la librería ECS, se pueden encontrar fácilmente por `@Comment`.

### Implementación detallada.

Para usar la Librería de Logging ECS en tu proyecto Java, agrega las siguientes dependencias en tu proyecto:


- Se debe Agregar Dependencia, en el `build.gradle` del modulo de aplicación donde se encuentra el Main application como se muestra posteriomente.

**Proyectos reactivos:**
```groovy
dependencies {
    implementation project(':model')
    implementation project(':usecase')
    implementation project(':reactive-web')
    implementation project(':r2dbc-core')
    implementation 'org.springframework.boot:spring-boot-starter'
    runtimeOnly('org.springframework.boot:spring-boot-devtools')
    testImplementation 'com.tngtech.archunit:archunit:1.4.1'
    testImplementation 'com.fasterxml.jackson.core:jackson-databind'

    /**
     * Ejemplo de implementación
     */

    implementation "co.com.bancolombia:ecs-reactive:${ecsversion}"
}

```

- Se debe Agregar Dependencia, en el `main.gradle` de la aplicación como se muestra posteriomente.

**Modelo:**
```groovy
subprojects {
    apply plugin: 'java'
    apply plugin: 'jacoco'
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'info.solidsoft.pitest'

    compileJava.dependsOn validateStructure

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    test {
        useJUnitPlatform()
    }

    dependencies {
        /**
         * Ejemplo de implementación
         **/
        implementation "co.com.bancolombia:ecs-model:${ecsversion}"
    }
}
```

Para evitar el error en la tarea `validate-structure` de scaffold, debes agregar la configuración de `whitelistedDependencies` en el archivo `main.gradle`. Así se indica que la dependencia `ecs-model` está permitida.

El bloque de código quedaría así:

```groovy
cleanPlugin {
    modelProps {
        whitelistedDependencies = "ecs-model"
    }
}
```

Esto asegura que la librería `ecs-model` sea reconocida como permitida durante la validación de estructura.


En la clase de excepción ejemplo `BusinessException` o `AppException` debe extender de la clase `BusinessExceptionECS` del modelo de la librería.
Ejemplo:

![Code](/docs/BusinessException-implementation-ecs.png)


En la clase de constantes de las excepciones ejemplo `ConstantBusinessException` debe implementar la interfaz `ErrorManagement` del modelo de la librería.
Ejemplo:

![Code](/docs/ConstantBusinessException-implementation-ecs.png)

Finalmente Agregar la configuración `@Import(ReactiveLogsConfiguration.class)` en la clase principal del proyecto:

![Code](/docs/ReactiveLogsConfiguration-implementation-mainapplication.png)

