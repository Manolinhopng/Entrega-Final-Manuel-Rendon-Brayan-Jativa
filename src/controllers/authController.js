// src/controllers/authController.js
const { admin } = require('../config/firebase');

const login = async (req, res) => {
  const { email, password } = req.body;
  try {
    const snapshot = await req.app.locals.db.collection('users')
      .where('email', '==', email)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return res.status(401).json({ success: false, message: 'Usuario no encontrado' });
    }

    const userDoc = snapshot.docs[0];
    const userData = userDoc.data();

    if (userData.password !== password) {
      return res.status(401).json({ success: false, message: 'Contraseña incorrecta' });
    }

    res.json({
      success: true,
      userId: userDoc.id,
      message: 'Login exitoso'
    });
  } catch (error) {
    console.error('Error en login:', error);
    res.status(500).json({ success: false, message: 'Error interno' });
  }
};

const register = async (req, res) => {
  const { name, age, email, weight, height, gender } = req.body;
  try {
    const existing = await req.app.locals.db.collection('users')
      .where('email', '==', email)
      .get();
    if (!existing.empty) {
      return res.status(400).json({ success: false, message: 'Correo ya registrado' });
    }

    const newUser = {
      name,
      age: parseInt(age),
      email,
      weight,
      height,
      gender,
      trainingFrequency: '',
      dietType: '',
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };

    const userRef = await req.app.locals.db.collection('users').add(newUser);
    res.status(201).json({
      success: true,
      userId: userRef.id,
      message: 'Registro exitoso'
    });
  } catch (error) {
    console.error('Error en register:', error);
    res.status(500).json({ success: false, message: 'Error al registrar' });
  }
};

module.exports = { login, register };