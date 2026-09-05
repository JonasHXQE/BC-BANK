# BC-BANK: Estructura Completa de Firestore, Reglas de Seguridad y Arquitectura del Panel Admin

Este documento detalla la arquitectura de base de datos en **Cloud Firestore** y **Realtime Database**, las **Reglas de Seguridad** para proteger la aplicación móvil y la **Guía Maestra para la Implementación del Panel Web Administrativo** utilizando el **Firebase Admin SDK**.

---

## 1. Filosofía de Seguridad y Arquitectura General

```
┌────────────────────────────────────────────────────────────────────────┐
│                        ARQUITECTURA DE SEGURIDAD                       │
├──────────────────────────────────┬─────────────────────────────────────┤
│        APP MÓVIL ANDROID         │          PANEL WEB ADMIN            │
│       (Firebase Client SDK)      │       (Firebase Admin SDK)          │
├──────────────────────────────────┼─────────────────────────────────────┤
│ • Sujeto a `firestore.rules`     │ • Se ejecuta en Servidor/Node.js    │
│ • Colección `panel_admin_users`  │ • EVADE todas las reglas cliente    │
│   es FANTASMA (acceso 100%       │ • Acceso con Service Account Key    │
│   DENEGADO para la app móvil)    │ • Autenticación independiente con   │
│ • Solo puede editar sus propios  │   roles: `admin`, `superadmin`,     │
│   datos de cuenta y transacciones│   y `general`                       │
└──────────────────────────────────┴─────────────────────────────────────┘
```

### Principio de Aislamiento Estricto:
1. **App Móvil (Cliente):** Utiliza las credenciales de `google-services.json` y el SDK móvil. Todas sus peticiones pasan por `firestore.rules`.
2. **Panel Web Admin (Servidor):** Debe desarrollarse en un entorno de backend seguro (Next.js con API Routes, Node.js + Express, NestJS o Fastify) usando `firebase-admin`. **Nunca** debe usar el SDK de cliente con API keys públicas. Al usar el Admin SDK, el panel posee privilegios de superusuario en el servidor, mientras que las reglas de Firestore mantienen a los usuarios móviles estrictamente confinados.
3. **Colección Fantasma (`panel_admin_users`):** En `firestore.rules`, la regla para esta colección es:
   ```javascript
   match /panel_admin_users/{adminId} {
     allow read, write: if false;
   }
   ```
   Esto hace que ningún usuario ni atacante pueda consultar si existen administradores, ni leer sus contraseñas ni enviar solicitudes de registro.

---

## 2. Sistema de Roles y Permisos (RBAC) del Panel Admin

El panel **NO** utiliza Firebase Authentication común para administradores; utiliza directamente la colección privada `/panel_admin_users` para autenticar y autorizar a su personal.

### Matriz de Acceso por Rol:

| Módulo / Función | Rol: `admin` | Rol: `superadmin` | Rol: `general` (Root) |
|---|:---:|:---:|:---:|
| **Dashboard y Métricas** | ✅ Acceso total | ✅ Acceso total | ✅ Acceso total |
| **Gestión de Cuentas y Saldos** | ✅ Operativo | ✅ Operativo | ✅ Control Absoluto |
| **Gestión de Depósitos y Retiros** | ✅ Operativo | ✅ Operativo | ✅ Control Absoluto |
| **Catálogo de Servicios y Pagos** | ✅ Operativo | ✅ Operativo | ✅ Control Absoluto |
| **Notificaciones Push Globales** | ✅ Envío | ✅ Envío | ✅ Control Absoluto |
| **Transacciones y Auditoría** | ✅ Consulta | ✅ Consulta | ✅ Consulta y Anulaciones |
| **Soporte y Canales Oficiales** | ✅ Edición | ✅ Edición | ✅ Control Absoluto |
| **Verificador de Comprobantes** | ✅ Verificación | ✅ Verificación | ✅ Control Absoluto |
| **Gate / Modo Mantenimiento** | ❌ Solo lectura | ✅ Modificación | ✅ Control Absoluto |
| **Acceso a Gestión de Admins** | ❌ **PROHIBIDO** | ✅ Permitido (limitado) | ✅ **Acceso Total** |
| **Crear rol `admin`** | ❌ Prohibido | ✅ **Sí puede** | ✅ **Sí puede** |
| **Crear rol `superadmin`** | ❌ Prohibido | ❌ **Prohibido** | ✅ **Sí puede** |
| **Crear rol `general`** | ❌ Prohibido | ❌ **Prohibido** | ✅ **Sí puede** |
| **Editar usuarios `admin`** | ❌ Prohibido | ✅ **Sí puede** | ✅ **Sí puede** |
| **Editar `superadmin` y `general`** | ❌ Prohibido | ❌ **Solo Lectura** | ✅ **Sí puede** |
| **Eliminar usuarios `admin`** | ❌ Prohibido | ✅ **Sí puede** | ✅ **Sí puede** |
| **Eliminar `superadmin` o `general`** | ❌ Prohibido | ❌ **Prohibido** | ✅ **Sí puede** |
| **VISUALIZAR CONTRASEÑAS** | ❌ **Oculto** | ❌ **Oculto** | ✅ **ÚNICO rol que puede ver contraseñas de todos** |

