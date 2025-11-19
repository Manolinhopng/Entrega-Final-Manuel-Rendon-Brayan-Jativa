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

    // No devolver contraseña
    const { password, ...profileData } = userData;

    // Asegurar que el historial existe
    profileData.pesoHistorico = userData.pesoHistorico || [];

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

    const userDoc = await req.app.locals.db.collection('users').doc(userId).get();
    if (!userDoc.exists) {
      return res.status(404).json({
        success: false,
        message: 'Usuario no encontrado'
      });
    }

    const allowedFields = [
      'name', 'age', 'email', 'weight', 'height', 'gender',
      'trainingFrequency', 'dietType'
    ];

    const updateFields = {};

    // Recorrer los campos
    for (const [key, value] of Object.entries(updateData)) {
      if (allowedFields.includes(key)) {
        updateFields[key] = value;

        // ⬇️ Historial automático de peso
        if (key === 'weight') {
          updateFields.pesoHistorico =
            admin.firestore.FieldValue.arrayUnion(value);
        }
      }
    }

    // Timestamp automático
    updateFields.updatedAt = admin.firestore.FieldValue.serverTimestamp();

    const userRef = req.app.locals.db.collection('users').doc(userId);
    await userRef.update(updateFields);

    const updatedDoc = await userRef.get();
    const updatedData = updatedDoc.data();

    const { password, ...profileData } = updatedData;

    // Asegurar que exista el array
    profileData.pesoHistorico = updatedData.pesoHistorico || [];

    res.json({
      success: true,
      message: 'Perfil actualizado exitosamente',
      data: { ...profileData, id: updatedDoc.id }
    });

  } catch (error) {
    console.error('Error en updateProfile:', error);

    if (error.code === 7) {
      return res.status(403).json({
        success: false,
        message: 'No tienes permiso para actualizar este perfil'
      });
    }

    if (error.code === 5) {
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

    const allowedFields = ['weight', 'height', 'trainingFrequency', 'dietType'];
    if (!allowedFields.includes(field)) {
      return res.status(400).json({
        success: false,
        message: `Campo '${field}' no permitido`
      });
    }

    const updateObj = {
      [field]: value,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    };

    if (field === 'weight') {
      updateObj.pesoHistorico =
        admin.firestore.FieldValue.arrayUnion(value);
    }

    await req.app.locals.db.collection('users').doc(userId).update(updateObj);

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

const { admin } = require('../config/firebase');

module.exports = {
  getProfile,
  updateProfile,
  updateSpecificFields
};