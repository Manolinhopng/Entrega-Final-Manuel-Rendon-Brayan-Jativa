// src/controllers/profileController.js
const getProfile = async (req, res) => {
    try {
      const userDoc = await req.app.locals.db.collection('users').doc(req.params.userId).get();
      if (!userDoc.exists) {
        return res.status(404).json({ message: 'Usuario no encontrado' });
      }
      res.json({ ...userDoc.data(), id: userDoc.id });
    } catch (error) {
      res.status(500).json({ message: 'Error al cargar perfil' });
    }
  };
  
  const updateProfile = async (req, res) => {
    try {
      const userRef = req.app.locals.db.collection('users').doc(req.params.userId);
      await userRef.update(req.body);
      const updated = await userRef.get();
      res.json({ ...updated.data(), id: updated.id });
    } catch (error) {
      res.status(500).json({ message: 'Error al actualizar' });
    }
  };
  
  module.exports = { getProfile, updateProfile };