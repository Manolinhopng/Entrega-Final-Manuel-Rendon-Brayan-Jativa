// src/controllers/profileController.js
const getProfile = async (req, res) => {
  try {
    const { userId } = req.params;
    
    if (!userId) {
      return res.status(400).json({ 
        success: false, 
        message: 'userId es requerido' 
      });
    }

    const userDoc = await req.app.locals.db.collection('users').doc(userId).get();
    
    if (!userDoc.exists) {
      return res.status(404).json({ 
        success: false, 
        message: 'Usuario no encontrado' 
      });
    }

    const userData = userDoc.data();
    
    // No devolver la contraseña en el perfil
    const { password, ...profileData } = userData;
    
    res.json({ 
      success: true,
      data: { ...profileData, id: userDoc.id } 
    });
  } catch (error) {
    console.error('Error en getProfile:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Error al cargar perfil: ' + error.message 
    });
  }
};

const updateProfile = async (req, res) => {
  try {
    const { userId } = req.params;
    const updateData = req.body;
    
    if (!userId) {
      return res.status(400).json({ 
        success: false, 
        message: 'userId es requerido' 
      });
    }

    // Verificar que el usuario existe
    const userDoc = await req.app.locals.db.collection('users').doc(userId).get();
    if (!userDoc.exists) {
      return res.status(404).json({ 
        success: false, 
        message: 'Usuario no encontrado' 
      });
    }

    // Preparar datos para actualizar (excluir campos sensibles)
    const allowedFields = [
      'name', 'age', 'email', 'weight', 'height', 'gender', 
      'trainingFrequency', 'dietType', 'updatedAt'
    ];
    
    const updateFields = {};
    for (const [key, value] of Object.entries(updateData)) {
      if (allowedFields.includes(key)) {
        updateFields[key] = value;
      }
    }
    
    // Añadir timestamp de actualización
    updateFields.updatedAt = admin.firestore.FieldValue.serverTimestamp();

    const userRef = req.app.locals.db.collection('users').doc(userId);
    await userRef.update(updateFields);
    
    const updatedDoc = await userRef.get();
    const updatedData = updatedDoc.data();
    
    // No devolver la contraseña en la respuesta
    const { password, ...profileData } = updatedData;
    
    res.json({ 
      success: true,
      message: 'Perfil actualizado exitosamente',
      data: { ...profileData, id: updatedDoc.id } 
    });

  } catch (error) {
    console.error('Error en updateProfile:', error);
    
    // Manejar errores específicos
    if (error.code === 7) { // PERMISSION_DENIED
      return res.status(403).json({ 
        success: false, 
        message: 'No tienes permiso para actualizar este perfil' 
      });
    }
    
    if (error.code === 5) { // NOT_FOUND
      return res.status(404).json({ 
        success: false, 
        message: 'Usuario no encontrado' 
      });
    }
    
    res.status(500).json({ 
      success: false, 
      message: 'Error al actualizar perfil: ' + error.message 
    });
  }
};

// Función para actualizar solo ciertos campos del perfil
const updateSpecificFields = async (req, res) => {
  try {
    const { userId } = req.params;
    const { field, value } = req.body;
    
    if (!userId || !field || value === undefined) {
      return res.status(400).json({ 
        success: false, 
        message: 'userId, field y value son requeridos' 
      });
    }

    // Verificar que el campo es permitido
    const allowedFields = ['weight', 'height', 'trainingFrequency', 'dietType'];
    if (!allowedFields.includes(field)) {
      return res.status(400).json({ 
        success: false, 
        message: `Campo '${field}' no permitido` 
      });
    }

    const userRef = req.app.locals.db.collection('users').doc(userId);
    await userRef.update({
      [field]: value,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    res.json({ 
      success: true,
      message: `Campo ${field} actualizado exitosamente` 
    });

  } catch (error) {
    console.error('Error en updateSpecificFields:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Error al actualizar campo: ' + error.message 
    });
  }
};

// Importante: Necesitas importar admin
const { admin } = require('../config/firebase');

module.exports = { 
  getProfile, 
  updateProfile,
  updateSpecificFields // Opcional: para actualizaciones específicas
};