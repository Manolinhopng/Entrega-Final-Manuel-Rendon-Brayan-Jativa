// src/config/firebase.js
require('dotenv').config();
const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

function tryParseServiceAccount(raw) {
    if (!raw) return null;

    const attempts = [
        raw,
        raw.replace(/\r/g, ''),
        raw.replace(/\\n/g, '\n'),
    ];

    for (const attempt of attempts) {
        try {
            return JSON.parse(attempt);
        } catch (err) {
            // seguir intentando
        }
    }

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

// Especifica la base de datos 'dbprincipal'
const db = admin.firestore();
// Si necesitas acceder a la base de datos específica
db._settings = { ...db._settings, databaseId: 'dbprincipal' };

const customDb = admin.firestore();
customDb.settings({ databaseId: 'dbprincipal' });

const auth = admin.auth();
const storage = admin.storage();

module.exports = {
    admin,
    db: customDb, // Usa la base de datos personalizada
    auth,
    storage,
    firebaseApp
};