# LivePoints
LivePoint es una aplicación móvil que permite el registro y autenticación de usuarios mediante Firebase, mostrando en tiempo real las ubicaciones de usuarios disponibles y puntos de interés en un mapa interactivo.
## Características Principales
### Registro y Autenticación de Usuarios
Los usuarios pueden registrarse con la siguiente información:
- Nombre
- Apellido
- Email
- Contraseña
- Número de identificación
- Ubicación (latitud y longitud)
- Imagen de perfil en alta resolución
#### Funcionalidades de Firebase:
- **Autenticación**: Se utiliza Firebase Authentication para email y contraseña.
- **Base de Datos**: Firebase Realtime Database almacena los datos secundarios del usuario.
- **Almacenamiento de Imágenes**: Firebase Storage guarda las imágenes de perfil en alta resolución.
### Mapa Principal con Localización Actual y Puntos de Interés
Tras autenticarse, el usuario accede a la pantalla principal, que muestra:
- Un mapa con las 5 localizaciones de puntos de interés obtenidas de un archivo JSON.
- Un marcador que señala la ubicación actual del usuario.
### Opciones de Menú
- **Cerrar Sesión**: Permite al usuario desconectarse de la aplicación.
- **Estado de Disponibilidad**: Permite al usuario marcarse como "Disponible" o "No Disponible".
- **Lista de Usuarios Disponibles**: Muestra una lista de usuarios activos, junto a su imagen, nombre y un botón para ver su ubicación en tiempo real.
### Seguimiento en Tiempo Real de Usuarios Disponibles
- La lista de usuarios disponibles muestra cada usuario con un botón que permite visualizar su ubicación actual en el mapa.
- La posición del usuario rastreado se actualiza en tiempo real si se desplaza, junto con la distancia en línea recta desde el usuario que hace el seguimiento.
### Servicio de Monitoreo de Estado
- Un servicio monitorea cambios en la lista de usuarios disponibles.
- Cuando un usuario cambia su estado a "Disponible", aparece en la lista y se muestra un Toast con su nombre.
- Al pasar a "No Disponible", el usuario desaparece de la lista.
## Requisitos Previos
- **Firebase**: Firebase Auth, Firebase Realtime Database, y Firebase Storage configurados en tu proyecto de Firebase.
- **Android Studio**: Android Studio y Android Koala configurados con las librerías necesarias para mapas y Firebase.
- **Archivo JSON**: Archivo de puntos de interés en `BrightSpace -> Clases -> Clase 10`.
## Instalación
1. Clona este repositorio en tu entorno local.
2. Configura Firebase en tu proyecto, agregando el archivo `google-services.json`.
3. Añade las dependencias necesarias para Firebase Auth, Realtime Database y Storage.
4. Configura el archivo JSON con las ubicaciones de puntos de interés en la estructura correcta.
## Uso
1. Regístrate con un nuevo usuario o inicia sesión si ya tienes una cuenta.
2. Accede a la pantalla principal para ver el mapa con tus puntos de interés y tu ubicación actual.
3. Cambia tu estado de disponibilidad desde el menú.
4. Consulta la lista de usuarios disponibles y accede a la ubicación en tiempo real de cualquiera de ellos.
5. Observa cambios en la disponibilidad de usuarios con las notificaciones Toast.
## Licencia
Este proyecto está bajo la licencia MIT. Consulta el archivo `LICENSE` para más detalles.
## Equipo de Desarrollo - Torino FC
- **Sebastián Nariño**: Apasionado por la tecnología y el desarrollo de software, con experiencia en automatización y manejo de bases de datos. Interesado en SaaS y emprendimiento.
- **Gustavo Parra**: Entusiasta de la programación y la innovación tecnológica, siempre buscando mejorar y aprender en el campo de la tecnología.
- **Juan David Torres**: Desarrollador con experiencia en automatización, manejo de bases de datos, y tutor privado de programación. Siempre en busca de nuevas formas de mejorar y aprender.
