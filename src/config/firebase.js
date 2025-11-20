require('dotenv').config();
const admin = require('firebase-admin');

let firebaseApp;

try {
    if (process.env.FIREBASE_SERVICE_ACCOUNT) {
        const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);

        firebaseApp = admin.initializeApp({
            credential: admin.credential.cert(serviceAccount),
            databaseURL: `https://${serviceAccount.project_id}.firebaseio.com`
        });

        console.log('✅ Firebase Admin inicializado con variables de entorno');
    } else {
        throw new Error("FIREBASE_SERVICE_ACCOUNT no está definido en .env");
    }

} catch (error) {
    console.error('❌ Error al inicializar Firebase Admin:', error);
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
