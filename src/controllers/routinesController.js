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


const getTrainingStats = async (req, res) => {
  const { userId } = req.query; // O req.params, dependiendo de cómo lo quieras enviar

  if (!userId) {
    return res.status(400).json({
      success: false,
      message: 'userId es requerido'
    });
  }

  try {
    console.log('Calculando estadísticas para userId:', userId);

    // Consultar Firestore para rutinas COMPLETADAS del usuario
    const snapshot = await req.app.locals.db.collection('routines')
      .where('userId', '==', userId)
      .where('completed', '==', true) // <-- Filtro crucial
      .get();

    if (snapshot.empty) {
      // Devolver valores por defecto si no hay rutinas completadas
      return res.json({
        success: true,
        stats: {
          totalSessions: 0,
          totalDurationSeconds: 0,
          totalWeightLifted: 0.0
        }
      });
    }

    let totalSessions = 0;
    let totalDurationSeconds = 0;
    let totalWeightLifted = 0.0;

    snapshot.forEach(doc => {
      const routine = doc.data();

      // 1. Contar rutina
      totalSessions++;

      // 2. Sumar duración
      if (routine.duration) {
        const durationParts = routine.duration.split(':');
        if (durationParts.length === 3) {
          const hours = parseInt(durationParts[0]) || 0;
          const minutes = parseInt(durationParts[1]) || 0;
          const seconds = parseInt(durationParts[2]) || 0;
          totalDurationSeconds += (hours * 3600) + (minutes * 60) + seconds;
        }
      }

      // 3. Sumar peso total
      if (Array.isArray(routine.exercises)) {
        routine.exercises.forEach(exercise => {
          // Asegúrate de que weight y reps existan y sean números
          const weight = parseFloat(exercise.weight) || 0;
          const reps = parseInt(exercise.reps) || 0;
          totalWeightLifted += weight * reps;
        });
      }
    });

    res.json({
      success: true,
      stats: {
        totalSessions,
        totalDurationSeconds,
        totalWeightLifted
      }
    });

  } catch (error) {
    console.error('Error en getTrainingStats:', error);

    if (error.code === 7) { // PERMISSION_DENIED
      return res.status(403).json({
        success: false,
        message: 'No tienes permiso para acceder a estas estadísticas'
      });
    }

    res.status(500).json({
      success: false,
      message: 'Error al calcular estadísticas: ' + error.message
    });
  }
};

// Finalizar rutina
const finishRoutine = async (req, res) => {
  const { routineId } = req.params;
  const { duration } = req.body; // "01:32:15" por ejemplo

  if (!routineId) {
    return res.status(400).json({ success: false, message: 'routineId es requerido' });
  }

  try {
    const routineRef = req.app.locals.db.collection('routines').doc(routineId);
    const routineDoc = await routineRef.get();

    if (!routineDoc.exists) {
      return res.status(404).json({ success: false, message: 'Rutina no encontrada' });
    }

    await routineRef.update({
      completed: true,
      duration: duration,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    res.json({
      success: true,
      message: 'Rutina finalizada correctamente'
    });

  } catch (error) {
    console.error('Error en finishRoutine:', error);
    res.status(500).json({
      success: false,
      message: 'Error al finalizar rutina: ' + error.message
    });
  }
};


const { admin } = require('../config/firebase');

module.exports = {
  getRoutines,
  createRoutine,
  updateRoutine,
  deleteRoutine,
  getTrainingStats,
  finishRoutine
};