---

## 3. Paso a Paso: Crear la Colección y el Primer Usuario `general`

> ⚠️ **REGLA DE SEGURIDAD OBLIGATORIA:**
> El código del backend o de la app **NUNCA debe auto-crear usuarios o colecciones admin por defecto** si no existen. Crear credenciales por defecto (como `admin/admin123`) en el código es una vulnerabilidad crítica. El primer usuario `general` debe ser dado de alta **manualmente en Firebase Console**.

### Paso 1: Ingresar a Firestore Database
1. Abre [Firebase Console](https://console.firebase.google.com/) y entra al proyecto **`blackcore-bank`**.
2. En el menú lateral izquierdo, haz clic en **Compilación** > **Firestore Database**.

### Paso 2: Crear la Colección Privada
1. En la pestaña **Datos**, haz clic en **+ Iniciar colección**.
2. **ID de colección:** Escribe exactamente:
   ```text
   panel_admin_users
   ```
3. Haz clic en **Siguiente**.

### Paso 3: Crear el Primer Documento (`general`)
1. **ID de documento:** Puedes usar un identificador seguro o autogenerado, por ejemplo:
   ```text
   admin_general_root
   ```
2. Agrega los siguientes campos con sus tipos respectivos:

| Campo | Tipo | Valor de Ejemplo | Descripción |
|---|---|---|---|
| `id` | `string` | `admin_general_root` | Identificador único |
| `username` | `string` | `master_general` | Usuario de login |
| `email` | `string` | `director@bcbank.com` | Correo institucional |
| `fullName` | `string` | `Superintendente General BC-BANK` | Nombre del titular |
| `role` | `string` | `general` | **Opciones:** `admin`, `superadmin`, `general` |
| `password` | `string` | `$2b$12$eX4mP1eH4sH...` o texto cifrado | Contraseña o Hash seguro |
| `status` | `string` | `ACTIVE` | `ACTIVE`, `SUSPENDED`, `BLOCKED` |
| `createdAt` | `number` | `1772750000000` | Timestamp milisegundos |
| `updatedAt` | `number` | `1772750000000` | Timestamp milisegundos |
| `createdBy` | `string` | `MANUAL_CONSOLE_PROVISION` | Auditoría de creación |
| `failedAttempts` | `number` | `0` | Contador de bloqueos |

3. Haz clic en **Guardar**.

---

## 4. Estructura Exhaustiva de Firestore (De Canto a Canto)

A continuación se detalla la totalidad de colecciones utilizadas en **BC-BANK**:

### 4.1. Colección `/users/{uid}`
Representa los perfiles de los clientes del banco.
- **Campos:**
  - `uid` (`string`): UID de Firebase Auth.
  - `fullName` (`string`): Nombre completo del cliente.
  - `email` (`string`): Correo registrado.
  - `phone` (`string`): Teléfono de 9 dígitos.
  - `dni` (`string`): DNI de 8 dígitos.
  - `accountNumber` (`string`): Número de cuenta interna (ej: `191-45892147-0-12`).
  - `cciNumber` (`string`): Código de Cuenta Interbancario (20 dígitos).
  - `cardLastFour` (`string`): Últimos 4 dígitos de la tarjeta virtual.
  - `accountStatus` (`string`): `"ACTIVE"`, `"FROZEN"`, `"BLOCKED"`, `"UNDER_REVIEW"`.
  - `isFrozen` (`boolean`): Si la cuenta está temporalmente congelada por prevención.
  - `statusReason` (`string`): Motivo en caso de congelamiento o bloqueo.
  - `userTier` (`string`): `"STANDARD"`, `"PREMIUM"`, `"VIP"`.
  - `dailyTransferLimit` (`number`): Límite diario de transferencias (ej: `5000.0`).
  - `dailyWithdrawalLimit` (`number`): Límite diario de retiros en cajero/agente (ej: `2500.0`).
  - `fcmToken` (`string`): Token de Firebase Cloud Messaging para notificaciones push.
  - `createdAt` / `updatedAt` (`number`): Timestamps.

#### Subcolecciones de `/users/{uid}`:
1. **`/users/{uid}/account/main` (Documento único):**
   - `balance` (`number`): Saldo disponible en soles (S/.). **Regla de Seguridad Bancaria:** Toda cuenta nueva se crea estrictamente con saldo `0.00`. Los clientes móviles tienen prohibido por `firestore.rules` aumentarse el saldo. La acreditación o recarga de fondos se realiza de forma exclusiva desde el Panel Admin utilizando el Firebase Admin SDK.
   - `accountHolder` (`string`): Nombre del titular.
   - `accountNumber` (`string`): Número de cuenta.
   - `cciNumber` (`string`): CCI.
   - `cardLastFour` (`string`): Últimos 4 dígitos.
   - `lastUpdated` (`number`): Timestamp de última sincronización.
2. **`/users/{uid}/transactions/{txId}`:**
   - `id` (`number` o `string`): ID de transacción.
   - `title` (`string`): Concepto (ej: `"Transferencia a Juan Perez"`).
   - `amount` (`number`): Monto en soles.
   - `type` (`string`): `"INCOME"`, `"EXPENSE"`, `"GOAL_DEPOSIT"`.
   - `category` (`string`): `"Alimentación"`, `"Servicios"`, `"Transporte"`, `"Transferencia"`, etc.
   - `timestamp` (`number`): Fecha y hora.
   - `recipientOrSender` (`string`): DNI, teléfono o nombre del tercero.
   - `referenceNumber` (`string`): Número de operación único de 8-12 dígitos.
   - `note` (`string`): Mensaje opcional.
3. **`/users/{uid}/savings_goals/{goalId}`:**
   - `id` (`number`): ID de la meta.
   - `name` (`string`): Nombre (ej: `"Viaje a Cusco"`).
   - `targetAmount` (`number`): Monto objetivo.
   - `currentAmount` (`number`): Monto ahorrado.
   - `categoryIcon` (`string`): `"TRAVEL"`, `"EMERGENCY"`, `"TECH"`, `"HOME"`, `"CAR"`, `"SAVINGS"`.
   - `status` (`string`): `"IN_PROGRESS"`, `"COMPLETED"`, `"WITHDRAWN"`.
4. **`/users/{uid}/budgets/{budgetId}`:**
   - `id` (`number`): ID del presupuesto.
   - `category` (`string`): Categoría asignada.
   - `monthlyLimit` (`number`): Tope máximo mensual.
   - `spentAmount` (`number`): Gasto acumulado en el mes.
5. **`/users/{uid}/notifications/{notifId}`:**
   - `id` (`number`): ID de la notificación.
   - `title` (`string`): Título de la notificación.
   - `message` (`string`): Contenido detallado de la operación bancaria o aviso de seguridad.
   - `category` (`string`): `"Transacciones"`, `"Retiros"`, `"Seguridad"`, `"Servicios"`, `"Depósitos"`.
   - `type` (`string`): `"TRANSFER_RECEIVED"`, `"WITHDRAWAL_COMPLETED"`, `"SECURITY"`, etc.
   - `status` (`string`): Estado de la notificación:
     * `"UNSEEN"`: Notificación pendiente de lectura (emite alerta emergente y badge de no leído).
     * `"SEEN"`: Notificación visualizada/leída por el usuario. Se sincroniza entre dispositivos de modo que si se marca como vista, en otra sesión o dispositivo ya no volverá a aparecer como no vista ni emitirá alertas repetidas.
     * `"DELETED"`: Notificación eliminada por el usuario. Permanece con este flag (o se remueve) para que jamás vuelva a mostrarse en ningún dispositivo o sesión futura.
   - `isRead` (`boolean`): Compatibilidad de lectura (`true` si `status == "SEEN"`).
   - `deleted` (`boolean`): Compatibilidad de eliminación (`true` si `status == "DELETED"`).
   - `timestamp` (`number`): Milisegundos epoch de creación.
   - `amountTag` (`string`, opcional): Etiqueta formateada (ej: `"+S/ 150.00"`).
6. **`/users/{uid}/categories/{catId}`:**
   - **Regla Estricta:** Cero categorías precargadas por defecto. La app solo carga y muestra las categorías que el usuario haya registrado expresamente desde su perfil en Firestore (`type`: `"EXPENSE"`, `"INCOME"`, `"GOAL"`, `"BUDGET"`).
   - `id` (`string`): Identificador único UUID de la categoría.
   - `name` (`string`): Nombre asignado por el usuario.
   - `type` (`string`): `"EXPENSE"`, `"INCOME"`, `"GOAL"`, o `"BUDGET"`.
   - `iconKey` (`string`): Identificador visual de icono.
   - `createdAt` (`number`): Timestamp de registro.
7. **`/users/{uid}/service_payments/{paymentId}`:**
   - Historial individual de pagos de servicios.
8. **`/users/{uid}/security_audit_logs/{logId}`:**
   - Registros de inicios de sesión, cambios de PIN y dispositivo del cliente.

---

### 4.2. Colección `/identities/{dni}`
- **Propósito:** Registro centralizado para garantizar unicidad de DNI y teléfono, evitando cuentas duplicadas o usurpaciones.
- **Campos:**
  - `dni` (`string`): Documento Nacional de Identidad.
  - `uid` (`string`): UID asociado en Firebase Auth.
  - `phone` (`string`): Teléfono móvil vinculado.
  - `email` (`string`): Correo del titular.
  - `createdAt` (`number`): Fecha de registro.

---

### 4.3. Colección `/security_pins/{uid}`
- **Propósito:** Verificación de PIN transaccional de 6 dígitos.
- **Campos:**
  - `uid` (`string`): UID del usuario.
  - `pinHash` (`string`): Hash SHA-256 con salt del PIN.
  - `salt` (`string`): Salting criptográfico.
  - `failedAttempts` (`number`): Intentos fallidos consecutivos.
  - `isLocked` (`boolean`): Si el PIN está bloqueado temporalmente por intentos fallidos.
  - `updatedAt` (`number`): Fecha de última modificación.

---

### 4.4. Colección `/withdrawals/{withdrawalId}`
- **Propósito:** Gestión de retiros sin tarjeta (Retiro por código / QR en agentes y cajeros automáticos).
- **Campos:**
  - `id` (`string`): ID del retiro.
  - `uid` (`string`): UID del solicitante.
  - `userName` (`string`): Nombre del solicitante.
  - `userPhone` (`string`): Teléfono de contacto.
  - `amount` (`number`): Monto a retirar en soles.
  - `opCode` (`string`): Clave de retiro de 6 dígitos.
  - `pinCode` (`string`): PIN temporal de 4 dígitos.
  - `status` (`string`): `"PENDING"`, `"COMPLETED"`, `"CANCELLED"`, `"EXPIRED"`.
  - `createdAt` (`number`): Timestamp de solicitud.
  - `expiresAt` (`number`): Timestamp de expiración (típicamente 60 a 120 minutos).
  - `completedAt` (`number`, opcional): Timestamp en que se efectuó el retiro en agente/cajero.
  - `cashierOrAtmId` (`string`, opcional): Identificador del agente o cajero que liquidó el efectivo.

---

### 4.5. Colección `/cip_codes/{cipCode}`
- **Propósito:** Códigos CIP (Código de Identificación de Pago) para depósitos y recargas por banca por internet o agentes.
- **Campos:**
  - `cipCode` (`string`): Código CIP de 8 dígitos (ej: `"45892147"`).
  - `uid` (`string`): UID de la cuenta beneficiaria.
  - `accountNumber` (`string`): Número de cuenta donde se acreditará el dinero.
  - `accountHolder` (`string`): Nombre del titular.
  - `dni` (`string`): DNI del titular.
  - `phone` (`string`): Teléfono.
  - `status` (`string`): `"ACTIVE"`, `"PAID"`, `"EXPIRED"`.
  - `createdAt` / `expiresAt` (`number`): Fechas de vigencia.

---

### 4.6. Colección `/services/{serviceId}`
- **Propósito:** Catálogo de empresas y servicios públicos (Luz, Agua, Telefonía, Internet, Universidades).
- **Campos:**
  - `id` (`string`): Identificador único (ej: `"sedapal"`, `"luz_del_sur"`, `"movistar"`).
  - `name` (`string`): Nombre público del servicio (ej: `"Luz del Sur"`).
  - `category` (`string`): `"LUZ"`, `"AGUA"`, `"TELEFONIA"`, `"INTERNET"`, `"EDUCACION"`, `"FINANZAS"`.
  - `code` (`string`): Código empresarial de facturación.
  - `iconUrl` (`string`): URL de icono/logotipo.
  - `enabled` (`boolean`): Activo/Inactivo en la aplicación.
  - `description` (`string`): Instrucciones para el cliente (ej: `"Ingresa tu número de suministro de 7 dígitos"`).
  - `fieldsRequired` (`array` de `string`): Campos que debe ingresar el usuario (ej: `["supplyNumber"]`).
  - `minAmount` (`number`): Monto mínimo de pago.
  - `maxAmount` (`number`): Monto máximo de pago.

---

### 4.7. Colección `/service_payments/{paymentId}`
- **Propósito:** Historial global consolidado de pagos de servicios para conciliación financiera en el Panel Admin.
- **Campos:**
  - `id` (`string`): ID único de pago.
  - `serviceId` (`string`): ID del servicio pagado.
  - `serviceName` (`string`): Nombre de la empresa.
  - `uid` (`string`): UID del pagador.
  - `userDni` (`string`): DNI del pagador.
  - `serviceCode` (`string`): Código de cliente/suministro pagado.
  - `amount` (`number`): Monto del recibo.
  - `commission` (`number`): Comisión bancaria cobrada (ej: S/ 1.50).
  - `totalPaid` (`number`): Total debitado (`amount + commission`).
  - `timestamp` (`number`): Fecha y hora del pago.
  - `referenceCode` (`string`): Número de operación bancaria.
  - `status` (`string`): `"CONFIRMED"`, `"REFUNDED"`.

---

### 4.8. Colección `/support_channels/{channelId}`
- **Propósito:** Directorio de canales oficiales de atención al cliente gestionables desde el panel en tiempo real.
- **Campos:**
  - `id` (`string`): ID del canal (ej: `"whatsapp_oficial"`).
  - `name` (`string`): Título visible (ej: `"WhatsApp Oficial 24/7"`).
  - `type` (`string`): `"WHATSAPP"`, `"PHONE"`, `"EMAIL"`, `"TELEGRAM"`.
  - `contactValue` (`string`): Número telefónico (ej: `"+51987654321"`) o correo electrónico.
  - `availableHours` (`string`): Horario de atención (ej: `"Lunes a Domingo, 24 horas"`).
  - `isActive` (`boolean`): Si debe mostrarse en la app.
  - `priorityOrder` (`number`): Orden de aparición en la lista.
  - `description` (`string`): Subtítulo explicativo.

---

### 4.9. Colección `/vouchers/{serial}`
- **Propósito:** Comprobantes oficiales digitales emitidos. Permite a cualquier usuario o comercio escanear el QR del comprobante y verificar si es genuino o falso.
- **Campos:**
  - `serial` (`string`): Código serial alfanumérico único (ej: `"BC-2026-981245"`).
  - `hash` (`string`): Hash criptográfico de verificación.
  - `operationId` (`string`): ID de la transacción asociada.
  - `type` (`string`): `"TRANSFER"`, `"PAYMENT"`, `"WITHDRAWAL"`, `"DEPOSIT"`.
  - `amount` (`number`): Importe operado.
  - `sender` (`string`): Nombre del emisor.
  - `recipient` (`string`): Nombre del receptor o empresa.
  - `timestamp` (`number`): Fecha de emisión.
  - `isValid` (`boolean`): Si el comprobante es auténtico y válido.
  - `verifiedCount` (`number`): Cantidad de veces que ha sido escaneado/verificado.

---

### 4.10. Colección `/app_config/global` (Documento único)
- **Propósito:** Sistema "Gate" y control global de la aplicación móvil.
- **Campos:**
  - `isMaintenanceActive` (`boolean`): Si se activa, bloquea el acceso general mostrando la pantalla de mantenimiento.
  - `maintenanceMessage` (`string`): Mensaje explicativo para los usuarios.
  - `minSupportedVersion` (`string`): Versión mínima requerida (ej: `"1.0.0"`).
  - `latestVersion` (`string`): Última versión publicada.
  - `forcedUpdate` (`boolean`): Obliga al usuario a actualizar si su versión es inferior.
  - `allowTransfers` (`boolean`): Interruptor de emergencia para pausar transferencias.
  - `allowWithdrawals` (`boolean`): Interruptor para pausar retiros sin tarjeta.
  - `allowDeposits` (`boolean`): Interruptor para pausar depósitos.
  - `allowServicePayments` (`boolean`): Interruptor para pausar pago de servicios.
  - `securityBannerText` (`string`, opcional): Texto de alerta mostrado en la cabecera del Dashboard móvil.

---

### 4.11. Colección `/blocked_devices/{deviceId}`
- **Propósito:** Bloqueo de terminales móviles sospechosos o reportados por robo/fraude.
- **Campos:**
  - `deviceId` (`string`): Identificador único de hardware/Android ID.
  - `model` (`string`): Modelo del teléfono (ej: `"Samsung SM-G998B"`).
  - `reason` (`string`): Motivo del veto (ej: `"Detección de intentos continuos de inyección / fraude"`).
  - `blockedAt` (`number`): Timestamp.
  - `blockedBy` (`string`): Usuario admin que ordenó el bloqueo.
  - `isActive` (`boolean`): Si el bloqueo está vigente.

---

### 4.12. Colección `/global_announcements/{announcementId}`
- **Propósito:** Banners y comunicados push/in-app para todos los clientes de BC-BANK.
- **Campos:**
  - `id` (`string`): ID del comunicado.
  - `title` (`string`): Título (ej: `"¡Mantenimiento programado hoy a las 23:00!"`).
  - `message` (`string`): Cuerpo del mensaje.
  - `type` (`string`): `"INFO"`, `"WARNING"`, `"ALERT"`, `"PROMO"`.
  - `isActive` (`boolean`): Mostrar u ocultar.
  - `createdAt` / `expiresAt` (`number`): Rango de fechas activo.
  - `actionUrl` (`string`, opcional): Enlace web o acción interna.

---

### 4.13. Colección `/security_logs_by_date/{dateDay}/events/{logId}`
- **Propósito:** Registro diario de eventos de auditoría y ciberseguridad.
- **Campos:**
  - `uid` (`string`): UID del cliente involucrado.
  - `email` (`string`): Correo del cliente.
  - `eventType` (`string`): `"LOGIN_SUCCESS"`, `"LOGIN_FAILED"`, `"PIN_CHANGE"`, `"TRANSFER_HIGH_VALUE"`.
  - `ipAddress` (`string`): Dirección IP de la petición.
  - `deviceModel` (`string`): Modelo del terminal.
  - `networkType` (`string`): `"WIFI"`, `"CELLULAR_5G"`, `"VPN"`.
  - `timestamp` (`number`): Timestamp exacto.
  - `status` (`string`): `"OK"`, `"SUSPICIOUS"`, `"DENIED"`.

---

## 5. Pautas para la Implementación del Panel Web Admin

### 5.1. Stack Técnico Recomendado
- **Frontend:** Next.js 14+ (App Router) con TypeScript y Tailwind CSS.
- **Backend / Rutas de API:** Next.js Server Actions o Route Handlers (`/api/admin/...`) ejecutándose en entorno Node.js con `firebase-admin`.
- **Librería de Componentes:** Lucide React para iconografía bancaria y Tailwind CSS para diseño moderno.

### 5.2. Inicialización de Firebase Admin SDK (Backend)

```typescript
// lib/firebaseAdmin.ts
import * as admin from 'firebase-admin';

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
    }),
    databaseURL: "https://blackcore-bank-default-rtdb.firebaseio.com"
  });
}

export const adminDb = admin.firestore();
export const adminMessaging = admin.messaging();
```

### 5.3. Implementación del Login y Verificación de Contraseñas del Panel Admin

```typescript
// app/api/admin/auth/login/route.ts
import { NextResponse } from 'next/server';
import { adminDb } from '@/lib/firebaseAdmin';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';

export async function POST(req: Request) {
  const { username, password } = await req.json();

  if (!username || !password) {
    return NextResponse.json({ error: 'Credenciales incompletas' }, { status: 400 });
  }

  // Consulta en la colección fantasma usando Admin SDK
  const snapshot = await adminDb
    .collection('panel_admin_users')
    .where('username', '==', username.trim())
    .limit(1)
    .get();

  if (snapshot.empty) {
    return NextResponse.json({ error: 'Usuario no encontrado' }, { status: 401 });
  }

  const userDoc = snapshot.docs[0];
  const userData = userDoc.data();

  if (userData.status !== 'ACTIVE') {
    return NextResponse.json({ error: 'Esta cuenta administrativa se encuentra suspendida' }, { status: 403 });
  }

  // Verificación de contraseña (bcrypt o texto cifrado)
  const isMatch = await bcrypt.compare(password, userData.password);
  if (!isMatch) {
    return NextResponse.json({ error: 'Contraseña incorrecta' }, { status: 401 });
  }

  // Generación de JWT de sesión con el rol asignado
  const token = jwt.sign(
    {
      adminId: userDoc.id,
      username: userData.username,
      role: userData.role, // 'admin', 'superadmin', 'general'
      fullName: userData.fullName
    },
    process.env.ADMIN_JWT_SECRET!,
    { expiresIn: '8h' }
  );

  return NextResponse.json({
    success: true,
    token,
    user: {
      id: userDoc.id,
      username: userData.username,
      fullName: userData.fullName,
      role: userData.role
    }
  });
}
```

### 5.4. Lógica de Negocio para la "Gestión de Usuarios del Panel Admin"

En el endpoint `GET /api/admin/users`:
```typescript
// El rol 'general' puede ver las contraseñas.
// Los roles 'admin' y 'superadmin' NUNCA reciben las contraseñas en el response.
export async function GET(req: Request) {
  const session = verifyAdminSession(req);
  if (!session) return unauthorized();

  // El rol 'admin' no tiene acceso a esta sección
  if (session.role === 'admin') {
    return NextResponse.json({ error: 'Acceso denegado a este módulo' }, { status: 403 });
  }

  const snapshot = await adminDb.collection('panel_admin_users').get();
  const users = snapshot.docs.map(doc => {
    const data = doc.data();
    return {
      id: doc.id,
      username: data.username,
      fullName: data.fullName,
      email: data.email,
      role: data.role,
      status: data.status,
      createdAt: data.createdAt,
      // Solo el rol general recibe el campo de contraseña
      password: session.role === 'general' ? data.password : '••••••••',
      canEdit: session.role === 'general' || (session.role === 'superadmin' && data.role === 'admin')
    };
  });

  return NextResponse.json({ users });
}
```

En el endpoint `POST /api/admin/users` (Creación de nuevos usuarios):
```typescript
export async function POST(req: Request) {
  const session = verifyAdminSession(req);
  if (!session) return unauthorized();

  const { username, password, fullName, email, targetRole } = await req.json();

  // Regla 1: 'admin' no puede crear a nadie
  if (session.role === 'admin') {
    return NextResponse.json({ error: 'No tienes permisos para crear usuarios' }, { status: 403 });
  }

  // Regla 2: 'superadmin' SOLO puede crear usuarios con rol 'admin'
  if (session.role === 'superadmin' && targetRole !== 'admin') {
    return NextResponse.json({
      error: 'Un Superadmin únicamente tiene autorización para crear usuarios con rol Admin'
    }, { status: 403 });
  }

  // Regla 3: 'general' puede crear cualquier rol ('admin', 'superadmin', 'general')

  const hashedPassword = await bcrypt.hash(password, 12);

  const newDocRef = adminDb.collection('panel_admin_users').doc();
  await newDocRef.set({
    id: newDocRef.id,
    username,
    password: hashedPassword,
    fullName,
    email,
    role: targetRole,
    status: 'ACTIVE',
    createdAt: Date.now(),
    updatedAt: Date.now(),
    createdBy: session.username
  });

  return NextResponse.json({ success: true, id: newDocRef.id });
}
```

---

## 6. Módulos Operativos del Panel Admin

1. **Gestión de Cuentas y Clientes:**
   - Visualización de clientes con búsqueda por DNI, Nombre, Teléfono o Número de Cuenta.
   - Acciones: Congelar Cuenta (`isFrozen = true`), Descongelar, Editar Límites Diarios de Transferencia y Retiro, Visualizar Saldo y Ajustar Saldo con Nota Contable de Auditoría.
2. **Servicios Públicos:**
   - CRUD de empresas (`/services`): Crear nueva empresa de servicios, subir logotipo, activar/pausar servicio y definir rango de montos mínimos y comisiones.
3. **Depósitos y Recargas:**
   - Listado de solicitudes de recarga y códigos CIP activos (`/cip_codes`).
   - Botón de conciliación manual para acreditar fondos a una cuenta tras validar la transferencia externa.
4. **Retiros sin Tarjeta:**
   - Monitor en vivo de retiros pendientes (`/withdrawals`).
   - Validación de códigos OTP/PIN para procesar o anular retiros a solicitud del usuario o por sospecha de fraude.
5. **Mensajes Push Globales:**
   - Formulario para redactar título y cuerpo de notificación.
   - Envío masivo mediante `adminMessaging.sendEachForMulticast()` a todos los tokens FCM registrados o envío por Topics (ej: topic `global_notifications`).
6. **Transacciones Bancarias:**
   - Búsqueda y filtrado exhaustivo de movimientos por rango de fechas, montos y categorías.
   - Exportación a formato CSV / Excel para auditoría contable.
7. **Canales de Soporte Oficial:**
   - Actualización instantánea de los enlaces de WhatsApp, números de central telefónica y horarios de atención (`/support_channels`).
8. **Verificador de Comprobantes:**
   - Interfaz para buscar por número de serie o pegar el hash de un voucher emitido.
   - Verifica en tiempo real si el comprobante fue generado legítimamente por la app o si se trata de un voucher adulterado.
9. **Gate y Modo Mantenimiento:**
   - Interruptores en tiempo real para `/app_config/global`: Activar/Desactivar Mantenimiento Global, Pausar Transferencias, Pausar Retiros, Bloqueo de Versiones antiguas de la APK.
10. **Terminales y Dispositivos Bloqueados:**
    - Listado de `/blocked_devices`. Posibilidad de agregar un Android ID a la lista negra con un solo clic.

---

## 7. Diseño, Tema y Experiencia de Usuario (UI/UX)

Para mantener una identidad visual coherente con **BC-BANK**, el Panel Web Admin debe utilizar la siguiente paleta de diseño:

### Paleta de Colores Oficial:
- **Canvas / Fondo Principal:** `#0B141B` (Negro Pizarra Profundo / Dark Slate)
- **Superficie de Tarjetas y Paneles:** `#121E28` (Azul Noche Financiero)
- **Bordes y Divisores:** `#1E2D3D` (Gris Pizarra Sutil)
- **Color Primario / Marca:** `#00A86B` (Verde Esmeralda Financiero)
- **Verde Acento / Éxito:** `#10B981` (Emerald Accent)
- **Texto Principal:** `#F8FAFC` (Blanco Puro de Alto Contraste)
- **Texto Secundario:** `#94A3B8` (Gris Neutro Suave)
- **Peligro / Alerta:** `#EF4444` (Rojo Carmesí)
- **Advertencia:** `#F59E0B` (Ámbar)

### Requisitos de Responsividad:
- **Desktop (1024px+):** Sidebar lateral fijo con menú colapsable, tablas de datos densas con paginación y tarjetas de KPIs en cuadrícula de 4 columnas.
- **Tablet / Móvil (< 1024px):** Menú tipo Drawer desplegable, tarjetas adaptables y tablas con desplazamiento horizontal (`overflow-x-auto`) sin pérdida de legibilidad.

---

## 8. Despliegue de Reglas de Seguridad en Firebase

Los archivos de reglas se encuentran en la raíz del proyecto:
- `/firestore.rules` (Cloud Firestore)
- `/database.rules.json` (Realtime Database)

Para aplicarlos en tu proyecto de Firebase:
```bash
# Si tienes Firebase CLI instalado en tu terminal local:
firebase deploy --only firestore:rules,database
```
O de forma manual:
1. Abre **Firebase Console** > **Firestore Database** > Pestaña **Reglas**.
2. Pega el contenido exacto de `firestore.rules` y haz clic en **Publicar**.
3. Abre **Realtime Database** > Pestaña **Reglas**.
4. Pega el contenido de `database.rules.json` y haz clic en **Publicar**.
