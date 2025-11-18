// src/controllers/authController.js
const { admin } = require('../config/firebase');
const crypto = require('crypto'); // Módulo nativo de Node.js para hashing

// Función para cifrar contraseña con SHA-256
const hashPassword = (password) => {
  return crypto.createHash('sha256').update(password).digest('hex');
};

const login = async (req, res) => {
  const { email, password } = req.body;
  
  try {
    // Hashear la contraseña proporcionada para la comparación
    const hashedPassword = hashPassword(password);
    
    const snapshot = await req.app.locals.db.collection('users')
      .where('email', '==', email)
      .limit(1)
      .get();

    if (snapshot.empty) {
      return res.status(401).json({ 
        success: false, 
        message: 'Usuario no encontrado' 
      });
    }

    const userDoc = snapshot.docs[0];
    const userData = userDoc.data();

    // Comparar contraseñas hash
    if (userData.password !== hashedPassword) {
      return res.status(401).json({ 
        success: false, 
        message: 'Contraseña incorrecta' 
      });
    }

    res.json({
      success: true,
      userId: userDoc.id,
      email: userData.email,
      name: userData.name,
      message: 'Login exitoso'
    });
  } catch (error) {
    console.error('Error en login:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Error interno del servidor' 
    });
  }
};

const register = async (req, res) => {
  const { name, age, email, weight, height, gender, password } = req.body; // Añadido password
  
  // Validación mejorada
  if (!name || !email || !password) {
    return res.status(400).json({ 
      success: false, 
      message: 'Nombre, email y contraseña son requeridos' 
    });
  }

  try {
    const db = req.app.locals.db;

    // Verificar si el email ya existe
    const existing = await db.collection('users')
      .where('email', '==', email)
      .limit(1)
      .get();

    if (!existing.empty) {
      return res.status(400).json({ 
        success: false, 
        message: 'Correo ya registrado' 
      });
    }

    // Hashear la contraseña antes de guardarla
    const hashedPassword = hashPassword(password);

    const newUser = {
      name,
      age: parseInt(age) || 0,
      email,
      password: hashedPassword, // Guardar contraseña hasheada
      weight: weight || null,
      height: height || null,
      gender: gender || '',
      trainingFrequency: '',
      dietType: '',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };

    const userRef = await db.collection('users').add(newUser);
    
    console.log(`Usuario creado con ID: ${userRef.id}`);
    
    // No devolver la contraseña en la respuesta
    res.status(201).json({
      success: true,
      userId: userRef.id,
      email: newUser.email,
      name: newUser.name,
      message: 'Registro exitoso'
    });

  } catch (error) {
    console.error('Error en register:', error);
    
    // Manejar errores específicos de Firestore
    if (error.code === 7) { // PERMISSION_DENIED
      return res.status(500).json({ 
        success: false, 
        message: 'Error de permisos de Firestore. Verifica las reglas de seguridad.' 
      });
    }
    
    if (error.code === 3) { // INVALID_ARGUMENT
      return res.status(500).json({ 
        success: false, 
        message: 'Datos inválidos para Firestore.' 
      });
    }
    
    res.status(500).json({ 
      success: false, 
      message: 'Error al registrar: ' + error.message 
    });
  }
};

// Función para actualizar contraseña
const updatePassword = async (req, res) => {
  const { userId, currentPassword, newPassword } = req.body;
  
  if (!userId || !currentPassword || !newPassword) {
    return res.status(400).json({ 
      success: false, 
      message: 'userId, currentPassword y newPassword son requeridos' 
    });
  }

  try {
    const db = req.app.locals.db;
    
    // Obtener usuario actual
    const userDoc = await db.collection('users').doc(userId).get();
    if (!userDoc.exists) {
      return res.status(404).json({ 
        success: false, 
        message: 'Usuario no encontrado' 
      });
    }

    const userData = userDoc.data();
    const currentHashedPassword = hashPassword(currentPassword);

    // Verificar contraseña actual
    if (userData.password !== currentHashedPassword) {
      return res.status(401).json({ 
        success: false, 
        message: 'Contraseña actual incorrecta' 
      });
    }

    // Hashear nueva contraseña
    const newHashedPassword = hashPassword(newPassword);

    // Actualizar contraseña
    await db.collection('users').doc(userId).update({
      password: newHashedPassword,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    res.json({
      success: true,
      message: 'Contraseña actualizada exitosamente'
    });

  } catch (error) {
    console.error('Error al actualizar contraseña:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Error al actualizar contraseña: ' + error.message 
    });
  }
};

// Función auxiliar para inicializar la colección (opcional)
const initializeUsersCollection = async (db) => {
  try {
    const tempRef = db.collection('users').doc('initialization_doc');
    await tempRef.set({
      _initialized: true,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
    
    setTimeout(async () => {
      try {
        await tempRef.delete();
        console.log('Documento de inicialización eliminado');
      } catch (deleteError) {
        console.log('No se pudo eliminar documento de inicialización:', deleteError);
      }
    }, 2000);
    
    console.log('Colección "users" inicializada correctamente');
    return true;
  } catch (error) {
    console.error('Error al inicializar colección users:', error);
    return false;
  }
};

module.exports = { 
  login, 
  register, 
  updatePassword, // Añadido nuevo endpoint
  initializeUsersCollection 
};