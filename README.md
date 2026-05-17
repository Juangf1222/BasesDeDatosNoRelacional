# Gestor de Base de Datos No Relacional en Java

Proyecto desarrollado en Java que implementa un gestor básico de base de datos no relacional utilizando estructuras de datos AVL y persistencia en archivos JSON.

---

# Descripción

El sistema permite administrar colecciones de documentos similares al funcionamiento básico de bases de datos NoSQL como MongoDB.

Cada colección se almacena como un archivo `.json` dentro de la carpeta `data/`.

Los documentos son almacenados en memoria utilizando un Árbol AVL autobalanceado para garantizar operaciones eficientes de búsqueda, inserción y eliminación.

---

# Características

- Inserción de documentos
- Búsqueda por ID
- Búsqueda por criterio/campo
- Actualización completa de documentos
- Actualización de campos específicos
- Eliminación de documentos
- Gestión de colecciones
- Persistencia automática en JSON
- Árbol AVL autobalanceado
- Interfaz de consola interactiva

---

# Tecnologías utilizadas

- Java SE
- Maven
- JSON
- Árbol AVL
- Programación Orientada a Objetos

---

# Estructura del proyecto
```plaintext
gestorbd/
│
├── data/
│   └── colecciones JSON
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── gestorbd/
│
│                   ├── App.java
│
│                   ├── consola/
│                   │   └── MenuConsola.java
│
│                   ├── estructura/
│                   │   ├── ArbolAVL.java
│                   │   └── NodoAVL.java
│
│                   ├── modelo/
│                   │   └── Documento.java
│
│                   ├── servicio/
│                   │   └── GestorBaseDatos.java
│
│                   └── util/
│                       └── JsonUtil.java
│
└── pom.xml
```
---
# Requisitos

- Java 11 o superior
- Maven

# Verificar instalación:

java -version
mvn -version
Configuración del proyecto

# 1. Clonar o descargar el proyecto

git clone https://github.com/Juangf1222/BasesDeDatosNoRelacional o descargar el archivo ZIP.

# 2. Abrir el proyecto

Abrir la carpeta gestorbd en:

IntelliJ IDEA, VS Code, Eclipse, NetBeans

# 3. Compilar el proyecto

mvn clean compile

# Ejecutar con Maven:

mvn exec:java "-Dexec.mainClass=com.gestorbd.App"

# Uso del sistema

Al iniciar, el programa solicitará una colección.

Ejemplo:

Ingrese el nombre de la colección a usar:

Si la colección no existe, se creará automáticamente.

Insertar un documento

Ejemplo:

ID del documento: 1

Nombre del campo: nombre
Valor del campo: Juan

Nombre del campo: edad
Valor del campo: 20

Resultado almacenado:

{
  "id": "1",
  "nombre": "Juan",
  "edad": 20
}

# Persistencia de datos

Los documentos se almacenan automáticamente en archivos JSON dentro de la carpeta:

data/

Ejemplo:

data/usuarios.json

# Ejemplo de colección JSON
[
  {
    "id": "1",
    "nombre": "Juan",
    "edad": 20
  },
  {
    "id": "2",
    "nombre": "Maria",
    "edad": 25
  }
]
