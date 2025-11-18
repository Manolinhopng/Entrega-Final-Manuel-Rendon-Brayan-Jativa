// src/controllers/routinesController.js
const getRoutines = async (req, res) => {
    const { userId } = req.query;
    if (!userId) return res.status(400).json({ message: 'userId requerido' });
  
    try {
      const snapshot = await req.app.locals.db.collection('routines')
        .where('userId', '==', userId)
        .orderBy('date', 'desc')
        .get();
  
      const routines = [];
      snapshot.forEach(doc => routines.push({ id: doc.id, ...doc.data() }));
      res.json(routines);
    } catch (error) {
      res.status(500).json([]);
    }
  };
  
  const createRoutine = async (req, res) => {
    try {
      const ref = await req.app.locals.db.collection('routines').add(req.body);
      const doc = await ref.get();
      res.status(201).json({ id: doc.id, ...doc.data() });
    } catch (error) {
      res.status(500).json({ message: 'Error al guardar rutina' });
    }
  };
  
  module.exports = { getRoutines, createRoutine };