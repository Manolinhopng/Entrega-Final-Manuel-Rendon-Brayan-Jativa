// src/controllers/routinesController.js
const getRoutines = async (req, res) => {
  const { userId } = req.query;
  
  if (!userId) {
    return res.status(400).json({ 
      success: false, 
      message: 'userId es requerido' 
    });
  }

  try {
    console.log('Buscando rutinas para userId:', userId);
    
    const snapshot = await req.app.locals.db.collection('routines')
      .where('userId', '==', userId)
      .orderBy('date', 'desc')
      .orderBy('createdAt', 'desc') // Orden secundario por si hay fechas iguales
      .get();

    if (snapshot.empty) {
      return res.json({ 
        success: true,
        routines: [],
        message: 'No se encontraron rutinas para este usuario'
      });
    }

    const routines = [];
    snapshot.forEach(doc => {
      const routineData = doc.data();
      routines.push({ 
        id: doc.id, 
        ...routineData 
      });
    });

    res.json({ 
      success: true,
      routines 
    });

  } catch (error) {
    console.error('Error en getRoutines:', error);
    
    if (error.code === 7) { // PERMISSION_DENIED
      return res.status(403).json({ 
        success: false, 
        message: 'No tienes permiso para acceder a estas rutinas' 
      });
    }
    
    res.status(500).json({ 
      success: false, 
      message: 'Error al cargar rutinas: ' + error.message 
    });
  }
};

const createRoutine = async (req, res) => {
  const { userId, name, exercises, date, duration } = req.body;
  
  // Validación básica
  if (!userId) {
    return res.status(400).json({ 
      success: false, 
      message: 'userId es requerido' 
    });
  }

  if (!name) {
    return res.status(400).json({ 
      success: false, 
      message: 'name de la rutina es requerido' 
    });
  }

  try {
    // Validar que el usuario existe
    const userDoc = await req.app.locals.db.collection('users').doc(userId).get();
    if (!userDoc.exists) {
      return res.status(404).json({ 
        success: false, 
        message: 'Usuario no encontrado' 
      });
    }

    // Preparar datos de la rutina
    const routineData = {
      userId,
      name,
      exercises: exercises || [],
      date: date || new Date().toISOString().split('T')[0], // Fecha actual si no se proporciona
      duration: duration || null,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      completed: false // Estado inicial
    };

    const ref = await req.app.locals.db.collection('routines').add(routineData);
    const doc = await ref.get();
    
    console.log('Rutina creada con ID:', doc.id);

    res.status(201).json({ 
      success: true,
      message: 'Rutina creada exitosamente',
      data: { 
        id: doc.id, 
        ...doc.data() 
      } 
    });

  } catch (error) {
    console.error('Error en createRoutine:', error);
    
    if (error.code === 7) { // PERMISSION_DENIED
      return res.status(403).json({ 
        success: false, 
        message: 'No tienes permiso para crear rutinas' 
      });
    }
    
    if (error.code === 3) { // INVALID_ARGUMENT
      return res.status(400).json({ 
        success: false, 
        message: 'Datos inválidos para la rutina' 
      });
    }
    
    res.status(500).json({ 
      success: false, 
      message: 'Error al guardar rutina: ' + error.message 
    });
  }
};

// Actualizar rutina existente
const updateRoutine = async (req, res) => {
  const { routineId } = req.params;
  const updateData = req.body;

  if (!routineId) {
    return res.status(400).json({ 
      success: false, 
      message: 'routineId es requerido' 
    });
  }

  try {
    const routineRef = req.app.locals.db.collection('routines').doc(routineId);
    const routineDoc = await routineRef.get();

    if (!routineDoc.exists) {
      return res.status(404).json({ 
        success: false, 
        message: 'Rutina no encontrada' 
      });
    }

    // Preparar datos para actualizar
    const allowedFields = ['name', 'exercises', 'date', 'duration', 'completed'];
    const updateFields = {};
    
    for (const [key, value] of Object.entries(updateData)) {
      if (allowedFields.includes(key)) {
        updateFields[key] = value;
      }
    }
    
    updateFields.updatedAt = admin.firestore.FieldValue.serverTimestamp();

    await routineRef.update(updateFields);
    const updatedDoc = await routineRef.get();

    res.json({ 
      success: true,
      message: 'Rutina actualizada exitosamente',
      data: { 
        id: updatedDoc.id, 
        ...updatedDoc.data() 
      } 
    });

  } catch (error) {
    console.error('Error en updateRoutine:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Error al actualizar rutina: ' + error.message 
    });
  }
};

// Eliminar rutina
const deleteRoutine = async (req, res) => {
  const { routineId } = req.params;

  if (!routineId) {
    return res.status(400).json({ 
      success: false, 
      message: 'routineId es requerido' 
    });
  }

  try {
    const routineRef = req.app.locals.db.collection('routines').doc(routineId);
    const routineDoc = await routineRef.get();

    if (!routineDoc.exists) {
      return res.status(404).json({ 
        success: false, 
        message: 'Rutina no encontrada' 
      });
    }

    await routineRef.delete();

    res.json({ 
      success: true,
      message: 'Rutina eliminada exitosamente' 
    });

  } catch (error) {
    console.error('Error en deleteRoutine:', error);
    res.status(500).json({ 
      success: false, 
      message: 'Error al eliminar rutina: ' + error.message 
    });
  }
};

// Importante: Necesitas importar admin
const { admin } = require('../config/firebase');

module.exports = { 
  getRoutines, 
  createRoutine,
  updateRoutine, // Opcional: para actualizar rutinas
  deleteRoutine  // Opcional: para eliminar rutinas
};