require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { db } = require('./src/config/firebase');
const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Conexión DB accesible en rutas
app.locals.db = db;

// Rutas
const authRoutes = require('./src/routes/authRoutes');
const profileRoutes = require('./src/routes/profileRoutes');
const routinesRoutes = require('./src/routes/routinesRoutes');

app.use('/api/auth', authRoutes);
app.use('/api/profile', profileRoutes);
app.use('/api/routines', routinesRoutes);

app.get('/', (req, res) => {
  res.json({ message: 'GymTracker Backend - API v1 ✅' });
});

// Iniciar servidor
app.listen(PORT, () => {
  console.log(`Servidor corriendo en http://localhost:${PORT}`);
});
