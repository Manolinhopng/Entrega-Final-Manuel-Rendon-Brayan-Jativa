require('dotenv').config();
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

function tryParseServiceAccount(raw) {
    if (!raw) return null;

    // Intentos de parseo en diferentes formas comunes
    const attempts = [
        raw,
        raw.replace(/\r/g, ''),            // eliminar CR
        raw.replace(/\\n/g, '\n'),        // convertir secuencias "\n" en saltos reales
    ];

    for (const attempt of attempts) {
        try {
            return JSON.parse(attempt);
        } catch (err) {
            // seguir intentando
        }
    }

    // intentar interpretar como base64
    try {
        const decoded = Buffer.from(raw, 'base64').toString('utf8');
        return JSON.parse(decoded);
    } catch (err) {
        return null;
    }
}

let firebaseApp;
let serviceAccount = null;

try {
    if (process.env.FIREBASE_SERVICE_ACCOUNT) {
        const parsed = tryParseServiceAccount(process.env.FIREBASE_SERVICE_ACCOUNT);
        if (!parsed) {
            throw new Error('FIREBASE_SERVICE_ACCOUNT no es JSON válido. Puedes: 1) poner la JSON en una sola línea y usar \\n para los saltos, o 2) codificarla en base64.');
        }
        // Asegurar que private_key contiene saltos reales
        if (parsed.private_key && parsed.private_key.includes('\\n')) {
            parsed.private_key = parsed.private_key.replace(/\\n/g, '\n');
        }
        serviceAccount = parsed;

        firebaseApp = admin.initializeApp({
            credential: admin.credential.cert(serviceAccount),
            databaseURL: `https://${serviceAccount.project_id}.firebaseio.com`
        });
        console.log('✅ Firebase Admin inicializado con variables de entorno');
    } else {
        // fallback a archivo local serviceAccountKey.json (útil en desarrollo)
        const localPath = path.resolve(__dirname, '../../serviceAccountKey.json');
        if (fs.existsSync(localPath)) {
            const raw = fs.readFileSync(localPath, 'utf8');
            serviceAccount = JSON.parse(raw);
            firebaseApp = admin.initializeApp({
                credential: admin.credential.cert(serviceAccount),
                databaseURL: `https://${serviceAccount.project_id}.firebaseio.com`
            });
            console.log('✅ Firebase Admin inicializado desde serviceAccountKey.json');
        } else {
            throw new Error("FIREBASE_SERVICE_ACCOUNT no está definido en .env y no se encontró serviceAccountKey.json en la raíz del proyecto");
        }
    }

} catch (error) {
    console.error('❌ Error al inicializar Firebase Admin:', error.message || error);
    throw error;
}

const db = admin.firestore();
const auth = admin.auth();
const storage = admin.storage();

module.exports = {
    admin,
    db,
    auth,
    storage,
    firebaseApp
};
